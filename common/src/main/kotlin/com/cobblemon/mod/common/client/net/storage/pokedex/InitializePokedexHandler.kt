/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pokedex

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.storage.pc.InitializePokedexPacket
import net.minecraft.client.MinecraftClient

class InitializePokedexHandler : ClientNetworkPacketHandler<InitializePokedexPacket> {
    override fun handle(packet: InitializePokedexPacket, client: MinecraftClient) {
        CobblemonClient.storage.createPokedex(packet.isThisPlayerPokedex, packet.uuid)
    }
}