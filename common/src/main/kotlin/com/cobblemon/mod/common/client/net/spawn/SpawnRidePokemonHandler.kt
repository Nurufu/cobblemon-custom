package com.cobblemon.mod.common.client.net.spawn

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import net.minecraft.client.MinecraftClient
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnRidePokemonPacket

class SpawnRidePokemonHandler : ClientNetworkPacketHandler<SpawnRidePokemonPacket> {
    override fun handle(packet: SpawnRidePokemonPacket, client: MinecraftClient) {
        packet.spawnRidePokemonAndApply(client)
    }
}