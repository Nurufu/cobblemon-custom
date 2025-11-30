/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.storage

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokedex.PokedexEntry
import net.minecraft.util.Identifier
import java.util.UUID

class ClientPokedex(var uuid: UUID) {
    var pokedexEntries: HashMap<Identifier, PokedexEntry> = HashMap<Identifier, PokedexEntry>()

    init {
        default()
    }

    fun default(){
        for (species in PokemonSpecies.implemented) {
            var id = species.resourceIdentifier
            pokedexEntries[id] = PokedexEntry(id)
        }
    }

    fun update(pokedexEntry: PokedexEntry){
        pokedexEntries[pokedexEntry.id] = pokedexEntry
    }
}