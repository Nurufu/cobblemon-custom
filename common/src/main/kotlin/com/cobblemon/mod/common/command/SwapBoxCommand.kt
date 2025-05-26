/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ChangePCBoxWallpaperEvent
import com.cobblemon.mod.common.api.events.storage.RenamePCBoxEvent
import com.cobblemon.mod.common.api.events.storage.SwapPCBoxEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.ChangeBoxWallpaperPlayerCommand.CANNOT_CHANGE_WALLPAPER
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object SwapBoxCommand {
    private val BOX_DOES_NOT_EXIST = { boxNo: Int -> commandLang("pokebox.box_does_not_exist", boxNo) }
    private val CANNOT_RENAME_BOX = { name: String -> commandLang("renamebox.cannot_rename_box", name) }
    private val CANNOT_SWAP = { box0: Int, box1: Int -> commandLang("pokebox.cannot_swap", box0, box1)}
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
        val s = box0.name.toString()
        val s2 = box1.name.toString()
        val w = box0.wallpaper
        val w2 = box1.wallpaper
        var i = 0

        if(box0.boxNumber > box1.boxNumber) throw SimpleCommandExceptionType(CANNOT_SWAP(box0.boxNumber+1, box1.boxNumber+1).red()).create()

        while(i<30) {
            CobblemonEvents.SWAP_PC_BOX_EVENT_PRE.postThen(
                event = SwapPCBoxEvent.Pre(player, box - 1, box2 - 1),
                ifSucceeded = {
                    playerPc.swap(PCPosition(box0.boxNumber, i), PCPosition(box1.boxNumber, i))
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
        CobblemonEvents.RENAME_PC_BOX_EVENT_PRE.postThen(
            event = RenamePCBoxEvent.Pre(player, box0, s2),
            ifSucceeded = {
                box0.name = s2
                CobblemonEvents.RENAME_PC_BOX_EVENT_POST.post(RenamePCBoxEvent.Post(player, box0, s2))
                RenamePCBoxPacket(playerPc.uuid, box0.boxNumber, s2).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_RENAME_BOX(s2).red()).create()
            }
        )
        CobblemonEvents.RENAME_PC_BOX_EVENT_PRE.postThen(
            event = RenamePCBoxEvent.Pre(player, box1, s),
            ifSucceeded = {
                box1.name = s
                CobblemonEvents.RENAME_PC_BOX_EVENT_POST.post(RenamePCBoxEvent.Post(player, box1, s))
                RenamePCBoxPacket(playerPc.uuid, box1.boxNumber, s).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_RENAME_BOX(s).red()).create()
            }
        )
        //Set Wallpapers
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThen(
            event = ChangePCBoxWallpaperEvent.Pre(player, box0, w2),
            ifSucceeded = {
                box0.wallpaper = w2
                CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(ChangePCBoxWallpaperEvent.Post(player, box0, w2))
                ChangePCBoxWallpaperPacket(playerPc.uuid, box0.boxNumber, w2).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_CHANGE_WALLPAPER(w2.toString()).red()).create()
            }
        )
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThen(
            event = ChangePCBoxWallpaperEvent.Pre(player, box1, w),
            ifSucceeded = {
                box0.wallpaper = w
                CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(ChangePCBoxWallpaperEvent.Post(player, box1, w))
                ChangePCBoxWallpaperPacket(playerPc.uuid, box1.boxNumber, w).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_CHANGE_WALLPAPER(w.toString()).red()).create()
            }
        )
        return Command.SINGLE_SUCCESS
    }
}