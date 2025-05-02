/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */



package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.item.interactive.PokerodItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object FishingRodTooltipGenerator : TooltipGenerator() {
    override fun generateTooltip(stack: ItemStack, lines: MutableList<Text>): MutableList<Text>? {
        val resultLines = mutableListOf<Text>()

        val rod = (stack.item as? PokerodItem)?.pokeRodId?.let { PokeRods.getPokeRod(it) } ?: return null
        val ball = PokeBalls.getPokeBall(rod.pokeBallId) ?: return null

        // Add ball desc
        ball.item.name.let {
            val bobberDescription = Text.translatable(
                "cobblemon.pokerod.bobber",
                it.copy().gray()
            )
            resultLines.add(bobberDescription)
        }

        PokerodItem.getBait(stack).item.name.let {
            if(PokerodItem.getBait(stack).count > 0) {
                val baitDesc = Text.translatable(
                    "cobblemon.pokerod.bait",
                    it.copy().gray(),
                    PokerodItem.getBait(stack).count
                )
                resultLines.add(baitDesc)
            }
        }

        // grey text for context for players on how to apply/remove bait to/from rod
        val greyText = if (PokerodItem.getBait(stack).count > 0) {
            Text.translatable("cobblemon.pokerod.remove").gray()
        } else {
            Text.translatable("cobblemon.pokerod.apply").gray()
        }
        resultLines.add(greyText)

        return resultLines

    }
}