/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc.search

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.HashSet
import java.util.Locale
import java.util.UUID

/**
 * Client-side class containing currently applied filters, and cached results of which Pokémon pass or fail those filters.
 * Used in [com.cobblemon.mod.common.client.gui.pc.PCGUI]
 */
class Search(
    private val filters: Set<PokemonFilter>,
    private val passed: MutableSet<UUID> = HashSet(),
    private val failed: MutableSet<UUID> = HashSet()
) {
    companion object {
        val DEFAULT = Search(setOf())
        fun of(search: String): Search {
            if (search.isBlank()) return DEFAULT

            val split = search.lowercase(Locale.ROOT).trim().split(" ")
            val filters = HashSet<PokemonFilter>()

            for (piece in split) {
                var filter = piece
                val inverted = filter.startsWith("!")
                if (inverted) {
                    filter = filter.substring(1)
                }

                val pokemonFilter: PokemonFilter = when (filter) {
                    "holding" -> PokemonFilter { pokemon -> !pokemon.heldItem().isEmpty }
                    "fainted" -> PokemonFilter { pokemon -> pokemon.isFainted() }
                    "legendary" -> PokemonFilter { pokemon -> pokemon.isLegendary() }
                    "mythical" -> PokemonFilter { pokemon -> pokemon.isMythical() }
                    "ultrabeast", "ultra_beast" -> PokemonFilter { pokemon -> pokemon.isUltraBeast() }
                    "shiny" -> PokemonFilter { pokemon -> pokemon.aspects.contains("shiny")}
                    "male" -> PokemonFilter { pokemon -> pokemon.gender == Gender.MALE}
                    "female" -> PokemonFilter { pokemon -> pokemon.gender == Gender.FEMALE}
                    //IV = X
                    "hp=${try{filter.substring(3)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.HP] == (filter.substring(
                            3
                        ).toIntOrNull() ?: 0)
                    }
                    "atk=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.ATTACK] == (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "def=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.DEFENCE] == (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spa=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_ATTACK] == (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spd=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_DEFENCE] == (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spe=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPEED] == (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    //IV >= X
                    "hp>=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.HP]!! >= (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "atk>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.ATTACK]!! >= (filter.substring(
                            5
                        ).toIntOrNull() ?: 0)
                    }
                    "def>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.DEFENCE]!! >= (filter.substring(
                            5
                        ).toIntOrNull() ?: 0)
                    }
                    "spa>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_ATTACK]!! >= (filter.substring(
                            5
                        ).toIntOrNull() ?: 0)
                    }
                    "spd>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_DEFENCE]!! >= (filter.substring(
                            5
                        ).toIntOrNull() ?: 0)
                    }
                    "spe>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPEED]!! >= (filter.substring(
                            5
                        ).toIntOrNull() ?: 0)
                    }
                    //IV > X
                    "hp>${try{filter.substring(3)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.HP]!! > (filter.substring(
                            3
                        ).toIntOrNull() ?: 0)
                    }
                    "atk>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.ATTACK]!! > (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "def>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.DEFENCE]!! > (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spa>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_ATTACK]!! > (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spd>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPECIAL_DEFENCE]!! > (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    "spe>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        pokemon.ivs[Stats.SPEED]!! > (filter.substring(
                            4
                        ).toIntOrNull() ?: 0)
                    }
                    //X amount of perfect IVs
                    "perfect", "6x" -> PokemonFilter { pokemon -> ivCheck(pokemon,6) }
                    "5x" -> PokemonFilter { pokemon -> ivCheck(pokemon, 5) }
                    "4x" -> PokemonFilter { pokemon -> ivCheck(pokemon, 4) }
                    "3x" -> PokemonFilter { pokemon -> ivCheck(pokemon, 3) }
                    "2x" -> PokemonFilter { pokemon -> ivCheck(pokemon, 2) }
                    "1x" -> PokemonFilter { pokemon -> ivCheck(pokemon, 1) }
                    else -> PokemonFilter { pokemon -> PokemonProperties.parse(filter).matches(pokemon) }
                }
                if (inverted) {
                    filters.add(pokemonFilter.inverted())
                } else {
                    filters.add(pokemonFilter)
                }
            }

            return Search(filters)
        }
        private fun ivCheck(pokemon: Pokemon, amt: Int): Boolean{
            var count = 0
            for(s in pokemon.ivs){
                if(s.value == 31){
                    count++
                }
            }
            return count>=5
        }
    }


    fun passes(pokemon: Pokemon?): Boolean {
        if (pokemon == null) return false
        val uuid = pokemon.uuid
        if (passed.contains(uuid)) return true
        if (failed.contains(uuid)) return false
        val passes = filters.all { it.test(pokemon) }
        val set = if (passes) passed else failed
        set.add(uuid)
        return passes
    }
}
