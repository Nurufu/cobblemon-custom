/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.PokedexEntryDTO
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class PokedexUIPacket internal constructor(val pokedexEntriesDTO: List<PokedexEntryDTO>):
    NetworkPacket<PokedexUIPacket> {

    override val id = ID


    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokedexEntriesDTO.count())
        for(pokedexEntryDTO in pokedexEntriesDTO) pokedexEntryDTO.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("pokedex_ui")
        fun decode(buffer: PacketByteBuf): PokedexUIPacket {
            val entryCount = buffer.readInt()
            var pokedexDTOEntries: MutableList<PokedexEntryDTO> = mutableListOf<PokedexEntryDTO>()
            for(i in 0..entryCount){
                pokedexDTOEntries.add(PokedexEntryDTO.decode(buffer))
            }

            return PokedexUIPacket(pokedexDTOEntries)
        }
    }
}