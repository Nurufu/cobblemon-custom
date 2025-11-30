/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.pokedex.PokedexEntry
import com.cobblemon.mod.common.pokedex.Progress
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

class PokedexEntryDTO : Encodable {
    var id : String = "none"
    var progressMap: MutableMap<String, Progress> = HashMap()

    constructor(pokedexEntry: PokedexEntry){
        this.id = pokedexEntry.id.path
        this.progressMap = pokedexEntry.progressMap.filter { it.value != Progress.NONE} as MutableMap<String, Progress>
    }

    override fun encode(buffer: PacketByteBuf) {
        val entryCount = progressMap.count()
        buffer.writeShort(entryCount)
        buffer.writeString(id)
        for((key, value) in progressMap.entries){
            buffer.writeString(key)
            if(value == Progress.ENCOUNTERED){
                buffer.writeBoolean(false)
            } else {
                buffer.writeBoolean(true)
            }
        }
    }

    fun create(): PokedexEntry {
        return PokedexEntry(Identifier(id), progressMap)
    }

    companion object {
        fun decode(buffer: PacketByteBuf): PokedexEntryDTO {
            val entryCount = buffer.readShort()
            val id = buffer.readString()
            val progressMap: MutableMap<String, Progress> = HashMap()

            for(i in 0..entryCount){
                val key = buffer.readString()
                val entryAsBoolean = buffer.readBoolean()

                if(!entryAsBoolean){
                    progressMap[key] = Progress.ENCOUNTERED
                } else {
                    progressMap[key] = Progress.CAUGHT
                }
            }

            return PokedexEntryDTO(PokedexEntry(Identifier(id), progressMap))
        }

    }
}