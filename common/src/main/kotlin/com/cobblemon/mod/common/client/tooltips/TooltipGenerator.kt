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