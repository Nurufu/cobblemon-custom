/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import java.util.*

class InitializePokedexPacket(
    /** Whether this should be set as the player's pokedex for rendering immediately. */
    var isThisPlayerPokedex: Boolean, uuid: UUID
) : NetworkPacket<InitializePokedexPacket> {
    /** The UUID of the pokedex storage. Does not need to be the player's UUID. */
    var uuid = uuid

    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(isThisPlayerPokedex)
        buffer.writeUuid(uuid)
    }


    companion object {
        val ID = cobblemonResource("initialize_pokedex")
        fun decode(buffer: PacketByteBuf) = InitializePokedexPacket(buffer.readBoolean(), buffer.readUuid())
    }
}