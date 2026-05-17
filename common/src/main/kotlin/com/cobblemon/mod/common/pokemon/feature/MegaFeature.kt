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

const val MEGA = "mega"
val megaList = listOf("venusaur", "blastoise", "beedrill", "pidgeot", "clefable", "alakazam", "victreebel", "slowbro", "gengar", "kangaskhan", "starmie", "pinsir", "gyrados", "aerodactyl", "dragonite", "meganium", "feraligatr", "ampharos", "steelix", "scizor", "heracross", "skarmory", "houndoom", "tyranitar", "sceptile", "blaziken", "swampert", "gardevoir", "sableye", "mawile", "aggron", "medicham", "manectric", "sharpedo", "camerupt", "altaria", "banette", "chimecho", "absol", "glalie", "salamence", "metagross", "latias", "latios", "staraptor", "lopunny", "garchomp", "lucario", "abomasnow", "gallade", "froslass", "emboar", "audino", "scolipede", "scrafty", "eelektross", "chandelure", "chesnaught", "delphox", "greninja", "pyroar", "malamar", "dragalge", "hawlucha", "diancie", "falinks", "tatsugiri")

object MegaFeatureHandler {
    fun updateMega(pokemon: Pokemon) {
        if(!megaList.contains(pokemon.species.name.lowercase())) return
        if (pokemon.getFeature<FlagSpeciesFeature>(MEGA) == null) pokemon.features.add(FlagSpeciesFeature(
            MEGA,
            false
        ))
        val feature = pokemon.getFeature<FlagSpeciesFeature>(MEGA) ?: return
        //reset if holding air or memory doesn't match current type
        if(pokemon.heldItem().isOf(Items.AIR) || !compareMega(pokemon)) {
            feature.enabled = false
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
            if (pokemon.heldItem().isOf(CobblemonItems.MEGA_STONE)) feature.enabled = true
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
    }

    fun compareMega(pokemon: Pokemon): Boolean{
        val feature = pokemon.getFeature<FlagSpeciesFeature>(MEGA) ?: return false
        return pokemon.heldItem().isOf(CobblemonItems.MEGA_STONE) && feature.enabled
    }
}