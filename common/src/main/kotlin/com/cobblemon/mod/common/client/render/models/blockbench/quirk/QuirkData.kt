/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.quirk

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import net.minecraft.entity.Entity

open class QuirkData<T : Entity> {
    val animations = mutableListOf<StatefulAnimation<T, *>>()
    var primaryAnimation: PrimaryAnimation<T>? = null

    open fun run(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        if (primaryAnimation != null && state.primaryAnimation != primaryAnimation) {
            primaryAnimation = null
        }
        animations.removeIf { !it.run(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity) }
    }
}