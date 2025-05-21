/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.loot

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTables
import net.minecraft.loot.entry.LootTableEntry
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus

/**
 * Used to append our loot table injections to existing ones.
 * This is not meant for API use and is only public due to visibility requirement in the platform implementations.
 *
 * @since June 8th, 2023
 */
@ApiStatus.Internal
object LootInjector {

    private const val PREFIX = "injection/"

    private val VILLAGE_HOUSE = cobblemonResource("injection/chests/village_house")

    private val villageHouseLootTables = hashSetOf(
        LootTables.VILLAGE_DESERT_HOUSE_CHEST,
        LootTables.VILLAGE_PLAINS_CHEST,
        LootTables.VILLAGE_SAVANNA_HOUSE_CHEST,
        LootTables.VILLAGE_SNOWY_HOUSE_CHEST,
        LootTables.VILLAGE_TAIGA_HOUSE_CHEST,
    )

    private val injections = hashSetOf(
        LootTables.ABANDONED_MINESHAFT_CHEST,
        LootTables.ANCIENT_CITY_CHEST,
        LootTables.BASTION_BRIDGE_CHEST,
        LootTables.BASTION_HOGLIN_STABLE_CHEST,
        LootTables.BASTION_OTHER_CHEST,
        LootTables.BASTION_TREASURE_CHEST,
        LootTables.END_CITY_TREASURE_CHEST,
        LootTables.IGLOO_CHEST_CHEST,
        LootTables.JUNGLE_TEMPLE_CHEST,
        LootTables.NETHER_BRIDGE_CHEST,
        LootTables.PILLAGER_OUTPOST_CHEST,
        LootTables.SHIPWRECK_SUPPLY_CHEST,
        LootTables.SIMPLE_DUNGEON_CHEST,
        LootTables.SPAWN_BONUS_CHEST,
        LootTables.STRONGHOLD_CORRIDOR_CHEST,
        LootTables.WOODLAND_MANSION_CHEST,
        LootTables.FISHING_TREASURE_GAMEPLAY,
        LootTables.FISHING_JUNK_GAMEPLAY
    ).apply { addAll(villageHouseLootTables) }

    /**
     * Attempts to inject a Cobblemon injection loot table to a loot table being loaded.
     * This will automatically query the existence of an injection.
     *
     * @param id The [Identifier] of the loot table being loaded.
     * @param provider The job invoked if the injection is possible, this is what the platform needs to do to append the loot table.
     * @return If the injection was made.
     */
    fun attemptInjection(id: Identifier, provider: (LootPool.Builder) -> Unit): Boolean {
        if (!this.injections.contains(id)) {
            return false
        }
        val resulting = this.convertToPotentialInjected(id)
        Cobblemon.LOGGER.debug("{}: Injected {} to {}", this::class.simpleName, resulting, id)
        provider(this.injectLootPool(resulting))
        return true
    }

    /**
     * Takes a source ID and converts it into the target injection.
     *
     * @param source The [Identifier] of the base loot table.
     * @return The [Identifier] for the expected Cobblemon injection.
     */
    private fun convertToPotentialInjected(source: Identifier): Identifier {
        if (this.villageHouseLootTables.contains(source)) {
            return VILLAGE_HOUSE
        }
        return cobblemonResource("$PREFIX${source.path}")
    }

    /**
     * Creates a loot pool builder with our injection.
     *
     * @param resulting The [Identifier] for our injection table.
     * @return A [LootPool.Builder] with the [resulting] table.
     */
    private fun injectLootPool(resulting: Identifier): LootPool.Builder {
        return LootPool.builder()
            .with(LootTableEntry.builder(resulting).weight(1))
            .bonusRolls(UniformLootNumberProvider.create(0F, 1F))
    }

}