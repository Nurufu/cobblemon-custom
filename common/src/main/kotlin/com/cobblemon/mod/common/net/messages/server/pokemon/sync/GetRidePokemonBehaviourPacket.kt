package com.cobblemon.mod.common.net.messages.server.pokemon.sync

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
import net.starliteheart.cobbleride.common.util.rideableResource

class GetRidePokemonBehaviourPacket(
    val pokemonID: Int,
) : NetworkPacket<GetRidePokemonBehaviourPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
    }

    companion object {
        val ID = rideableResource("get_ride_behaviour")
        fun decode(buffer: PacketByteBuf) = GetRidePokemonBehaviourPacket(
            buffer.readInt()
        )
    }
}