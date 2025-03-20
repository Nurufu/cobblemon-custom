/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.animation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Packet sent to arbitrarily play the first animation of the given set that exists on the given entity. MoLang
 * expressions are also sent to setup any MoLang variables that are needed for the animation.
 *
 * @author Hiroku
 * @since October 21st, 2023
 */
class PlayPoseableAnimationPacket(
    val entityId: Int,
    val animation: Set<String>,
    val expressions: Set<String>
) : NetworkPacket<PlayPoseableAnimationPacket> {
    override val id: Identifier = ID

    companion object {
        val ID = cobblemonResource("play_poseable_animation")
        fun decode(buffer: PacketByteBuf) = PlayPoseableAnimationPacket(
            buffer.readInt(),
            buffer.readList { buffer.readString() }.toSet(),
            buffer.readList { buffer.readString() }.toSet()
        )
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeCollection(animation) { pb, value -> pb.writeString(value) }
        buffer.writeCollection(expressions) { pb, value -> pb.writeString(value) }
    }
}