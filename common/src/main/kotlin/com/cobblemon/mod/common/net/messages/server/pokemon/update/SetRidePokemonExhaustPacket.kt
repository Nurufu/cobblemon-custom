package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
import net.starliteheart.cobbleride.common.util.rideableResource

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
        val ID = rideableResource("set_ride_state")
        fun decode(buffer: PacketByteBuf) = SetRidePokemonExhaustPacket(
            buffer.readInt(), buffer.readBoolean()
        )
    }
}