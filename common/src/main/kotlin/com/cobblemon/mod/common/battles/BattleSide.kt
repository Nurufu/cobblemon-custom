/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.interpreter.ContextManager
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.text.Text

/**
 * Unlike the Showdown side.ts, this can represent multiple actors.
 *
 * @author Hiroku
 * @since March 9th, 2022
 */
class BattleSide(vararg val actors: BattleActor) {
    val activePokemon: List<ActiveBattlePokemon>
        get() = actors.flatMap { it.activePokemon }

    lateinit var battle: PokemonBattle
    val contextManager = ContextManager()
    fun getOppositeSide() = if (this == battle.side1) battle.side2 else battle.side1

    fun broadcastChatMessage(component: Text) {
        return this.actors.forEach { it.sendMessage(component) }
    }

    fun stillSendingOut() = actors.any { it.stillSendingOutCount > 0 }

    fun playCries() {
        activePokemon.forEach {
            val entity = it.battlePokemon?.entity ?: return@forEach
            entity.cry()
            if(entity.pokemon.shiny && entity.pokemon.isWild()){
                SpawnSnowstormEntityParticlePacket(cobblemonResource("shiny_ring"),entity.id, listOf("shiny_particles","middle"))
                    .sendToPlayersAround(entity.x, entity.y, entity.z, 64.0, entity.world.registryKey)
            }
        }
    }
}