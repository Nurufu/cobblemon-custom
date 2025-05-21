/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.criterion.*
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.advancement.criterion.Criterion

/**
 * Contains all the advancement criteria in Cobblemon.
 *
 * @author Licious
 * @since October 26th, 2022
 */
object CobblemonCriteria {
    val PICK_STARTER = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("pick_starter"),
            PickStarterCriterionCondition::class.java
        )
    )
    val CATCH_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("catch_pokemon"),
            CaughtPokemonCriterionCondition::class.java
        )
    )
    val CATCH_SHINY_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("catch_shiny_pokemon"),
            SimpleCountableCriterionCondition::class.java
        )
    )
    val EGG_HATCH = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("eggs_hatched"),
            SimpleCountableCriterionCondition::class.java
        )
    )
    val EVOLVE_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("pokemon_evolved"),
            EvolvePokemonCriterionCondition::class.java
        )
    )
    val WIN_BATTLE = this.create(
        BattleCountableCriterionTrigger(
            cobblemonResource("battles_won"),
            BattleCountableCriterionCondition::class.java
        )
    )

    val DEFEAT_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("pokemon_defeated"),
            SimpleCountableCriterionCondition::class.java
        )
    )

    val COLLECT_ASPECT = this.create(
        AspectCriterionTrigger(
            cobblemonResource("aspects_collected"),
            AspectCriterionCondition::class.java
        )
    )

    val POKEMON_INTERACT = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("pokemon_interact"),
            PokemonInteractCriterion::class.java
        )
    )

    val PARTY_CHECK = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("party"),
            PartyCheckCriterion::class.java
        )
    )

    val LEVEL_UP = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("level_up"),
            LevelUpCriterionCondition::class.java
        )
    )

    val PASTURE_USE = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("pasture_use"),
            PickStarterCriterionCondition::class.java
        )
    )

    val RESURRECT_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("resurrect_pokemon"),
            PickStarterCriterionCondition::class.java
        )
    )

    val TRADE_POKEMON = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("trade_pokemon"),
            TradePokemonCriterionCondition::class.java
        )
    )

    val CAST_POKE_ROD = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("cast_poke_rod"),
            CastPokeRodCriterionCondition::class.java
        )
    )

    // Advancement criteria for [grow_tumblestone.json]
    val PLANT_TUMBLESTONE = this.create(
        SimpleCriterionTrigger(
            cobblemonResource("plant_tumblestone"),
            PlantTumblestoneCriterionCondition::class.java
        )
    )

    private fun <T : Criterion<*>> create(criteria: T): T = Cobblemon.implementation.registerCriteria(criteria)

}