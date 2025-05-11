/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pokemon.sync

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.sync.UpdatePokemonBehaviourPacket
import net.minecraft.client.MinecraftClient

class UpdatePokemonBehaviourHandler : ClientNetworkPacketHandler<UpdatePokemonBehaviourPacket> {

    override fun handle(packet: UpdatePokemonBehaviourPacket, client: MinecraftClient) {
        val entity = client.world?.getEntityById(packet.pokemonID)
        if (entity is PokemonEntity) {
            entity.moveBehaviour = packet.behaviour
        }
    }
}