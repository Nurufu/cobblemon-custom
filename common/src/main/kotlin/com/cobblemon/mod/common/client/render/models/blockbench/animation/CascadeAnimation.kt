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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.client.model.ModelPart
import net.minecraft.entity.Entity

/**
 * A cascading animation that will increase movement over chained parts
 *
 * @author Deltric
 * @since December 21st, 2021
 */
class CascadeAnimation<T : Entity>(
    frame: ModelFrame,
    val rootFunction: RootFunction,
    val amplitudeFunction: AmplitudeFunction,
    val segments: Array<ModelPart>
): StatelessAnimation<T, ModelFrame>(frame) {

    override val targetFrame = ModelFrame::class.java

    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        segments.forEachIndexed { index, modelPart ->
            modelPart.yaw += rootFunction(ageInTicks) * amplitudeFunction(index+1) * intensity
        }
    }

}

typealias AmplitudeFunction = (Int) -> Float
typealias RootFunction = (Float) -> Float

fun gradualFunction(base: Float = 1F, step: Float = 1F): AmplitudeFunction = { index ->
    base + step * index
}

fun cosineFunction(period: Float = 1F): RootFunction = { x ->
    cos(x * period)
}

fun sineFunction(period: Float = 1F): RootFunction = { x ->
    sin(x * period)
}