/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ChangePCBoxWallpaperEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

object RequestChangePCBoxWallpaperHandler : ServerNetworkPacketHandler<RequestChangePCBoxWallpaperPacket> {
    override fun handle(packet: RequestChangePCBoxWallpaperPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        if (pc.boxes.size <= packet.boxNumber || Cobblemon.wallpapers[player.uuid]?.contains(packet.wallpaper) == false) {
            return
        }

        val box = pc.boxes[packet.boxNumber]
        val event = ChangePCBoxWallpaperEvent.Pre(player, box, packet.wallpaper)
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThenFinally(
            event = event,
            ifSucceeded = { preEvent ->
                box.wallpaper = preEvent.wallpaper
                CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(ChangePCBoxWallpaperEvent.Post(player, box, preEvent.wallpaper))
            },
            finally = {
                ChangePCBoxWallpaperPacket(pc.uuid, packet.boxNumber, box.wallpaper).sendToPlayer(player)
            }
        )
    }
}
