/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pose

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.entity.Entity

/**
 * A pose for a model.
 */
class Pose<T : Entity, F : ModelFrame>(
    var poseName: String,
    val poseTypes: Set<PoseType>,
    val condition: ((T) -> Boolean)?,
    val onTransitionedInto: (PosableState<T>?) -> Unit = {},
    val transformTicks: Int,
    val animations: MutableMap<String, ExpressionLike> = mutableMapOf(),
    val idleAnimations: Array<StatelessAnimation<T, out F>>,
    val transformedParts: Array<ModelPartTransformation>,
    val quirks: Array<ModelQuirk<T, *>>
) {
    fun isSuitable(entity: T) = condition?.invoke(entity) ?: true

    val transitions = mutableMapOf<String, (Pose<T, out ModelFrame>, Pose<T, out ModelFrame>) -> StatefulAnimation<T, ModelFrame>>()

    fun idleStateless(model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float = 0F, limbSwingAmount: Float = 0F, ageInTicks: Float = 0F, headYaw: Float = 0F, headPitch: Float = 0F, intensity: Float) {
        idleAnimations.forEach { it.apply(null, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity) }
    }

    fun idleStateful(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        idleAnimations.filter { state.shouldIdleRun(it, 0F) }.forEach { idleAnimation ->
            idleAnimation.apply(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, state.getIdleIntensity(idleAnimation))
        }
    }
}