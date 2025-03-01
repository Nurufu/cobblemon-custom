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
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

class SetPCBoxWallpapersPacket internal constructor(val wallpapers: List<Identifier>) : NetworkPacket<SetPCBoxWallpapersPacket>, UnsplittablePacket {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeSizedInt(IntSize.INT, wallpapers.size)
        for (wallpaper in wallpapers) {
            buffer.writeString(wallpaper.toString())
        }
    }

    companion object {
        val ID = cobblemonResource("set_pc_box_wallpapers")
        fun decode(buffer: PacketByteBuf): SetPCBoxWallpapersPacket {
            val wallpapers = mutableListOf<Identifier>()
            val size = buffer.readSizedInt(IntSize.INT)
            repeat(size) {
                wallpapers.add(Identifier.tryParse(buffer.readString()))
            }
            return SetPCBoxWallpapersPacket(wallpapers)
        }
    }
}
