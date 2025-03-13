/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.controller

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.network.PacketByteBuf

class ClientEvolutionController(override val pokemon: Pokemon) : EvolutionController<EvolutionDisplay> {

    private val evolutions = hashSetOf<EvolutionDisplay>()

    init {
        if (evolutions.isNotEmpty()){
            MinecraftClient.getInstance().player?.sendMessage("cobblemon.ui.evolve.hint".asTranslated(pokemon.getDisplayName()).green())
            MinecraftClient.getInstance().player?.playSound(CobblemonSounds.CAN_EVOLVE, 1F, 1F)
        }
    }

    override val size: Int
        get() = this.evolutions.size

    override fun start(evolution: EvolutionDisplay) {
        CobblemonNetwork.sendPacketToServer(AcceptEvolutionPacket(this.pokemon, evolution))
    }

    override fun progress(): Collection<EvolutionProgress<*>> {
        // Nothing is done on the client
        return emptyList()
    }

    override fun <P : EvolutionProgress<*>> trackProgress(progress: P): P {
        // Nothing is done on the client
        return progress
    }

    override fun <P : EvolutionProgress<*>> progressFirstOrCreate(predicate: (progress: EvolutionProgress<*>) -> Boolean, progressFactory: () -> P): P {
        // Nothing is done on the client
        return progressFactory()
    }

    override fun saveToNBT(): NbtElement {
        return NbtCompound()
    }

    override fun loadFromNBT(nbt: NbtElement) {
        // Nothing is done on the client
    }

    override fun saveToJson(): JsonElement {
        return JsonArray()
    }

    override fun loadFromJson(json: JsonElement) {
        // Nothing is done on the client
    }

    override fun saveToBuffer(buffer: PacketByteBuf, toClient: Boolean) {
        // Nothing is done on the client
    }

    override fun loadFromBuffer(buffer: PacketByteBuf) {
        buffer.readList(AddEvolutionPacket::decodeDisplay).forEach { this.add(it) }
    }

    override fun add(element: EvolutionDisplay) = this.evolutions.add(element)

    override fun addAll(elements: Collection<EvolutionDisplay>) = this.evolutions.addAll(elements)

    override fun clear() {
        this.evolutions.clear()
    }

    override fun iterator() = this.evolutions.iterator()

    override fun remove(element: EvolutionDisplay) = this.evolutions.remove(element)

    override fun removeAll(elements: Collection<EvolutionDisplay>) = this.evolutions.removeAll(elements.toSet())

    override fun retainAll(elements: Collection<EvolutionDisplay>) = this.evolutions.retainAll(elements.toSet())

    override fun contains(element: EvolutionDisplay) = this.evolutions.contains(element)

    override fun containsAll(elements: Collection<EvolutionDisplay>) = this.evolutions.containsAll(elements)

    override fun isEmpty() = this.evolutions.isEmpty()

}