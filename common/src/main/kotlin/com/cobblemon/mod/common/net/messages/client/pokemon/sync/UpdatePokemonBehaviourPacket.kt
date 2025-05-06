package com.cobblemon.mod.common.net.messages.client.pokemon.sync

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
import com.cobblemon.mod.common.net.messages.client.pokemon.ai.ClientMoveBehaviour
import net.starliteheart.cobbleride.common.util.rideableResource

class UpdatePokemonBehaviourPacket(
    val pokemonID: Int,
    val behaviour: ClientMoveBehaviour,
) : NetworkPacket<UpdatePokemonBehaviourPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
        behaviour.encode(buffer)
    }

    companion object {
        val ID = rideableResource("update_ride_behaviour")
        fun decode(buffer: PacketByteBuf) = UpdatePokemonBehaviourPacket(
            buffer.readInt(),
            ClientMoveBehaviour.decode(buffer),
        )
    }
}