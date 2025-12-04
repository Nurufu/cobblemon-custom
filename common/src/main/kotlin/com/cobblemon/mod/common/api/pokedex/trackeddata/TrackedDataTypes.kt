/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.trackeddata

import com.mojang.serialization.Codec
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

//When serializing and deserializing we need to know what string goes to what codec and
//What class goes to what string
object TrackedDataTypes {
    val classToVariant = mutableMapOf<KClass<*>, Identifier>()
    val variantToCodec = mutableMapOf<Identifier, Codec<*>>()
    val variantToDecoder = mutableMapOf<Identifier, (PacketByteBuf) -> GlobalTrackedData>()
    init {
        register(CountTypeCaughtGlobalTrackedData.ID, CountTypeCaughtGlobalTrackedData::class, CountTypeCaughtGlobalTrackedData.CODEC, CountTypeCaughtGlobalTrackedData::decode)
    }

    fun register(variant: Identifier, classObj: KClass<*>, codec: Codec<*>, decoder: (PacketByteBuf) -> GlobalTrackedData) {
        classToVariant[classObj] = variant
        variantToCodec[variant] = codec
        variantToDecoder[variant] = decoder
    }
}