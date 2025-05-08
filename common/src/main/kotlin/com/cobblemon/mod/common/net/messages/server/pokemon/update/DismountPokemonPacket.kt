package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import com.cobblemon.mod.common.util.rideableResource

class DismountPokemonPacket(
    val slot: Int
) : NetworkPacket<DismountPokemonPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(slot)
    }

    companion object {
        val ID = cobblemonResource("dismount_pokemon")
        fun decode(buffer: PacketByteBuf) = DismountPokemonPacket(
            buffer.readInt()
        )
    }
}