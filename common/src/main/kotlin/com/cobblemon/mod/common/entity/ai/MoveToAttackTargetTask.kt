/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.WalkTarget
import net.minecraft.entity.ai.brain.task.SingleTickTask
import net.minecraft.entity.ai.brain.task.TaskRunnable
import net.minecraft.entity.ai.brain.task.TaskTriggerer

object MoveToAttackTargetTask {
    fun create(): SingleTickTask<LivingEntity> = TaskTriggerer.task {
        it.group(
            it.queryMemoryValue(MemoryModuleType.ATTACK_TARGET),
            it.queryMemoryOptional(MemoryModuleType.WALK_TARGET)
        ).apply(it) { attackTarget, walkTarget ->
            TaskRunnable { world, entity, _ ->
                val attackTarget = it.getValue(attackTarget)
                val position = attackTarget.pos
                val walkTarget = it.getOptionalValue(walkTarget).orElse(null)
                if (walkTarget == null || walkTarget.lookTarget.pos.squaredDistanceTo(position) > 4.0) {
                    entity.brain.remember(MemoryModuleType.WALK_TARGET, WalkTarget(attackTarget, 0.5F, 1))
                    true
                } else {
                    false
                }
            }
        }
    }
}