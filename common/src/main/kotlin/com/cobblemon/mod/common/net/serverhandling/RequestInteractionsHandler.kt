/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.EnumMap
import java.util.EnumSet

object RequestInteractionsHandler : ServerNetworkPacketHandler<RequestPlayerInteractionsPacket> {
    override fun handle(
        packet: RequestPlayerInteractionsPacket,
        server: MinecraftServer,
        player: ServerPlayerEntity
    ) {
        //We could potentially check if the targeted player has pokemon here
        val options : EnumMap<PlayerInteractOptionsPacket.Options, PlayerInteractOptionsPacket.OptionStatus> = EnumMap<PlayerInteractOptionsPacket.Options, PlayerInteractOptionsPacket.OptionStatus>(PlayerInteractOptionsPacket.Options::class.java)

        val isTargetBattling = BattleRegistry.getBattleByParticipatingPlayerId(packet.targetId) != null
        if (isTargetBattling and Cobblemon.config.allowSpectating) {
            options[PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE] = PlayerInteractOptionsPacket.OptionStatus.AVAILABLE
        }
        else {
            options[PlayerInteractOptionsPacket.Options.BATTLE] = PlayerInteractOptionsPacket.OptionStatus.AVAILABLE
        }
        PlayerInteractOptionsPacket(options, packet.targetId, packet.targetNumericId, packet.pokemonId).sendToPlayer(player)

    }

}