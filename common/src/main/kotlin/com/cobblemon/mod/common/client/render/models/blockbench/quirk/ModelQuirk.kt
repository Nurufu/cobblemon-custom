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
import net.minecraft.entity.Entity

abstract class ModelQuirk<T : Entity, D : QuirkData<T>> {
    abstract fun createData(): D
    protected abstract fun tick(state: PosableState<T>, data: D)
    fun tick(entity: T?, model: PoseableEntityModel<T>, state: PosableState<T>, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        val data = getOrCreateData(state)
        tick(state, data)
        data.run(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity)
    }
    fun getOrCreateData(state: PosableState<T>): D {
        return state.quirks.getOrPut(this, this::createData) as D
    }
}