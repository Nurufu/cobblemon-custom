/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

object OpenStarterScreenCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("openstarterscreen")
                .permission(CobblemonPermissions.OPEN_STARTER_SCREEN)
                .then(
                    argument("player", EntityArgumentType.player())
                        .executes { execute(it,) }
                )
        )
    }

    private fun execute(context: CommandContext<ServerCommandSource>) : Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        if (playerData.starterSelected) {
            context.source.sendFeedback({ lang("ui.starter.hasalreadychosen", player.name).red() }, true)
            return 0
        }
        if (playerData.starterLocked) {
            playerData.starterLocked = false
            playerData.sendToPlayer(player)
        }
        playerData.starterPrompted = true
        Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreType.GENERAL)
        player.sendPacket(OpenStarterUIPacket(Cobblemon.starterHandler.getStarterList(player)))
        return SINGLE_SUCCESS
    }

}