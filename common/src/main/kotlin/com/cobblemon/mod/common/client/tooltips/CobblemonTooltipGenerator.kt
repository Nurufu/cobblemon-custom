/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */



package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.item.ItemStack
import net.minecraft.util.Language
import net.minecraft.text.Text

object CobblemonTooltipGenerator : TooltipGenerator() {
    @Suppress("DEPRECATION")
    override fun generateTooltip(stack: ItemStack, lines: MutableList<Text>): MutableList<Text>? {
        val resultLines = mutableListOf<Text>()

        if (stack.item.registryEntry.registryKey() != null && stack.item.registryEntry.registryKey().value.namespace == Cobblemon.MODID) {
            val language = Language.getInstance()
            val key = this.baseLangKeyForItem(stack)
            if (language.hasTranslation(key)) {
                resultLines.add(key.asTranslated().gray())
            }
            var i = 1
            var listKey = "${key}_$i"
            while(language.hasTranslation(listKey)) {
                resultLines.add(listKey.asTranslated().gray())
                listKey = "${key}_${++i}"
            }
        }

        return resultLines
    }

    private fun baseLangKeyForItem(stack: ItemStack): String {
        if (stack.item is PokeBallItem) {
            val asPokeball = stack.item as PokeBallItem
            return "item.${asPokeball.pokeBall.name.namespace}.${asPokeball.pokeBall.name.path}.tooltip"
        }
        return "${stack.translationKey}.tooltip"
    }
}