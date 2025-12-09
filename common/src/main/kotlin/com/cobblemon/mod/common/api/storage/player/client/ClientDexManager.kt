package com.cobblemon.mod.common.api.storage.player.client

import com.cobblemon.mod.common.api.pokedex.AbstractDexManager
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import net.minecraft.network.PacketByteBuf

class ClientDexManager(
    override val entries: MutableMap<String, String>,
    override val isIncrement: Boolean = false
) : AbstractDexManager(), ClientInstancedPlayerData {
    override fun encode(buf: PacketByteBuf) {
        buf.writeInt(entries.size)
        for (entry in entries) {
            buf.writeString(entry.key)
            buf.writeString(entry.value)
        }
    }

    companion object {
        fun decode(buf: PacketByteBuf): SetClientPlayerDataPacket {
            val map = mutableMapOf<String, String>()
            val numEntries = buf.readInt()
            for (i in 0 until numEntries) {
                val key = buf.readString()
                val value = buf.readString()
                map[key] = value
            }
            return SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, ClientDexManager(map, false))
        }

        fun runAction(data: ClientInstancedPlayerData) {
            if (data !is ClientDexManager) return
            CobblemonClient.clientPokedexData = data
        }
    }
}