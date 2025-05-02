/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */



package com.cobblemon.mod.common.client.tooltips

import net.minecraft.item.ItemStack
import net.minecraft.text.Text

abstract class TooltipGenerator {
    open fun generateTooltip(stack: ItemStack, lines: MutableList<Text>): MutableList<Text>? {
        return null
    }
    open fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Text>): MutableList<Text>? {
        return null
    }
}