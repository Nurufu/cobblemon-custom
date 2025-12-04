/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.adapter

import com.cobblemon.mod.common.api.pokedex.PokedexRecord
import com.google.gson.InstanceCreator
import java.lang.reflect.Type
import java.util.UUID

/**
 *
 * @author Apion
 * @since February 24, 2024
 */
//GSON sets gennedFactories to null if we dont use this
//UUID gets set later during deserialization
object PokedexInstanceCreator : InstanceCreator<PokedexRecord> {
    private val uuid = UUID.randomUUID()
    override fun createInstance(p0: Type?): PokedexRecord {
        val result = PokedexRecord(uuid)
        result.gennedFactories = mutableSetOf()
        return result
    }
}