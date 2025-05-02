package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.client.MinecraftClient
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

        val client = MinecraftClient.getInstance()
        val itemRegistry = client.world?.itemRegistry
        itemRegistry.let { registry ->
            if (registry != null) {
                FishingBaits.getFromItemStack(stack)?.toItemStack(registry)?.item?.name
                    ?.let { // maybe this can be simplified to not use the FishingBaits to get the stack and just use PokerodItem to get the stack since we have it already
                        val baitDescription =
                            Text.translatable(
                                "cobblemon.pokerod.bait",
                                it.copy().gray(),
                                PokerodItem.getBait(stack).count
                            )
                        resultLines.add(baitDescription)
                    }
            }
        }


        // grey text for context for players on how to apply/remove bait to/from rod
        val greyText = if (FishingBaits.getFromItemStack(stack) != null) {
            Text.translatable("cobblemon.pokerod.remove").gray()
        } else {
            Text.translatable("cobblemon.pokerod.apply").gray()
        }
        resultLines.add(greyText)

        return resultLines

    }
}