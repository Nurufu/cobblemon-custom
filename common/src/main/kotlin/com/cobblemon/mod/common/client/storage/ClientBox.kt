/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.storage

import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

class ClientBox(var name : MutableText?, var wallpaper : Identifier) : Iterable<Pokemon?> {
    constructor() : this(null, cobblemonResource("textures/gui/pc/pc_screen_overlay.png"))

    val slots = MutableList<Pokemon?>(POKEMON_PER_BOX) {null}
    override fun iterator() = slots.iterator()
}
