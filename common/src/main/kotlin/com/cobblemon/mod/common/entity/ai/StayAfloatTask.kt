/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import net.minecraft.entity.ai.brain.task.MultiTickTask
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.world.ServerWorld

class StayAfloatTask(private val chance: Float = 0F) : MultiTickTask<PathAwareEntity>(emptyMap()) {
    override fun shouldRun(world: ServerWorld, entity: PathAwareEntity): Boolean {
        return entity.isTouchingWater && entity.getFluidHeight(FluidTags.WATER) > entity.swimHeight || entity.isInLava
    }

    override fun shouldKeepRunning(world: ServerWorld, entity: PathAwareEntity, l: Long): Boolean {
        return this.shouldRun(world, entity)
    }

    override fun keepRunning(world: ServerWorld, entity: PathAwareEntity, l: Long) {
        if (entity.random.nextFloat() < this.chance) {
            entity.jumpControl.setActive()
        }
    }
}
