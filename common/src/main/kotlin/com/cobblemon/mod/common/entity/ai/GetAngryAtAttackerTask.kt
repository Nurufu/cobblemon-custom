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

object GetAngryAtAttackerTask {
    fun create(): SingleTickTask<LivingEntity> {
        return TaskTriggerer.task {
            it.group(
                it.queryMemoryValue(MemoryModuleType.HURT_BY_ENTITY),
                it.queryMemoryOptional(MemoryModuleType.ANGRY_AT)
            ).apply(it) { hurtByEntity, angryAt ->
                TaskRunnable { _, entity, _ ->
                    val hurtByEntity = it.getValue(hurtByEntity)
                    val angryAt = it.getOptionalValue(angryAt).orElse(null)
                    if (angryAt != null && angryAt == hurtByEntity.uuid) {
                        return@TaskRunnable false
                    }
                    entity.brain.remember(MemoryModuleType.ANGRY_AT, hurtByEntity.uuid)
                    return@TaskRunnable true
                }
            }
        }
    }
}