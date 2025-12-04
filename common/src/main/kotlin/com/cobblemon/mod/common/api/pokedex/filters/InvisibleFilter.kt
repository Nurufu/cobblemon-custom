/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.filters

import com.cobblemon.mod.common.api.pokedex.ClientPokedex
import com.cobblemon.mod.common.api.pokedex.EntryFilter
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.pokedex.DexPokemonData

class InvisibleFilter(val clientPokedex: ClientPokedex) : EntryFilter(clientPokedex) {
    override fun filter(dexPokemonData: DexPokemonData): Boolean {
        return clientPokedex.discoveryLevel(dexPokemonData.identifier) >= PokedexEntryProgress.CAUGHT || !dexPokemonData.invisibleUntilFound
    }
}