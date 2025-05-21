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
import net.minecraft.entity.ai.brain.task.SingleTickTask
import net.minecraft.entity.ai.brain.task.TaskRunnable
import net.minecraft.entity.ai.brain.task.TaskTriggerer

object AttackAngryAtTask {
    fun create(): SingleTickTask<LivingEntity>  = TaskTriggerer.task {
        it.group(
            it.queryMemoryValue(MemoryModuleType.ANGRY_AT),
            it.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET)
        ).apply(it) { angryAt, attackTarget ->
            TaskRunnable { world, entity, _ ->
                val angryAt = it.getValue(angryAt)
                val livingEntity = world.getEntity(angryAt) as? LivingEntity
                if (livingEntity != null) {
                    entity.brain.remember(MemoryModuleType.ATTACK_TARGET, livingEntity)
                } else {
                    entity.brain.forget(MemoryModuleType.ANGRY_AT)
                }
                return@TaskRunnable true
            }
        }
    }
}