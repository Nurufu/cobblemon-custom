/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import java.util.*

/**
 * Used to populate the player interaction menu
 *
 *
 * @author Apion
 * @since November 5th, 2023
 */
class PlayerInteractOptionsPacket(
    val options: EnumMap<Options, OptionStatus>,
    val targetId: UUID,
    val numericTargetId: Int,
    val selectedPokemonId: UUID
    ) : NetworkPacket<PlayerInteractOptionsPacket> {
    companion object {
        val ID = cobblemonResource("player_interactions")
        fun decode(buffer: PacketByteBuf) = PlayerInteractOptionsPacket(
            readOptionsMap(buffer),
            buffer.readUuid(),
            buffer.readInt(),
            buffer.readUuid()
        )

        private fun readOptionsMap(buffer: PacketByteBuf) : EnumMap<Options, OptionStatus> {
            val size = buffer.readInt()
            val options : EnumMap<Options, OptionStatus> = EnumMap<Options, OptionStatus>(Options::class.java)
            repeat(size) {
                val key = buffer.readEnumConstant(Options::class.java)
                val value = buffer.readEnumConstant(OptionStatus::class.java)
                options[key] = value
            }
            return options
        }
    }

    override val id = ID
    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(options.size)
        for((key,value) in options) {
            buffer.writeEnumConstant(key)
            buffer.writeEnumConstant(value)
        }
        buffer.writeUuid(targetId)
        buffer.writeInt(numericTargetId)
        buffer.writeUuid(selectedPokemonId)
    }

    enum class Options {
        BATTLE,
        SPECTATE_BATTLE,
        TRADE
    }

    enum class OptionStatus {
        AVAILABLE,
        TOO_FAR,
        INSUFFICIENT_POKEMON,
        OTHER
    }

}