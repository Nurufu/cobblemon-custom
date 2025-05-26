/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import com.cobblemon.mod.common.api.text.red
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
import net.minecraft.text.Text

object ForcedAspectFixCommand {
    private val CANNOT_EMPTY = { box0: Int, box1: Int -> commandLang("pokebox.cannot_empty", box0, box1) }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>){
        dispatcher.register(
            CommandManager.literal("emptyboxes")
                .permission(CobblemonPermissions.EMPTY_BOXES)
                        .executes { context ->
                            val player = context.source.playerOrThrow
                            execute(player)
                        })
    }

    private fun execute(
        player: ServerPlayerEntity
    ) : Int {
        val playerPc = player.pc()

        playerPc.boxes.forEach { box ->
            for (i in 0 until POKEMON_PER_BOX) {
                box[i]?.forcedAspects?.forEach {
                    //if(it == "male") box[i].forcedAspects.
                }
            }
        }

        return Command.SINGLE_SUCCESS
    }
}