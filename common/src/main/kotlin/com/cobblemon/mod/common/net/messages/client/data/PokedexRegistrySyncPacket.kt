/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.pokedex.PokedexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class PokedexRegistrySyncPacket(pokedexEntries: Collection<PokedexEntry>, ) : DataRegistrySyncPacket<PokedexEntry, PokedexRegistrySyncPacket>(pokedexEntries) {
    override val id = ID

    override fun encodeEntry(buffer: PacketByteBuf, entry: PokedexEntry) {
        TODO("Not yet implemented")
    }

    override fun decodeEntry(buffer: PacketByteBuf): PokedexEntry? {
        TODO("Not yet implemented")
    }

    override fun synchronizeDecoded(entries: Collection<PokedexEntry>) {
        TODO("Not yet implemented")
    }

    companion object {
        val ID = cobblemonResource("pokedex_sync")
        fun decode(buffer: PacketByteBuf): SpeciesRegistrySyncPacket = TODO("Not yet implemented")
    }
}