package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import com.cobblemon.mod.common.util.rideableResource

class SetRidePokemonExhaustPacket(
    val pokemonID: Int,
    val bl: Boolean
) : NetworkPacket<SetRidePokemonExhaustPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
        buffer.writeBoolean(bl)
    }

    companion object {
        val ID = cobblemonResource("set_ride_state")
        fun decode(buffer: PacketByteBuf) = SetRidePokemonExhaustPacket(
            buffer.readInt(), buffer.readBoolean()
        )
    }
}