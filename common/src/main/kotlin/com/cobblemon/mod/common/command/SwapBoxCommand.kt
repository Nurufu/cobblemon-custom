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
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

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
                        execute(context, player, box, box2)
                    })))
    }

    private fun execute(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
        box: Int,
        box2: Int
    ) : Int {
        val playerPc = player.pc()
        val box0 = playerPc.boxes[box-1]
        val box1 = playerPc.boxes[box2-1]
        val boxT = playerPc.boxes[box-1]

        CobblemonEvents.SWAP_PC_BOX_EVENT_PRE.postThen(
            event = SwapPCBoxEvent.Pre(player, box-1, box2-1),
            ifSucceeded = {
                playerPc.boxes[box-1] = box1
                playerPc.boxes[box2-1] = boxT
                CobblemonEvents.SWAP_PC_BOX_EVENT_POST.post(SwapPCBoxEvent.Post(player, box-1, box2-1))
                SetPCBoxPokemonPacket(box0).sendToPlayer(player)
                SetPCBoxPokemonPacket(box1).sendToPlayer(player)
                player.sendMessage(lang("box.swapped", box0.boxNumber+1, box1.boxNumber+1))
            },
            ifCanceled ={
                throw SimpleCommandExceptionType(
                    SwapBoxCommand.BOX_DOES_NOT_EXIST(box0.boxNumber).red()).create()
            }
        )

        return box0.boxNumber
    }
}