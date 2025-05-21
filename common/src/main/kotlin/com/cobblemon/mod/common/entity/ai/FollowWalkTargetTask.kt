/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import net.minecraft.entity.ai.NoPenaltyTargeting
import net.minecraft.entity.ai.brain.EntityLookTarget
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.WalkTarget
import net.minecraft.entity.ai.brain.task.MultiTickTask
import net.minecraft.entity.ai.pathing.Path
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class FollowWalkTargetTask(
    minRunTime: Int = 150,
    maxRunTime: Int = 250
) : MultiTickTask<PathAwareEntity>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryModuleState.VALUE_PRESENT,
        MemoryModuleType.PATH to MemoryModuleState.VALUE_ABSENT,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE to MemoryModuleState.REGISTERED
    ), minRunTime, maxRunTime
) {
    private var pathUpdateCountdownTicks = 0
    private var path: Path? = null
    private var lookTargetPos: BlockPos? = null
    private var speed = 0f

    override fun shouldRun(arg: ServerWorld, arg2: PathAwareEntity): Boolean {
        if (this.pathUpdateCountdownTicks > 0) {
            --this.pathUpdateCountdownTicks
            return false
        } else {
            val brain = arg2.brain
            val walkTarget = brain.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).get()
            val hasReached = this.hasReached(arg2, walkTarget)
            if (!hasReached && this.hasFinishedPath(arg2, walkTarget, arg.time)) {
                this.lookTargetPos = walkTarget.lookTarget.blockPos
                return true
            } else {
                brain.forget(MemoryModuleType.WALK_TARGET)
                if (hasReached) {
                    brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                }

                return false
            }
        }
    }

    override fun shouldKeepRunning(arg: ServerWorld, arg2: PathAwareEntity, l: Long): Boolean {
        if (this.path != null && this.lookTargetPos != null) {
            val optional = arg2.brain.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET)
            val bl = optional.map(::isTargetSpectator).orElse(false)
            val entityNavigation = arg2.navigation
            return !entityNavigation.isIdle && optional.isPresent && !this.hasReached(arg2, optional.get()) && !bl
        } else {
            return false
        }
    }

    override fun finishRunning(world: ServerWorld, entity: PathAwareEntity, l: Long) {
        val walkTarget = entity.brain.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).orElse(null)
        if (walkTarget != null && !this.hasReached(entity, walkTarget) && entity.navigation.isNearPathStartPos) {
            this.pathUpdateCountdownTicks = world.getRandom().nextInt(40)
        }

        entity.navigation.stop()
        entity.brain.forget(MemoryModuleType.WALK_TARGET)
        entity.brain.forget(MemoryModuleType.PATH)
        this.path = null
    }

    override fun run(arg: ServerWorld, arg2: PathAwareEntity, l: Long) {
        arg2.brain.remember(MemoryModuleType.PATH, this.path)
        arg2.navigation.startMovingAlong(this.path, speed.toDouble())
    }

    override fun keepRunning(world: ServerWorld, entity: PathAwareEntity, l: Long) {
        val path = entity.navigation.currentPath
        val brain = entity.brain
        if (this.path !== path) {
            this.path = path
            brain.remember(MemoryModuleType.PATH, path)
        }

        if (path != null && lookTargetPos != null) {
            val walkTarget = brain.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).get()
            if (walkTarget.lookTarget.blockPos.getSquaredDistance(lookTargetPos) > 4.0 && hasFinishedPath(entity, walkTarget, world.time)) {
                lookTargetPos = walkTarget.lookTarget.blockPos
                run(world, entity, l)
            }
        }
    }

    private fun hasFinishedPath(entity: PathAwareEntity, walkTarget: WalkTarget, time: Long): Boolean {
        val blockPos = walkTarget.lookTarget.blockPos
        this.path = entity.navigation.findPathTo(blockPos, 0)
        this.speed = walkTarget.speed
        val brain = entity.brain
        if (hasReached(entity, walkTarget)) {
            brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        } else {
            val bl = this.path != null && path!!.reachesTarget()
            if (bl) {
                brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            } else if (!brain.hasMemoryModule(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time)
            }

            if (this.path != null) {
                return true
            }

            val vec3d = NoPenaltyTargeting.findTo(
                entity,
                10,
                7,
                Vec3d.ofBottomCenter(blockPos),
                1.5707963705062866
            )
            if (vec3d != null) {
                this.path = entity.navigation.findPathTo(vec3d.x, vec3d.y, vec3d.z, 0)
                return this.path != null
            }
        }

        return false
    }

    private fun hasReached(entity: PathAwareEntity, walkTarget: WalkTarget): Boolean {
        return walkTarget.lookTarget.blockPos.getManhattanDistance(entity.blockPos) <= walkTarget.completionRange
    }

    companion object {
        private const val MAX_UPDATE_COUNTDOWN = 40
        private fun isTargetSpectator(target: WalkTarget): Boolean {
            val lookTarget = target.lookTarget
            return if (lookTarget is EntityLookTarget) {
                lookTarget.entity.isSpectator
            } else {
                false
            }
        }
    }
}
