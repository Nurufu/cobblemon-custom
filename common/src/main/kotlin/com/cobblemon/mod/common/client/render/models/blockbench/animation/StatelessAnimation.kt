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
import net.minecraft.entity.Entity

/**
 * An animation that can run without an entity associated. These are
 * locked to a specific [ModelFrame], and CAN be given an entity along with
 * limb swing and age information, but might not.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
abstract class StatelessAnimation<T : Entity, F : ModelFrame>(val frame: F) {
    abstract val targetFrame: Class<F>
    open var labels: Set<String> = setOf()
    protected abstract fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float)

    fun apply(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        setAngles(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity)
    }

    open fun applyEffects(entity: T, state: PosableState<T>, previousSeconds: Float, newSeconds: Float) {}
}