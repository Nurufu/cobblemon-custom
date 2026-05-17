/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.Items

const val MEGAX = "mega-x"
val megaListX = listOf("charizard", "raichu", "mewtwo")

object MegaXFeatureHandler {
    fun updateMega(pokemon: Pokemon) {
        if(!megaListX.contains(pokemon.species.name.lowercase())) return
        if (pokemon.getFeature<FlagSpeciesFeature>(MEGAX) == null) pokemon.features.add(FlagSpeciesFeature(
            MEGAX,
            false
        ))
        val feature = pokemon.getFeature<FlagSpeciesFeature>(MEGAX) ?: return
        //reset if holding air or memory doesn't match current type
        if(pokemon.heldItem().isOf(Items.AIR) || !compareMega(pokemon)) {
            feature.enabled = false
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
            if (pokemon.heldItem().isOf(CobblemonItems.MEGA_STONE_X)) feature.enabled = true
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
    }

    fun compareMega(pokemon: Pokemon): Boolean{
        val feature = pokemon.getFeature<FlagSpeciesFeature>(MEGAX) ?: return false
        return pokemon.heldItem().isOf(CobblemonItems.MEGA_STONE_X) && feature.enabled
    }
}