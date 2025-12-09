package com.cobblemon.mod.common.api.events.pokedex.scanning

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.network.ServerPlayerEntity

data class PokemonScannedEvent(val player: ServerPlayerEntity, val scannedEntity: PokemonEntity) {
    val pokemon: Pokemon
        get() = scannedEntity.pokemon
}