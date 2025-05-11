/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.sync

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.ai.ClientMoveBehaviour
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class UpdatePokemonBehaviourPacket(
    val pokemonID: Int,
    val behaviour: ClientMoveBehaviour,
) : NetworkPacket<UpdatePokemonBehaviourPacket> {
    override val id = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(pokemonID)
        behaviour.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("update_ride_behaviour")
        fun decode(buffer: PacketByteBuf) = UpdatePokemonBehaviourPacket(
            buffer.readInt(),
            ClientMoveBehaviour.decode(buffer),
        )
    }
}