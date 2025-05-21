/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import java.util.Optional
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.util.Uuids

object CobblemonMemories {
    val memories = mutableMapOf<String, MemoryModuleType<*>>()

    val BATTLING_POKEMON = register("battling_pokemon", ListCodec(Uuids.INT_STREAM_CODEC))
    val NPC_BATTLING = register("npc_battling", PrimitiveCodec.BOOL)

    fun <U> register(id: String, codec: Codec<U>): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType(Optional.of(codec))
        memories[id] = memoryModule
        return memoryModule
    }

    fun <U> register(id: String): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType<U>(Optional.empty())
        memories[id] = memoryModule
        return memoryModule
    }
}