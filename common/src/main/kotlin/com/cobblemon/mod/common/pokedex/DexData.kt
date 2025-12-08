/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.pokedex.scanner.DexPokemonData
import net.minecraft.util.Identifier

class DexData (
    var identifier : Identifier,
    var pokemonList : MutableList<DexPokemonData> = mutableListOf(),
    var overrideCategories : Boolean = false
    var entryIds: List<Identifier>
): ClientDataSynchronizer<DexData> {

}