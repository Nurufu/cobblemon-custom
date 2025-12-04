/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.adapter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.trackeddata.CountTypeCaughtGlobalTrackedData
import com.cobblemon.mod.common.api.pokedex.trackeddata.GlobalTrackedData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.lang.reflect.Type
import kotlin.reflect.KClass

object GlobalTrackedDataAdapter : JsonSerializer<GlobalTrackedData>, JsonDeserializer<GlobalTrackedData> {

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): GlobalTrackedData {
        val result = JsonOps.INSTANCE.withDecoder(GlobalTrackedData.CODEC).apply(jElement)
        return result.get().left().get().first
    }

    override fun serialize(data: GlobalTrackedData, type: Type, context: JsonSerializationContext): JsonElement {
        val result = JsonOps.INSTANCE.withEncoder(GlobalTrackedData.CODEC).apply(data)
        return result.get().left().get()
    }
}