package net.starliteheart.cobbleride.common.net.messages.server.pokemon.sync

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
import net.starliteheart.cobbleride.common.util.rideableResource
import java.util.UUID

class GetRidePokemonPassengersPacket(
    val pokemonID: Int,
) : NetworkPacket<GetRidePokemonPassengersPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
    }

    companion object {
        val ID = rideableResource("get_ride_passengers")
        fun decode(buffer: PacketByteBuf) = GetRidePokemonPassengersPacket(
            buffer.readInt()
        )
    }
}