/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetRidePokemonExhaustPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class SetRidePokemonExhaustHandler : ServerNetworkPacketHandler<SetRidePokemonExhaustPacket> {

    override fun handle(packet: SetRidePokemonExhaustPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val pokemon = player.serverWorld.getEntityById(packet.pokemonID)
        if (pokemon is PokemonEntity) {
            pokemon.isExhausted = packet.bl
        }
    }
}