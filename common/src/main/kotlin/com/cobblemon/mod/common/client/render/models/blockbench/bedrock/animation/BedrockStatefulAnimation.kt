/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import net.minecraft.entity.Entity

/**
 * A stateful animation that runs a [BedrockAnimation]. It is completed when the underlying
 * bedrock animation is complete.
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
open class BedrockStatefulAnimation<T : Entity>(
    val animation: BedrockAnimation,
) : StatefulAnimation<T, ModelFrame> {
    var startedSeconds = -1F
    override val duration = animation.animationLength.toFloat()
    override var enduresPrimaryAnimations = false
    private var afterAction: (T, PoseableEntityState<T>) -> Unit = { _, _ -> }

    override val isTransform = false

    fun andThen(action: (entity: T, PoseableEntityState<T>) -> Unit) = this.also {
        it.afterAction = action
    }

    override fun run(
        entity: T?,
        model: PoseableEntityModel<T>,
        state: PoseableEntityState<T>,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ): Boolean {
        if (startedSeconds == -1F) {
            startedSeconds = state.animationSeconds
        }

        return animation.run(model, state, state.animationSeconds - startedSeconds, intensity).also {
            if (!it && entity != null) {
                afterAction(entity, state)
            }
        }
    }

    override fun applyEffects(entity: T, state: PoseableEntityState<T>, previousSeconds: Float, newSeconds: Float) {
        val previousSecondsOffset = previousSeconds - startedSeconds
        val currentSecondsOffset = newSeconds - startedSeconds
        animation.applyEffects(entity, state, previousSecondsOffset, currentSecondsOffset)
    }
}