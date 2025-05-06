package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import com.cobblemon.mod.common.net.messages.server.pokemon.update.DismountPokemonPacket

class DismountPokemonHandler : ServerNetworkPacketHandler<DismountPokemonPacket> {

    override fun handle(packet: DismountPokemonPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val slot = packet.slot.takeIf { it >= 0 } ?: return
        val party = Cobblemon.storage.getParty(player)
        val pokemon = party.get(slot) ?: return
        if (pokemon.entity != null && pokemon.entity!!.hasPassenger(player)) {
            player.stopRiding()
        }
    }
}