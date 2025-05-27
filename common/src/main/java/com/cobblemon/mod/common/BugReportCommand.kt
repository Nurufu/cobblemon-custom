/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.BOLD
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.onClick
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.awt.Color.green

object BugReportCommand {

    private const val NAME = "report"

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal(NAME)
            .permission(CobblemonPermissions.BUG)
            .executes(this::execute)
        )
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.playerOrThrow

        player.sendMessage((Text.literal("Click Here to open the Bug Report / Feature Request form")).setStyle(Style.EMPTY.withClickEvent(
            ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.google.com/forms/d/e/1FAIpQLSfjWTcpQh4Et5GPNzxn36M3pIM9HU0VF2j4mKxIkKG3baF9XA/viewform?usp=sharing")
        ).withColor(TextColor.parse("green"))))
        return Command.SINGLE_SUCCESS
    }
}
