/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Util

object WebDexCommand {

    private const val NAME = "webdex"

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal(NAME)
                .permission(CobblemonPermissions.DEX)
                .executes(this::execute)
        )
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.playerOrThrow

        player.sendMessage(Text.literal("Opening the WebDex"))

        Util.getOperatingSystem().open("https://nurufu.github.io/")
        return Command.SINGLE_SUCCESS
    }
}
