/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

object CobblemonModelPredicateRegistry {

    fun registerPredicates() {

        val rods = listOf(
            CobblemonItems.AZURE_ROD,
            CobblemonItems.BEAST_ROD,
            CobblemonItems.CHERISH_ROD,
            CobblemonItems.CITRINE_ROD,
            CobblemonItems.DIVE_ROD,
            CobblemonItems.DREAM_ROD,
            CobblemonItems.DUSK_ROD,
            CobblemonItems.FAST_ROD,
            CobblemonItems.FRIEND_ROD,
            CobblemonItems.GREAT_ROD,
            CobblemonItems.HEAL_ROD,
            CobblemonItems.HEAVY_ROD,
            CobblemonItems.LEVEL_ROD,
            CobblemonItems.LOVE_ROD,
            CobblemonItems.LURE_ROD,
            CobblemonItems.LUXURY_ROD,
            CobblemonItems.MASTER_ROD,
            CobblemonItems.MOON_ROD,
            CobblemonItems.NEST_ROD,
            CobblemonItems.NET_ROD,
            CobblemonItems.PARK_ROD,
            CobblemonItems.POKE_ROD,
            CobblemonItems.PREMIER_ROD,
            CobblemonItems.QUICK_ROD,
            CobblemonItems.REPEAT_ROD,
            CobblemonItems.ROSEATE_ROD,
            CobblemonItems.SAFARI_ROD,
            CobblemonItems.SLATE_ROD,
            CobblemonItems.SPORT_ROD,
            CobblemonItems.TIMER_ROD,
            CobblemonItems.ULTRA_ROD,
            CobblemonItems.VERDANT_ROD,
            CobblemonItems.ANCIENT_AZURE_ROD,
            CobblemonItems.ANCIENT_CITRINE_ROD,
            CobblemonItems.ANCIENT_FEATHER_ROD,
            CobblemonItems.ANCIENT_GIGATON_ROD,
            CobblemonItems.ANCIENT_GREAT_ROD,
            CobblemonItems.ANCIENT_HEAVY_ROD,
            CobblemonItems.ANCIENT_IVORY_ROD,
            CobblemonItems.ANCIENT_JET_ROD,
            CobblemonItems.ANCIENT_LEADEN_ROD,
            CobblemonItems.ANCIENT_ORIGIN_ROD,
            CobblemonItems.ANCIENT_POKE_ROD,
            CobblemonItems.ANCIENT_ROSEATE_ROD,
            CobblemonItems.ANCIENT_SLATE_ROD,
            CobblemonItems.ANCIENT_ULTRA_ROD,
            CobblemonItems.ANCIENT_VERDANT_ROD,
            CobblemonItems.ANCIENT_WING_ROD
        )

        rods.forEach { rod ->
            ModelPredicateProviderRegistry.register(rod, Identifier("cast")) { stack, world, entity, seed ->
                if (entity !is PlayerEntity || entity.fishHook !is PokeRodFishingBobberEntity) return@register 0.0f

                val rodId = entity.fishHook!!.dataTracker.get(PokeRodFishingBobberEntity.POKEROD_ID)

                val isMainHand = stack == entity.mainHandStack
                var isOffHand = stack == entity.offHandStack

                var mainHandItem = entity.mainHandStack.item
                val isFishingWithMainHand = mainHandItem is PokerodItem && rodId == mainHandItem.pokeRodId.toString()

                if (isFishingWithMainHand) {
                        isOffHand = false
                    }

                if (isMainHand && isFishingWithMainHand || isOffHand) 1.0f else 0.0f
            }
            }
        }



    }