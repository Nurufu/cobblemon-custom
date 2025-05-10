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
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Z_AXIS
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper

/**
 * A bimanual arm animation that will have more force while moving and idle sway.
 * This creates a simple predictable arm movement like Minecraft bimanuals.
 *
 * @author Deltric
 * @since December 21st, 2021
 */
class BimanualSwingAnimation<T : Entity>(
    frame: ModelFrame,
    /** The multiplier to apply to the cosine movement of the arms. The smaller this value, the quicker the arms move. */
    val swingPeriodMultiplier: Float = 0.6662F,
    /** The multiplier to apply to the swing of the entity. The larger this is, the further the arms move. */
    val amplitudeMultiplier: Float = 1F,
    val leftArm: Bone?,
    val rightArm: Bone?
) : StatelessAnimation<T, ModelFrame>(frame) {
    constructor(
        frame: BimanualFrame,
        swingPeriodMultiplier: Float = 0.6662F,
        amplitudeMultiplier: Float = 1F
    ): this(
        frame = frame,
        swingPeriodMultiplier = swingPeriodMultiplier,
        amplitudeMultiplier = amplitudeMultiplier,
        leftArm = frame.leftArm,
        rightArm = frame.rightArm
    )

    override val targetFrame: Class<ModelFrame> = ModelFrame::class.java
    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        // Movement swing
        rightArm?.addRotation(Y_AXIS, MathHelper.cos(limbSwing * swingPeriodMultiplier) * limbSwingAmount * amplitudeMultiplier * intensity)
        leftArm?.addRotation(Y_AXIS, MathHelper.cos(limbSwing * swingPeriodMultiplier) * limbSwingAmount * amplitudeMultiplier * intensity)

        // Idle sway
        rightArm?.addRotation(Z_AXIS, 1.0f * (MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f) * intensity)
        rightArm?.addRotation(Y_AXIS, MathHelper.sin(ageInTicks * 0.067f) * 0.05f * intensity)
        leftArm?.addRotation(Z_AXIS, -1.0f * (MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f) * intensity)
        leftArm?.addRotation(Y_AXIS, -1.0f * MathHelper.sin(ageInTicks * 0.067f) * 0.05f * intensity)
    }
}