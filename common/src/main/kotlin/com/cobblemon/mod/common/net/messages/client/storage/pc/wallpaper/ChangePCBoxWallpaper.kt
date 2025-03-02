/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.util.*

open class ChangePCBoxWallpaperPacket internal constructor(val storeID: UUID, val boxNumber: Int, val wallpaper: Identifier) : NetworkPacket<ChangePCBoxWallpaperPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, boxNumber)
        buffer.writeIdentifier(wallpaper)
    }

    companion object {
        val ID = cobblemonResource("change_pc_box_wallpaper")
        fun decode(buffer: PacketByteBuf): ChangePCBoxWallpaperPacket {
            val storeID = buffer.readUuid()
            val boxNumber = buffer.readSizedInt(IntSize.U_SHORT)
            val wallpaper = buffer.readIdentifier()
            return ChangePCBoxWallpaperPacket(storeID, boxNumber, wallpaper)
        }
    }
}
