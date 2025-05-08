package com.cobblemon.mod.common.net.messages.server.pokemon.sync

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import com.cobblemon.mod.common.util.rideableResource

class GetRidePokemonPassengersPacket(
    val pokemonID: Int,
) : NetworkPacket<GetRidePokemonPassengersPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
    }

    companion object {
        val ID = cobblemonResource("get_ride_passengers")
        fun decode(buffer: PacketByteBuf) = GetRidePokemonPassengersPacket(
            buffer.readInt()
        )
    }
}