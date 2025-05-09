/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ChangePCBoxWallpaperEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object MassChangeBoxWallpaperCommand {
    private val WALLPAPER_DOES_NOT_EXIST = { wallpaper: String -> commandLang("changewallpaper.wallpaper_does_not_exist", wallpaper) }
    val CANNOT_CHANGE_WALLPAPER = { name: String -> commandLang("changewallpaper.cannot_change_wallpaper", name) }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("changeallwallpaper")
            .permission(CobblemonPermissions.CHANGE_WALLPAPER_PLAYER)
                    .then(CommandManager.argument("wallpaper", StringArgumentType.greedyString())
                        .suggests { context, builder ->
                            Cobblemon.wallpapers[context.source.playerOrThrow.uuid]?.forEach {
                                builder.suggest(it.toString())
                            }
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val player = context.source.playerOrThrow
                            val wallpaper = Identifier.tryParse(StringArgumentType.getString(context, "wallpaper"))
                            execute(player, wallpaper)
                        }
                    )
                )
    }

    private fun execute(
        player: ServerPlayerEntity,
        wallpaper: Identifier?
    ): Int {
        val playerPc = player.pc()

        if (wallpaper == null || Cobblemon.wallpapers[player.uuid]?.contains(wallpaper) == false) {
            throw SimpleCommandExceptionType(WALLPAPER_DOES_NOT_EXIST(wallpaper.toString()).red()).create()
        }

        for ((i, boxes) in playerPc.boxes.withIndex()) {
            val pcBox = playerPc.boxes[i]
            CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThen(
                event = ChangePCBoxWallpaperEvent.Pre(player, pcBox, wallpaper),
                ifSucceeded = {
                    boxes.wallpaper = it.wallpaper
                    CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(
                        ChangePCBoxWallpaperEvent.Post(
                            player,
                            playerPc.boxes[i],
                            it.wallpaper
                        )
                    )
                    ChangePCBoxWallpaperPacket(playerPc.uuid, pcBox.boxNumber, it.wallpaper).sendToPlayer(player)
                },
                ifCanceled = {
                    throw SimpleCommandExceptionType(CANNOT_CHANGE_WALLPAPER(wallpaper.toString()).red()).create()
                }
            )
        }

        return Command.SINGLE_SUCCESS
    }
}
