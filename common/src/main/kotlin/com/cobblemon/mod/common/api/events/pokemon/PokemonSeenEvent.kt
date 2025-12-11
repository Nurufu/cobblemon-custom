package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.*

data class PokemonSeenEvent(
    val playerId: UUID,
    val pokemon: Pokemon
) {
}
