/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.Items

const val MEMORY = "memory"

object MemoryFeatureHandler {
    fun updateMemory(pokemon: Pokemon) {
        if(pokemon.species.name == "Silvally" && pokemon.getFeature<StringSpeciesFeature>(MEMORY) == null) pokemon.features.add(
            StringSpeciesFeature(
                MEMORY,
                "normal"
            ))
        val feature = pokemon.getFeature<StringSpeciesFeature>(MEMORY) ?: return
        //reset if holding air or memory doesn't match current type
        if(pokemon.heldItem().isOf(Items.AIR) || !compareMemory(pokemon)) {
            feature.value = "normal"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
            if (pokemon.heldItem().isOf(CobblemonItems.FIRE_MEMORY)) feature.value = "fire"
            else if (pokemon.heldItem().isOf(CobblemonItems.WATER_MEMORY)) feature.value = "water"
            else if (pokemon.heldItem().isOf(CobblemonItems.GRASS_MEMORY)) feature.value = "grass"
            else if (pokemon.heldItem().isOf(CobblemonItems.ELECTRIC_MEMORY)) feature.value = "electric"
            else if (pokemon.heldItem().isOf(CobblemonItems.ICE_MEMORY)) feature.value = "ice"
            else if (pokemon.heldItem().isOf(CobblemonItems.FIGHTING_MEMORY)) feature.value = "fighting"
            else if (pokemon.heldItem().isOf(CobblemonItems.POISON_MEMORY)) feature.value = "poison"
            else if (pokemon.heldItem().isOf(CobblemonItems.GROUND_MEMORY)) feature.value = "ground"
            else if (pokemon.heldItem().isOf(CobblemonItems.FLYING_MEMORY)) feature.value = "flying"
            else if (pokemon.heldItem().isOf(CobblemonItems.PSYCHIC_MEMORY)) feature.value = "psychic"
            else if (pokemon.heldItem().isOf(CobblemonItems.BUG_MEMORY)) feature.value = "bug"
            else if (pokemon.heldItem().isOf(CobblemonItems.ROCK_MEMORY)) feature.value = "rock"
            else if (pokemon.heldItem().isOf(CobblemonItems.GHOST_MEMORY)) feature.value = "ghost"
            else if (pokemon.heldItem().isOf(CobblemonItems.DRAGON_MEMORY)) feature.value = "dragon"
            else if (pokemon.heldItem().isOf(CobblemonItems.STEEL_MEMORY)) feature.value = "steel"
            else if (pokemon.heldItem().isOf(CobblemonItems.FAIRY_MEMORY)) feature.value = "fairy"
            else if (pokemon.heldItem().isOf(CobblemonItems.DARK_MEMORY)) feature.value = "dark"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
    }

    fun compareMemory(pokemon: Pokemon): Boolean{
        val feature = pokemon.getFeature<StringSpeciesFeature>(MEMORY) ?: return false
        if (pokemon.heldItem().isOf(CobblemonItems.FIRE_MEMORY) && feature.value == "fire") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.WATER_MEMORY) && feature.value == "water") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.GRASS_MEMORY) && feature.value == "grass") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.ELECTRIC_MEMORY) && feature.value == "electric") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.ICE_MEMORY) && feature.value == "ice") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.FIGHTING_MEMORY) && feature.value == "fighting") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.POISON_MEMORY) && feature.value == "poison") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.GROUND_MEMORY) && feature.value == "ground") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.FLYING_MEMORY) && feature.value == "flying") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.PSYCHIC_MEMORY) && feature.value == "psychic") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.BUG_MEMORY) && feature.value == "bug") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.ROCK_MEMORY) && feature.value == "rock") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.GHOST_MEMORY) && feature.value == "ghost") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.DRAGON_MEMORY) && feature.value == "dragon") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.STEEL_MEMORY) && feature.value == "steel") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.FAIRY_MEMORY) && feature.value == "fairy") return true
        else if (pokemon.heldItem().isOf(CobblemonItems.DARK_MEMORY) && feature.value == "dark") return true
        else return false
    }
}