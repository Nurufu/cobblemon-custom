/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex

import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.util.Identifier

class DexPokemonData(
    val speciesId : Identifier = cobblemonResource("bulbasaur"),
    val forms : MutableList<PokedexForm> = mutableListOf(),
    val dexNum: String
) {

}