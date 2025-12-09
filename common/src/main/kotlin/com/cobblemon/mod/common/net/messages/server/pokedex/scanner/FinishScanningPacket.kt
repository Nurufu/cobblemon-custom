package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class FinishScanningPacket(val targetedId: Int) : NetworkPacket<FinishScanningPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(targetedId)
    }

    companion object {
        val ID = cobblemonResource("finish_scanning_packet")

        fun decode(buffer: PacketByteBuf): FinishScanningPacket {
            val targetId = buffer.readInt()
            return FinishScanningPacket(targetId)
        }
    }
}