/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.util.Identifier

class PokedexEntry(var id: Identifier, var progressMap: MutableMap<String, Progress> = HashMap()) {
    var species: Species? = PokemonSpecies.getByIdentifier(id)

    init {
        if(species == null){
            LOGGER.warn("Species {} is null, this Pokedex Entry may not work properly. Check if Pokemon was registered.", id)
        }
    }

    fun setProgress(formString: String, newProgress : Progress){
        progressMap[formString] = newProgress
    }

    fun getProgress(formString: String): Progress = progressMap.getOrDefault(formString, Progress.NONE)

    companion object {
        fun formToFormString(form: FormData, shiny: Boolean): String = if (shiny) form.name + "_shiny" else form.name
    }
}