package net.starliteheart.cobbleride.common.client.net.pokemon.sync

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import net.minecraft.client.MinecraftClient
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.sync.UpdatePokemonBehaviourPacket

class UpdatePokemonBehaviourHandler : ClientNetworkPacketHandler<UpdatePokemonBehaviourPacket> {

    override fun handle(packet: UpdatePokemonBehaviourPacket, client: MinecraftClient) {
        val entity = client.world?.getEntityById(packet.pokemonID)
        if (entity is RideablePokemonEntity) {
            entity.moveBehaviour = packet.behaviour
        }
    }
}