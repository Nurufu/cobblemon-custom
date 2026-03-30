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

const val PLATE = "plate"

object PlateFeatureHandler {
    fun updatePlate(pokemon: Pokemon) {
        if(pokemon.species.name == "Arceus" && pokemon.getFeature<StringSpeciesFeature>(PLATE) == null) pokemon.features.add(
            StringSpeciesFeature(
                PLATE,
                "normal"
            ))
        val feature = pokemon.getFeature<StringSpeciesFeature>(PLATE) ?: return
        if(pokemon.heldItem().isOf(Items.AIR)) {
            feature.value = "normal"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
        else {
            if (pokemon.heldItem().isOf(CobblemonItems.FIRE_PLATE)) feature.value = "fire"
            else if (pokemon.heldItem().isOf(CobblemonItems.WATER_PLATE)) feature.value = "water"
            else if (pokemon.heldItem().isOf(CobblemonItems.GRASS_PLATE)) feature.value = "grass"
            else if (pokemon.heldItem().isOf(CobblemonItems.ELECTRIC_PLATE)) feature.value = "electric"
            else if (pokemon.heldItem().isOf(CobblemonItems.ICE_PLATE)) feature.value = "ice"
            else if (pokemon.heldItem().isOf(CobblemonItems.FIGHTING_PLATE)) feature.value = "fighting"
            else if (pokemon.heldItem().isOf(CobblemonItems.POISON_PLATE)) feature.value = "poison"
            else if (pokemon.heldItem().isOf(CobblemonItems.GROUND_PLATE)) feature.value = "ground"
            else if (pokemon.heldItem().isOf(CobblemonItems.FLYING_PLATE)) feature.value = "flying"
            else if (pokemon.heldItem().isOf(CobblemonItems.PSYCHIC_PLATE)) feature.value = "psychic"
            else if (pokemon.heldItem().isOf(CobblemonItems.BUG_PLATE)) feature.value = "bug"
            else if (pokemon.heldItem().isOf(CobblemonItems.ROCK_PLATE)) feature.value = "rock"
            else if (pokemon.heldItem().isOf(CobblemonItems.GHOST_PLATE)) feature.value = "ghost"
            else if (pokemon.heldItem().isOf(CobblemonItems.DRAGON_PLATE)) feature.value = "dragon"
            else if (pokemon.heldItem().isOf(CobblemonItems.STEEL_PLATE)) feature.value = "steel"
            else if (pokemon.heldItem().isOf(CobblemonItems.FAIRY_PLATE)) feature.value = "fairy"
            else if (pokemon.heldItem().isOf(CobblemonItems.DARK_PLATE)) feature.value = "dark"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
    }
}