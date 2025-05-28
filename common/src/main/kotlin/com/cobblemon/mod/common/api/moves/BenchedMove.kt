/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves

import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
class BenchedMoves : Iterable<BenchedMove> {
    val observable = SimpleObservable<BenchedMoves>()
    private var emit = true
    private val benchedMoves = mutableListOf<BenchedMove>()

    fun doWithoutEmitting(action: () -> Unit) {
        val previousEmit = emit
        emit = false
        action()
        emit = previousEmit
    }

    fun copyFrom(other: BenchedMoves) {
        doWithoutEmitting {
            clear()
            other.forEach { add(it) }
        }
        update()
    }

    fun doThenEmit(action: () -> Unit) {
        doWithoutEmitting(action)
        update()
    }

    fun update() {
        if (emit) {
            observable.emit(this)
        }
    }

    fun add(benchedMove: BenchedMove) = doThenEmit { benchedMoves.add(benchedMove) }
    fun addAll(benchedMoves: Iterable<BenchedMove>) = doThenEmit { this.benchedMoves.addAll(benchedMoves) }
    fun clear() = doThenEmit { benchedMoves.clear() }
    fun remove(benchedMove: BenchedMove) = doThenEmit { benchedMoves.remove(benchedMove) }
    fun remove(moveTemplate: MoveTemplate) = doThenEmit { benchedMoves.removeIf { it.moveTemplate == moveTemplate } }
    override fun iterator() = benchedMoves.iterator()

    fun saveToNBT(nbt: NbtList): NbtList {
        nbt.addAll(benchedMoves.map { it.saveToNBT(NbtCompound()) })
        return nbt
    }

    fun saveToJSON(json: JsonArray): JsonArray {
        val jsons = benchedMoves.map { it.saveToJSON(JsonObject()) }
        jsons.forEach { json.add(it) }
        return json
    }

    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeShort(benchedMoves.size)
        benchedMoves.forEach { it.saveToBuffer(buffer) }
    }

    fun loadFromNBT(nbt: NbtList): BenchedMoves {
        doThenEmit {
            clear()
            nbt.forEach { benchedMoves.add(BenchedMove.loadFromNBT(it as NbtCompound)) }
        }

        return this
    }

    fun loadFromJSON(json: JsonArray): BenchedMoves {
        doThenEmit {
            clear()
            json.forEach { benchedMoves.add(BenchedMove.loadFromJSON(it.asJsonObject)) }
        }
        return this
    }

    fun loadFromBuffer(buffer: PacketByteBuf): BenchedMoves {
        doThenEmit {
            clear()
            repeat(times = buffer.readShort().toInt()) {
                benchedMoves.add(BenchedMove.loadFromBuffer(buffer))
            }
        }
        return this
    }
}

data class BenchedMove(val moveTemplate: MoveTemplate, val ppRaisedStages: Int) {
    fun saveToNBT(nbt: NbtCompound): NbtCompound {
        nbt.putString(DataKeys.POKEMON_MOVESET_MOVENAME, moveTemplate.name)
        nbt.putByte(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES, ppRaisedStages.toByte())
        return nbt
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_MOVESET_MOVENAME, moveTemplate.name)
        json.addProperty(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES, ppRaisedStages)
        return json
    }

    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeString(moveTemplate.name)
        buffer.writeSizedInt(IntSize.U_BYTE, ppRaisedStages)
    }

    companion object {
        fun loadFromNBT(nbt: NbtCompound): BenchedMove {
            val name = nbt.getString(DataKeys.POKEMON_MOVESET_MOVENAME)
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                nbt.getByte(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES).toInt()
            )
        }

        fun loadFromJSON(json: JsonObject): BenchedMove {
            val name = json.get(DataKeys.POKEMON_MOVESET_MOVENAME).asString
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                json.get(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES).asInt
            )
        }

        fun loadFromBuffer(buffer: PacketByteBuf): BenchedMove {
            val name = buffer.readString()
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                buffer.readSizedInt(IntSize.U_BYTE)
            )
        }
    }
}