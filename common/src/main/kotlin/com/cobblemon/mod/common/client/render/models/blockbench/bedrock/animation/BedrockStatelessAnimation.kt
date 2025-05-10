/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import net.minecraft.entity.Entity

/**
 * Animation that analyzes a [BedrockAnimation] and applies transformations to the model based on
 * the given animation time.
 *
 * @param frame The model frame to apply the animation to
 * @param animation The [BedrockAnimation] to be played
 *
 * @author landonjw
 * @since January 5th, 2022
 */
class BedrockStatelessAnimation<T: Entity>(frame: ModelFrame, val animation: BedrockAnimation) : StatelessAnimation<T, ModelFrame>(frame) {
    override val targetFrame: Class<ModelFrame> = ModelFrame::class.java
    val particleKeyFrames = animation.effects.filterIsInstance<BedrockParticleKeyframe>()

    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        animation.run(model, state, state?.animationSeconds ?: 0F, intensity)
    }

    override fun applyEffects(entity: T, state: PosableState<T>, previousSeconds: Float, newSeconds: Float) {
        val effectiveAnimationLength = animation.animationLength.takeUnless { it <= 0 }?.toFloat() ?: animation.effects.maxOfOrNull { it.seconds }?.takeIf { it != 0F }
        val (loopedPreviousSeconds, loopedNewSeconds) = if (effectiveAnimationLength != null) {
            (previousSeconds % effectiveAnimationLength) to (newSeconds % effectiveAnimationLength)
        } else {
            previousSeconds to newSeconds
        }
        animation.applyEffects(entity, state, loopedPreviousSeconds, loopedNewSeconds)
    }
}