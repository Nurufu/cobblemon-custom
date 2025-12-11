/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.def

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * A list of dex entries
 */
class PokedexDef: ClientDataSynchronizer<PokedexDef> {
    val id = cobblemonResource("blank")
    val entries = mutableListOf<Identifier>()

    // zero clue if this will work
    fun getListings(): List<PokedexEntry> {
        return entries as List<PokedexEntry>
    }

    override fun shouldSynchronize(other: PokedexDef) = true

    override fun decode(buffer: PacketByteBuf) {
        val size = buffer.readInt()
        for (i in 0 until size) {
            entries.add(buffer.readIdentifier())
        }
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(entries.size)
        entries.forEach {
            buffer.writeIdentifier(it)
        }
    }


}