/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */



package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.util.lang
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object TooltipManager {
    private val tooltipGenerators = mutableListOf<TooltipGenerator>()
    private val seeMoreInfo by lazy { lang("see_more_info").yellow() }

    fun registerTooltipGenerator(generator: TooltipGenerator) {
        tooltipGenerators.add(generator)
    }

    fun generateTooltips(stack: ItemStack, lines: MutableList<Text>, hasShiftDown: Boolean) {
        val standardLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Text>()
            val regularTooltip = it.generateTooltip(stack, lines)
            if (regularTooltip?.isNotEmpty() == true) {
                innerLines.addAll(regularTooltip)
            }
            return@flatMap innerLines
        }

        val additionalLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Text>()
            val additionalTooltip = it.generateAdditionalTooltip(stack, lines)
            if (additionalTooltip?.isNotEmpty() == true) {
                if (hasShiftDown) {
                    innerLines.addAll(additionalTooltip)
                } else {
                    innerLines.add(seeMoreInfo)
                }
            }
            return@flatMap innerLines
        }

        if (standardLines.isNotEmpty()) {
            lines.addAll(standardLines)
        }

        if (additionalLines.isNotEmpty()) {
            lines.addAll(additionalLines)
        }
    }
}