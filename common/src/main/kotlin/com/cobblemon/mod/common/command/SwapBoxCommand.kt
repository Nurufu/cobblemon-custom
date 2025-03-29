/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.SwapPCBoxEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object SwapBoxCommand {
    private val BOX_DOES_NOT_EXIST = { boxNo: Int -> commandLang("pokebox.box_does_not_exist", boxNo) }
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("swapbox")
            .permission(CobblemonPermissions.SWAP_BOX)
            .then(CommandManager.argument("box", IntegerArgumentType.integer(1))
                .then(CommandManager.argument("box2", IntegerArgumentType.integer(1))
                    .executes{ context ->
                        val player = context.source.playerOrThrow
                        val box = IntegerArgumentType.getInteger(context, "box")
                        val box2 = IntegerArgumentType.getInteger(context, "box2")
                        execute(player, box, box2)
                    })))
    }

    private fun execute(
        player: ServerPlayerEntity,
        box: Int,
        box2: Int
    ) : Int {
        val playerPc = player.pc()
        val box0 = playerPc.boxes[box-1]
        val box1 = playerPc.boxes[box2-1]
        val boxT = PCStore(player.uuid)
        val s = box0.name.toString()
        val s2 = box1.name.toString()
        val w = box0.wallpaper.toString()
        val w2 = box1.wallpaper.toString()
        var i = 0

        while(i<30) {
            CobblemonEvents.SWAP_PC_BOX_EVENT_PRE.postThen(
                event = SwapPCBoxEvent.Pre(player, box - 1, box2 - 1),
                ifSucceeded = {
                    playerPc.swap(PCPosition(box0.boxNumber, i), PCPosition(box1.boxNumber, i))
                    if(i<1)
                    {
                        player.sendMessage(lang("box.swapped", box0.boxNumber + 1, box1.boxNumber + 1))
                        box0.name = s2
                        RenamePCBoxPacket(playerPc.uuid, box0.boxNumber, s2).sendToPlayer(player)
                        box1.name = s
                        RenamePCBoxPacket(playerPc.uuid, box1.boxNumber, s).sendToPlayer(player)
                        box0.wallpaper = Identifier(w2)
                        ChangePCBoxWallpaperPacket(playerPc.uuid, box0.boxNumber, Identifier(w2)).sendToPlayer(player)
                        box1.wallpaper = Identifier(w)
                        ChangePCBoxWallpaperPacket(playerPc.uuid, box1.boxNumber, Identifier(w)).sendToPlayer(player)
                    }
                },
                ifCanceled = {
                    throw SimpleCommandExceptionType(
                        BOX_DOES_NOT_EXIST(box0.boxNumber).red()
                    ).create()
                }
            )
            i++
        }
        //Set Names

        //Set Wallpapers

        return Command.SINGLE_SUCCESS
    }
}