package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class StartScanningPacket(val targetedId: Int) : NetworkPacket<StartScanningPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(targetedId)
    }

    companion object {
        val ID = cobblemonResource("start_scanning_packet")

        fun decode(buffer: PacketByteBuf): StartScanningPacket {
            val targetId = buffer.readInt()
            return StartScanningPacket(targetId)
        }
    }
}