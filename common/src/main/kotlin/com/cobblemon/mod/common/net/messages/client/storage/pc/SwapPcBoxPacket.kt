package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.events.storage.SwapPCBoxEvent
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf
import java.util.*

open class SwapPcBoxPacket internal constructor(val storeID: UUID, val box1: Int, val box2: Int) : NetworkPacket<SwapPcBoxPacket> {

    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, box1)
        buffer.writeSizedInt(IntSize.U_SHORT, box2)
    }

    companion object{
        val ID = cobblemonResource("change_pc_box_wallpaper2")
        fun decode(buffer: PacketByteBuf) : SwapPcBoxPacket {
            val storeID = buffer.readUuid()
            val box1 = buffer.readSizedInt(IntSize.U_SHORT)
            val box2 = buffer.readSizedInt(IntSize.U_SHORT)
            return SwapPcBoxPacket(storeID, box1, box2)
        }
    }
}
