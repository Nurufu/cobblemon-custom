/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scripting

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.ScriptRegistrySyncPacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import java.io.File
import java.util.concurrent.ExecutionException
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object CobblemonScripts : DataRegistry {
    const val MOLANG_EXTENSION = ".molang"
    override val id = cobblemonResource("molang")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonScripts>()

    val clientScripts = mutableMapOf<Identifier, ExpressionLike>()
    val scripts = mutableMapOf<Identifier, ExpressionLike>()

    override fun reload(manager: ResourceManager) {
        manager.findResources("molang") { path -> path.endsWith(MOLANG_EXTENSION) }.forEach { (identifier, resource) ->
            resource.inputStream.use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    try {
                        val expression = reader.readText().asExpressionLike()
                        if (identifier.path.startsWith("molang/client/")) {
                            clientScripts[resolvedIdentifier] = expression
                        } else {
                            scripts[resolvedIdentifier] = expression
                        }
                    } catch (exception: Exception) {
                        throw ExecutionException("Error loading MoLang script: $identifier", exception)
                    }
                }
            }
        }

        Cobblemon.LOGGER.info("Loaded ${scripts.size} server scripts and ${clientScripts.size} client scripts")
        observable.emit(this)
    }


    override fun sync(player: ServerPlayerEntity) {
        player.sendPacket(ScriptRegistrySyncPacket(clientScripts.entries))
    }

    fun run(identifier: Identifier, runtime: MoLangRuntime): MoValue? {
        return scripts[identifier]?.resolve(runtime)
    }
}