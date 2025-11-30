/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pokedex.Pokedex
import com.cobblemon.mod.common.net.messages.client.ui.PokedexUIPacket
import net.minecraft.client.MinecraftClient

object PokedexUIPacketHandler : ClientNetworkPacketHandler<PokedexUIPacket> {
    override fun handle(packet: PokedexUIPacket, client: MinecraftClient) {
        try {
            var clientPokedex = CobblemonClient.storage.myPokedex
            packet.pokedexEntriesDTO.forEach{ clientPokedex.update(it.create()) }
            Pokedex.open(clientPokedex)
        } catch (e: Exception) {
            Cobblemon.LOGGER.debug("Failed to open the pokedex from the Pokedex UI packet handler", e)
        }
    }
}