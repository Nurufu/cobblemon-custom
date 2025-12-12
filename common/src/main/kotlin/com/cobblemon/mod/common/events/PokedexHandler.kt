package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonGainedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.getPlayer

object PokedexHandler : EventHandler {
    override fun registerListeners() {
        CobblemonEvents.POKEMON_GAINED.subscribe(Priority.NORMAL, ::onPokemonGained)
        CobblemonEvents.POKEMON_SEEN.subscribe(Priority.NORMAL, ::onPokemonSeen)
    }

    fun onPokemonGained(event: PokemonGainedEvent) {
        Cobblemon.playerDataManager.getPokedexData(event.playerId).catch(event.pokemon)
    }

    fun onPokemonSeen(event: PokemonSeenEvent) {
        Cobblemon.playerDataManager.getPokedexData(event.playerId).encounter(event.pokemon)
    }
}