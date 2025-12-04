/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.EntityDimensions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ChangeScaleAndSize {
    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = CommandManager.literal("changescaleandsize")
            .permission(CobblemonPermissions.CHANGE_SCALE_AND_SIZE)
            .then(
                CommandManager.argument("pokemon", SpeciesArgumentType.species())
                    .then(
                        CommandManager.argument("scale", FloatArgumentType.floatArg())
                            .then(CommandManager.argument("width", FloatArgumentType.floatArg())
                                .then(CommandManager.argument("height", FloatArgumentType.floatArg()).executes(::execute))
                            )
                    )

                    .executes(::execute))
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<ServerCommandSource>) : Int {
        val pkm = SpeciesArgumentType.getPokemon(context, "pokemon")
        val scale = FloatArgumentType.getFloat(context, "scale")
        val width = FloatArgumentType.getFloat(context, "width")
        val height = FloatArgumentType.getFloat(context, "height")

        pkm.baseScale = scale
        pkm.hitbox = EntityDimensions(width, height, false)
        pkm.forms.clear()
        pkm.forms.add(FormData().also { it.initialize(pkm) })
        return Command.SINGLE_SUCCESS
    }
}