/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes

fun Entity.setPositionSafely(pos: Vec3d): Boolean {
    // TODO: Rework this function. Best way to do it would be to define a vertical and horizontal min/max shift based on the Pokemon's hitbox.
    // Then loop through horizontal/vertical shifts, and detect collisions in three categories: suffocation, damaging blocks, and general collision
    // The closest position with the least severe collision types will be selected to move the Pokemon to
    // The throw could be cancelled if there are no viable locations without severe problems

    // Optional: use getBlockCollisions iterator and VoxelShapes.combineAndSimplify to create a single cube to represent collision area
    // Use that cube to "push" the Pokemon out of the wall at an angle
    // Note: may not work well with L shape wall collisions
//    val collisions = world.getBlockCollisions(this, box).iterator()
//    if (collisions.hasNext()) {
//        var collisionShape = collisions.next()
//        while (collisions.hasNext()) {
//            collisionShape = VoxelShapes.union(collisionShape, collisions.next())
//            println(collisionShape)
//        }
//    } else {
//        setPosition(pos)
//        return true
//    }

    var result = pos
    val eyes = pos.withAxis(Direction.Axis.Y, pos.y + this.standingEyeHeight)

    var box = boundingBox.offset(pos)
    val conflicts = mutableSetOf<Direction>()

    if (!world.getBlockCollisions(this, box).iterator().hasNext()) {
        setPosition(pos)
        return true
    }

    for (target in BlockPos.stream(box)) {
        val blockState = this.world.getBlockState(target)
        val collides = !blockState.isAir &&
                blockState.shouldSuffocate(this.world, target) &&
                VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.world, target)
                    .offset(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    VoxelShapes.cuboid(box),
                    BooleanBiFunction.AND
                )
        if (collides) {
            val x = eyes.toBlockPos()
            for (direction in Direction.values()) {
                if (conflicts.contains(direction)) continue

                val conflict = target.toVec3d()
                if (x.add(direction.vector) == target) {
                    conflicts.add(direction)
                    when (direction) {
                        Direction.UP -> return false
                        Direction.NORTH -> {
                            result = result.add(Vec3d(0.0, 0.0, 1 + (conflict.z - box.minZ + (1.0 / 8.0))))
                        }
                        Direction.SOUTH -> {
                            result = result.add(Vec3d(0.0, 0.0, (conflict.z - box.maxZ) - (1.0 / 8.0)))
                        }
                        Direction.WEST -> {
                            result = result.add(Vec3d(1 + (conflict.x - box.minX  + (1.0 / 8.0)), 0.0, 0.0))
                        }
                        Direction.EAST -> {
                            result = result.add(Vec3d((conflict.x - box.maxX) - (1.0 / 8.0), 0.0, 0.0))
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    box = boundingBox.offset(result)
    if (!world.getBlockCollisions(this, box).iterator().hasNext()) {
        this.setPosition(result)
        return true
    } else {
        val yChanges = listOf(1.0, -1.0, 2.0, -2.0)
        var previousChange = 0.0

        // If the Pokemon still collides with blocks after horizontal shifting, try vertical shifting from the shifted position
        for (yChange in yChanges) {
            box = box.offset(0.0, yChange - previousChange, 0.0)
            previousChange = yChange
            if (world.getBlockCollisions(this, box).iterator().hasNext()) {
                continue
            } else {
                val roundedY = (result.y + yChange).toInt()
                box = box.offset(0.0, roundedY - result.y, 0.0)
                // If the rounded position actually collides again, then don't round at all.
                if (world.getBlockCollisions(this, box).iterator().hasNext()) {
                    result = result.add(0.0, yChange, 0.0)
                    this.setPosition(result)
                    return true
                } else {
                    result = Vec3d(result.x, roundedY.toDouble(), result.z)
                    this.setPosition(result)
                    return true
                }
            }
        }

        // If vertical shifting from the new position still collides, try vertical shifting from the original pos
        previousChange = 0.0
        box = boundingBox.offset(pos)
        for (yChange in yChanges) {
            box = box.offset(0.0, yChange - previousChange, 0.0)
            previousChange = yChange
            if (world.getBlockCollisions(this, box).iterator().hasNext()) {
                continue
            } else {
                val roundedY = (result.y + yChange).toInt()
                box = box.offset(0.0, roundedY - result.y, 0.0)
                // If the rounded position actually collides again, then don't round at all.
                if (world.getBlockCollisions(this, box).iterator().hasNext()) {
                    result = result.add(0.0, yChange, 0.0)
                    this.setPosition(result)
                    return true
                } else {
                    result = Vec3d(result.x, roundedY.toDouble(), result.z)
                    this.setPosition(result)
                    return true
                }
            }
        }
    }

    if (conflicts.size >= 3) {
        this.setPosition(pos)
    }

    // This final check guarantees that the sendout will return to the original position if the Pokemon will suffocate in the new one
    // This will only happen if the horizontal shift moved the Pokemon into a suffocating position, and there was no valid vertical shift
    val resultEyes = result.withAxis(Direction.Axis.Y, result.y + this.standingEyeHeight)
    val resultEyeBox = Box.of(resultEyes, width.toDouble(), 1.0E-6, width.toDouble())
    var collides = false

    for (target in BlockPos.stream(resultEyeBox)) {
        val blockState = this.world.getBlockState(target)
        collides = !blockState.isAir &&
                blockState.shouldSuffocate(this.world, target) &&
                VoxelShapes.matchesAnywhere(
                    blockState.getCollisionShape(this.world, target)
                        .offset(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    VoxelShapes.cuboid(box),
                    BooleanBiFunction.AND
                )
        if (collides) break
    }
    if (collides) {
        this.setPosition(pos)
        return true
    } else {
        this.setPosition(result)
        return true
    }
}

fun Entity.isStandingOnSandOrRedSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.world.getBlockState(blockPos.down(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isFullCube(this.world, blockPos.down(a))) {
            return true
        }
        if (sandBlock == Blocks.RED_SAND && !sandBlockState.isAir && sandBlockState.isFullCube(this.world, blockPos.down(a))) {
            return true
        }
    }
    return false
}

fun Entity.isDusk(): Boolean {
    val time = world.timeOfDay % 24000
    return time in 12000..13000
}

fun Entity.isStandingOnSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.world.getBlockState(blockPos.down(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isFullCube(this.world, blockPos.down(a))) {
            return true
        }
    }
    return false
}

fun Entity.isStandingOnRedSand(): Boolean {
    val redSandDepth = 2 // Define the depth you want to check
    for (i in 1..redSandDepth) {
        val redSandBlockState = this.world.getBlockState(blockPos.down(i))
        val redSandBlock = redSandBlockState.block
        if (redSandBlock == Blocks.RED_SAND && !redSandBlockState.isAir && redSandBlockState.isFullCube(this.world, blockPos.down(i))) {
            return true
        }
    }
    return false
}

fun Entity.distanceTo(pos: BlockPos): Double {
    val difference = pos.toVec3d().subtract(this.pos)
    return difference.length()
}

fun Entity.closestPosition(positions: Iterable<BlockPos>, filter: (BlockPos) -> Boolean = { true }): BlockPos? {
    var closest: BlockPos? = null
    var closestDistance = Double.MAX_VALUE

    val iterator = positions.iterator()
    while (iterator.hasNext()) {
        val position = iterator.next()
        if (filter(position)) {
            val distance = distanceTo(position)
            if (distance < closestDistance) {
                closest = BlockPos(position)
                closestDistance = distance
            }
        }
    }

    return closest
}

fun Entity.getIsSubmerged() = isInLava || isSubmergedInWater

fun <T> DataTracker.update(data: TrackedData<T>, mutator: (T) -> T) {
    val value = get(data)
    val newValue = mutator(value)
    if (value != newValue) {
        set(data, newValue)
    }
}