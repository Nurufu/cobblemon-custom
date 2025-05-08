package com.cobblemon.mod.common.client.net.pokemon.sync

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.sync.UpdatePokemonBehaviourPacket
import net.minecraft.client.MinecraftClient

class UpdatePokemonBehaviourHandler : ClientNetworkPacketHandler<UpdatePokemonBehaviourPacket> {

    override fun handle(packet: UpdatePokemonBehaviourPacket, client: MinecraftClient) {
        val entity = client.world?.getEntityById(packet.pokemonID)
        if (entity is RideablePokemonEntity) {
            entity.moveBehaviour = packet.behaviour
        }
    }
}