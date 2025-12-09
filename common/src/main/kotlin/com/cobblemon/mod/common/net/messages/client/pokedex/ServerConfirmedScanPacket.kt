package com.cobblemon.mod.common.net.messages.client.pokedex

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

class ServerConfirmedScanPacket(
    val prevKnowledge: PokedexEntryProgress,
    val newKnowledge: PokedexEntryProgress,
    val species: Identifier
): NetworkPacket<ServerConfirmedScanPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeEnumConstant(prevKnowledge)
        buffer.writeEnumConstant(newKnowledge)
        buffer.writeIdentifier(species)
    }

    companion object {
        fun decode(buffer: PacketByteBuf) = ServerConfirmedScanPacket(buffer.readEnumConstant(PokedexEntryProgress::class.java), buffer.readEnumConstant(PokedexEntryProgress::class.java), buffer.readIdentifier())

        val ID = cobblemonResource("server_confirmed_scan")
    }
}