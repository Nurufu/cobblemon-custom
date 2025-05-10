/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.addPosition
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import net.minecraft.client.model.ModelPart
import net.minecraft.entity.Entity

/**
 * Animation simply works by moving a part along a particular function
 */
class TranslationFunctionStatelessAnimation<T : Entity>(
    val part: ModelPart,
    val function: WaveFunction,
    val axis: Int,
    val timeVariable: (state: PosableState<T>?, limbSwing: Float, ageInTicks: Float) -> Float?,
    frame: ModelFrame
) : StatelessAnimation<T, ModelFrame>(frame) {
    override val targetFrame = ModelFrame::class.java
    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        part.addPosition(axis, function(timeVariable(state, limbSwing, ageInTicks) ?: 0F) * intensity)
    }
}