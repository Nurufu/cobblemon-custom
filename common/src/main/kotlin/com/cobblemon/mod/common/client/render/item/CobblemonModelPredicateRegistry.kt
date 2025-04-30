/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.interactive.PokerodItem
import net.minecraft.client.item.ClampedModelPredicateProvider
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
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
            CobblemonItems.VERDANT_ROD
        )

        rods.forEach { rod ->
            ModelPredicateProviderRegistry.register(rod, Identifier("cast"), ClampedModelPredicateProvider { stack, world, entity, seed ->
                if (entity == null) {
                    0.0f
                } else {
                    val isMainHand = entity.mainHandStack == stack
                    var isOffHand = entity.offHandStack == stack
                    if (entity.mainHandStack.item is PokerodItem) {
                        isOffHand = false
                    }

                    if ((isMainHand || isOffHand) && entity is PlayerEntity && entity.fishHook != null) 1.0f else 0.0f
                }
            })
        }



    }
}