/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.distanceTo
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.toBlockPos
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.DimensionArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.entity.Entity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

object BedrockParticleCommand {
    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = dispatcher.register(CommandManager.literal("bedrockparticle")
            .permission(CobblemonPermissions.BEDROCK_PARTICLE)
            .then(
                CommandManager.argument("effect", IdentifierArgumentType.identifier())
                    .then(
                        CommandManager.argument("target", EntityArgumentType.entities())
                            .executes {
                                val effectId = IdentifierArgumentType.getIdentifier(it, "effect")
                                val entities = EntityArgumentType.getEntities(it, "target")
                                return@executes entities.sumOf { entity -> execute(it.source, effectId, entity.world as ServerWorld, entity.pos) }
                            }
                            .then(
                                CommandManager.argument("locator", StringArgumentType.word())
                                    .executes {
                                        val effectId = IdentifierArgumentType.getIdentifier(it, "effect")
                                        val entities = EntityArgumentType.getEntities(it, "target")
                                        val locator = StringArgumentType.getString(it, "locator")

                                        return@executes entities.sumOf { entity -> execute(it.source, effectId, entity.world as ServerWorld, entity, listOf(locator)) }
                                    }
                            )
                    )
                    .then(
                        CommandManager.argument("world", DimensionArgumentType.dimension())
                            .then(
                                CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                    .executes {
                                        val effectId = IdentifierArgumentType.getIdentifier(it, "effect")
                                        val world = DimensionArgumentType.getDimensionArgument(it, "world")
                                        val pos = Vec3ArgumentType.getVec3(it, "pos")
                                        return@executes execute(it.source, effectId, world as ServerWorld, pos)
                                    }
                            )
                    )
                )
            )
        dispatcher.register(command.alias("bedrockparticle"))
    }

    private fun execute(source: ServerCommandSource, effectId: Identifier, world: ServerWorld, target: Vec3d): Int {
        val pos = target.toBlockPos()
        val nearbyPlayers = world.getPlayers { it.distanceTo(pos) < 1000 }
        nearbyPlayers.forEach { player -> player.sendPacket(SpawnSnowstormParticlePacket(effectId, target)) }
        return Command.SINGLE_SUCCESS
    }

    private fun execute(source: ServerCommandSource, effectId: Identifier, world: ServerWorld, target: Entity, locator: List<String>): Int {
        val pos = target.blockPos
        val nearbyPlayers = world.getPlayers { it.distanceTo(pos) < 1000 }
        nearbyPlayers.forEach { player -> player.sendPacket(SpawnSnowstormEntityParticlePacket(effectId, target.id, locator)) }
        return Command.SINGLE_SUCCESS
    }

}