/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.core.config.builder.api.Component

object MassEmptyBoxCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>){
        dispatcher.register(
            CommandManager.literal("emptyboxes")
                .permission(CobblemonPermissions.EMPTY_BOXES)
                .then(CommandManager.argument("box1", IntegerArgumentType.integer(1))
                    .then(CommandManager.argument("box2", IntegerArgumentType.integer(1))
                        .executes { context ->
                            val player = context.source.playerOrThrow
                            val box = IntegerArgumentType.getInteger(context, "box1")
                            val box2 = IntegerArgumentType.getInteger(context, "box2")
                            execute(player, box, box2)
                        })))
    }

    private fun execute(
        player: ServerPlayerEntity,
        box1: Int,
        box2: Int
    ) : Int {
        val playerPc = player.pc()
        val box = playerPc.boxes[box1-1]
        val boxx = playerPc.boxes[box2-1]

        for(i in box.boxNumber..boxx.boxNumber){
            for(x in 0..29){
                playerPc.remove(PCPosition(i, x))
            }
        }
        player.sendMessage(Text.literal("Boxes ${box1}-${box2} have been emptied."))

        return Command.SINGLE_SUCCESS
    }
}