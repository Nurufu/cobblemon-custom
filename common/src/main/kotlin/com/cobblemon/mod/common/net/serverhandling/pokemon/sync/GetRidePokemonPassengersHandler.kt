package com.cobblemon.mod.common.net.serverhandling.pokemon.sync

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import net.starliteheart.cobbleride.common.net.messages.server.pokemon.sync.GetRidePokemonPassengersPacket

class GetRidePokemonPassengersHandler : ServerNetworkPacketHandler<GetRidePokemonPassengersPacket> {

    override fun handle(packet: GetRidePokemonPassengersPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val entity = player.serverWorld.getEntityById(packet.pokemonID)
        if (entity is RideablePokemonEntity && entity.hasPassengers())
            player.networkHandler.connection.send(EntityPassengersSetS2CPacket(entity))
    }
}