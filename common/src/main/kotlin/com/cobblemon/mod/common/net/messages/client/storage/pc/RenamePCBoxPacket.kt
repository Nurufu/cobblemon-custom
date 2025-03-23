/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.PacketByteBuf
import java.util.*

open class RenamePCBoxPacket internal constructor(val storeID: UUID, val boxNumber: Int, val name: String?) : NetworkPacket<RenamePCBoxPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, boxNumber)
        buffer.writeString(name ?: "")
    }

    companion object {
        val ID = cobblemonResource("rename_pc_box")
        fun decode(buffer: PacketByteBuf): RenamePCBoxPacket {
            val storeID = buffer.readUuid()
            val boxNumber = buffer.readSizedInt(IntSize.U_SHORT)
            val name = buffer.readString()
            return RenamePCBoxPacket(storeID, boxNumber, name)
        }
    }
}
