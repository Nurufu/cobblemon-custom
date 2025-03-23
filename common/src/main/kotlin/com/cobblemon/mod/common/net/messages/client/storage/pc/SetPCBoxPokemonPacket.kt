/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.pc.PCBox
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.net.messages.PokemonDTO
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readMapK
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeMapK
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Sets an entire box of Pokémon in the client side representation of a PC. This is used
 * during the initial sending of a PC's contents. It's better than sending hundreds of packets
 * for a full PC - this way it's one large-ish packet per box.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.pc.SetPCBoxPokemonHandler].
 *
 * @author Hiroku
 * @since June 18th, 2022
 */
class SetPCBoxPokemonPacket internal constructor(val storeID: UUID, val boxNumber: Int, val name: String, val wallpaper: Identifier, val pokemon: Map<Int, PokemonDTO>) : NetworkPacket<SetPCBoxPokemonPacket> {

    override val id = ID

    constructor(box: PCBox): this(box.pc.uuid, box.boxNumber, box.name?: "", box.wallpaper, box.getNonEmptySlots().map { it.key to PokemonDTO(it.value, toClient = true) }.toMap())

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(storeID)
        buffer.writeSizedInt(IntSize.U_BYTE, boxNumber)
        buffer.writeString(name)
        buffer.writeIdentifier(wallpaper)
        buffer.writeMapK(map = pokemon) { (slot, pokemon) ->
            buffer.writeSizedInt(IntSize.U_BYTE, slot)
            pokemon.encode(buffer)
        }
    }

    companion object {
        val ID = cobblemonResource("set_pc_box")
        fun decode(buffer: PacketByteBuf): SetPCBoxPokemonPacket {
            val storeID = buffer.readUuid()
            val boxNumber = buffer.readSizedInt(IntSize.U_BYTE)
            val name = buffer.readString()
            val wallpaper = buffer.readIdentifier()
            val pokemonMap = mutableMapOf<Int, PokemonDTO>()
            buffer.readMapK(map = pokemonMap) { buffer.readSizedInt(IntSize.U_BYTE) to PokemonDTO().also { it.decode(buffer) } }
            return SetPCBoxPokemonPacket(storeID, boxNumber, name, wallpaper, pokemonMap)
        }
    }
}