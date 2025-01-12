/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import net.minecraft.entity.Entity

/**
 * An animation that requires entity state. It is able to prevent some idle
 * animations, usually on the basis of what [ModelFrame] class the animation
 * uses.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
interface StatefulAnimation<T : Entity, F : ModelFrame> {
    val isTransform: Boolean
    val duration: Float
    /** Whether the animation will resist being cleared when a primary animation is added. */
    val enduresPrimaryAnimations: Boolean
    /** Runs the animation. You can check that the model fits a particular frame. Returns true if the animation should continue. */
    fun run(
        entity: T?,
        model: PoseableEntityModel<T>,
        state: PoseableEntityState<T>,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ): Boolean

    fun applyEffects(entity: T, state: PoseableEntityState<T>, previousSeconds: Float, newSeconds: Float) {}
}