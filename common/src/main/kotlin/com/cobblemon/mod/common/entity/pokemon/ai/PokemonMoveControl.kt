/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getWaterAndLavaIn
import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.resolveFloat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.FluidTags
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class PokemonMoveControl(val pokemonEntity: PokemonEntity) : MoveControl(pokemonEntity) {
    companion object {
        const val VERY_CLOSE = 2.500000277905201E-3
    }

    val runtime = MoLangRuntime().also {
        it.environment.query.addStandardFunctions().addFunctions(pokemonEntity.struct.functions)
    }

    override fun tick() {
        if (pokemonEntity.pokemon.status?.status == Statuses.SLEEP || pokemonEntity.isDead) {
            pokemonEntity.movementSpeed = 0F
            pokemonEntity.upwardSpeed = 0F
            return
        }

        val behaviour = pokemonEntity.behaviour
        val mediumSpeed = runtime.resolveFloat(if(pokemonEntity.getCurrentPoseType() in setOf(PoseType.FLY, PoseType.HOVER)) {
            behaviour.moving.fly.flySpeedHorizontal
        } else if (pokemonEntity.isSubmergedIn(FluidTags.WATER) || pokemonEntity.isSubmergedIn(FluidTags.LAVA)) {
            behaviour.moving.swim.swimSpeed
        } else {
            behaviour.moving.walk.walkSpeed
        })

        val baseSpeed = entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED).toFloat() * this.speed.toFloat()
        val adjustedSpeed = baseSpeed * mediumSpeed

        if (state == State.STRAFE) {
            var movingDistanceTotal = MathHelper.sqrt(forwardMovement * forwardMovement + sidewaysMovement * sidewaysMovement)
            if (movingDistanceTotal < 1.0f) {
                movingDistanceTotal = 1.0f
            }

            movingDistanceTotal = adjustedSpeed / movingDistanceTotal

            val adjustedForward = forwardMovement * movingDistanceTotal
            val adjustedStrafe = sidewaysMovement * movingDistanceTotal

            val xComponent = -MathHelper.sin(entity.yaw.toRadians())
            val zComponent = MathHelper.cos(entity.yaw.toRadians())
            val xMovement = adjustedForward * zComponent - adjustedStrafe * xComponent
            val zMovement = adjustedStrafe * zComponent + adjustedForward * xComponent
            if (!isWalkable(xMovement, zMovement)) {
                forwardMovement = 1.0f
                sidewaysMovement = 0.0f
            }
            entity.movementSpeed = adjustedSpeed
            entity.setForwardSpeed(forwardMovement)
            entity.setSidewaysSpeed(sidewaysMovement)
            state = State.WAIT
        } else if (state == State.MOVE_TO) {
            // Don't instantly move to WAIT for fluid movements as they overshoot their mark.
            if (!pokemonEntity.isFlying() && !pokemonEntity.isSwimming) {
                state = State.WAIT
            }
            var xDist = targetX - entity.x
            var zDist = targetZ - entity.z
            var yDist = targetY - entity.y

            if (xDist * xDist + yDist * yDist + zDist * zDist < VERY_CLOSE) {
                // If we're close enough, pull up stumps here.
                entity.setForwardSpeed(0F)
                entity.setUpwardSpeed(0F)
                // If we're super close and we're fluid movers, forcefully stop moving so you don't overshoot
                if ((pokemonEntity.isFlying() || pokemonEntity.isSwimming)) {
                    state = State.WAIT
                    entity.velocity = Vec3d.ZERO
                }
                return
            }

            val horizontalDistanceFromTarget = xDist * xDist + zDist * zDist
            val closeHorizontally = horizontalDistanceFromTarget < VERY_CLOSE
            if (!closeHorizontally) {
                val angleToTarget = MathHelper.atan2(zDist, xDist).toDegrees() - 90.0f
                val currentMovingAngle = entity.yaw
                val steppedAngle = MathHelper.stepUnwrappedAngleTowards(currentMovingAngle, angleToTarget,  100 * mediumSpeed)
                entity.yaw = steppedAngle
            }

            val (inWater, inLava) = entity.world.getWaterAndLavaIn(entity.boundingBox)
            val inFluid = inWater || inLava
            var verticalHandled = false
            val blockPos = entity.blockPos
            val blockState = entity.world.getBlockState(blockPos)
            val voxelShape = blockState.getCollisionShape(entity.world, blockPos)

            if (pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) || inFluid) {
                verticalHandled = true
                entity.upwardSpeed = 0F
                entity.movementSpeed = 0F
                // Refinement is to prevent the entity from spinning around trying to get to a super precise location.
                val refine: (Double) -> Double = { if (abs(it) < 0.05) 0.0 else it }

                val fullDistance = Vec3d(
                    xDist,
                    refine(yDist + 0.05), // + 0.05 for dealing with swimming out of water, they otherwise get stuck on the lip
                    zDist
                )

                val direction = fullDistance.normalize()

                val scale = min(adjustedSpeed.toDouble(), fullDistance.length())

                entity.velocity = direction.multiply(scale)

                xDist = fullDistance.x
                zDist = fullDistance.z
                yDist = fullDistance.y
            } else {
                // division is to slow the speed down a bit so they don't overshoot when they get there.
                val forwardSpeed = min(adjustedSpeed, max(horizontalDistanceFromTarget.toFloat() / 2, 0.15F))
                entity.movementSpeed = forwardSpeed
            }

            if (!verticalHandled) {
                val tooBigToStep = yDist > entity.stepHeight.toDouble()
                val xComponent = -MathHelper.sin(entity.yaw.toRadians()).toDouble()
                val zComponent = MathHelper.cos(entity.yaw.toRadians()).toDouble()

                val motion = Vec3d(xComponent, 0.0, zComponent).normalize()
                val offset = motion.multiply(entity.movementSpeed.toDouble())
                val closeEnoughToJump = !entity.doesNotCollide(offset.x, 0.0, offset.z)// sqrt(xDist * xDist + zDist * zDist) - 0.5 <  entity.width / 2 + entity.movementSpeed * 4

                if (tooBigToStep &&
                    closeEnoughToJump ||
                    !voxelShape.isEmpty && entity.y < voxelShape.getMax(Direction.Axis.Y) + blockPos.y.toDouble() &&
                    !blockState.isIn(BlockTags.DOORS) &&
                    !blockState.isIn(BlockTags.FENCES)
                ) {
                    entity.jumpControl.setActive()
                    state = State.JUMPING
                }
            }

            if (closeHorizontally && abs(yDist) < VERY_CLOSE) {
                state = State.WAIT
            }
        } else if (state == State.JUMPING) {
            entity.movementSpeed = adjustedSpeed
            entity.upwardSpeed = 0F
            if (entity.isOnGround || pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                state = State.WAIT
            }
        } else {
            entity.setForwardSpeed(0.0f)
            entity.upwardSpeed = 0F
        }

        if (state == State.WAIT && !entity.navigation.isFollowingPath) {
            if (entity.isOnGround && behaviour.moving.walk.canWalk && pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            }

            // Float
            if (entity.isSubmergedIn(FluidTags.WATER) && behaviour.moving.swim.canSwimInWater) {
                pokemonEntity.setUpwardSpeed(0.2F)
            }
        }
    }

    private fun isWalkable(xMovement: Float, zMovement: Float): Boolean {
        val entityNavigation = entity.navigation
        if (entityNavigation != null) {
            val pathNodeMaker = entityNavigation.nodeMaker
            if (pathNodeMaker != null &&
                pathNodeMaker.getDefaultNodeType(
                    entity.world,
                    MathHelper.floor(entity.x + xMovement.toDouble()),
                    entity.blockY,
                    MathHelper.floor(entity.z + zMovement.toDouble())
                ) != PathNodeType.WALKABLE
            ) {
                return false
            }
        }
        return true
    }
}