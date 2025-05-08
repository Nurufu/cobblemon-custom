package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetRidePokemonExhaustPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class SetRidePokemonExhaustHandler : ServerNetworkPacketHandler<SetRidePokemonExhaustPacket> {

    override fun handle(packet: SetRidePokemonExhaustPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val pokemon = player.serverWorld.getEntityById(packet.pokemonID)
        if (pokemon is PokemonEntity) {
            pokemon.isExhausted = packet.bl
        }
    }
}