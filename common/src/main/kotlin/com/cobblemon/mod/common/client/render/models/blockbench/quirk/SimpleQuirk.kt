/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.quirk

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.util.math.random
import net.minecraft.entity.Entity

class SimpleQuirk<T : Entity>(
    private val secondsBetweenOccurrences: Pair<Float, Float>,
    val condition: (state: PosableState<T>) -> Boolean = { true },
    val loopTimes: IntRange = 1..1,
    val animations: (state: PosableState<T>) -> Iterable<StatefulAnimation<T, *>>
) : ModelQuirk<T, SimpleQuirkData<T>>() {
    override fun createData(): SimpleQuirkData<T> = SimpleQuirkData()
    override fun tick(state: PosableState<T>, data: SimpleQuirkData<T>) {
        if (data.animations.isNotEmpty() || data.primaryAnimation != null) {
            return
        }

        if (!condition(state)) {
            return
        }

        if (data.remainingLoops > 0) {
            applyAnimations(state, data)
            data.remainingLoops--
        }

        if (data.remainingLoops == 0) {
            if (data.nextOccurrenceSeconds > 0F) {
                // Is it time?
                if (data.nextOccurrenceSeconds <= state.animationSeconds) {
                    data.remainingLoops = loopTimes.random() - 1
                    applyAnimations(state, data)
                    data.nextOccurrenceSeconds = -1F
                }
            } else {
                data.nextOccurrenceSeconds = state.animationSeconds + secondsBetweenOccurrences.random()
            }
        }
    }

    private fun applyAnimations(state: PosableState<T>, data: SimpleQuirkData<T>) {
        val (primary, stateful) = animations(state).partition { it is PrimaryAnimation }
        data.animations.addAll(stateful)
        if (primary.isNotEmpty()) {
            val primaryAnimation = primary.first() as PrimaryAnimation<T>
            data.primaryAnimation = primaryAnimation
            state.addPrimaryAnimation(primaryAnimation)
        }
    }
}