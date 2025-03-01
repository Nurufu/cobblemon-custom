/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.RenamePCBoxEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object RenameBoxCommand {
    private val BOX_DOES_NOT_EXIST = { boxNo: Int -> commandLang("pokebox.box_does_not_exist", boxNo) }
    private val CANNOT_RENAME_BOX = { name: String -> commandLang("renamebox.cannot_rename_box", name) }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("renamebox")
            .permission(CobblemonPermissions.RENAMEBOX)
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .then(CommandManager.argument("box", IntegerArgumentType.integer(1))
                    .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .executes { context ->
                            val player = context.player()
                            val box = IntegerArgumentType.getInteger(context, "box")
                            val name = StringArgumentType.getString(context, "name")
                            execute(player, box, name)
                        }
                    )
                )
            ))
    }

    private fun execute(
        player: ServerPlayerEntity,
        box: Int,
        name: String
    ): Int {
        val playerPc = player.pc()
        if (playerPc.boxes.size < box) {
            throw SimpleCommandExceptionType(BOX_DOES_NOT_EXIST(box).red()).create()
        }

        val pcBox = playerPc.boxes[box - 1]
        CobblemonEvents.RENAME_PC_BOX_EVENT_PRE.postThen(
            event = RenamePCBoxEvent.Pre(player, pcBox, name),
            ifSucceeded = {
                pcBox.name = it.name
                CobblemonEvents.RENAME_PC_BOX_EVENT_POST.post(RenamePCBoxEvent.Post(player, pcBox, it.name))
                RenamePCBoxPacket(playerPc.uuid, pcBox.boxNumber, it.name).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_RENAME_BOX(name).red()).create()
            }
        )

        return Command.SINGLE_SUCCESS
    }
}
