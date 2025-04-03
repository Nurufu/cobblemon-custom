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
import com.cobblemon.mod.common.api.pokemon.stats.Stat
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
                    //IV <= X
                    "hp<=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.HP)
                    }
                    "atk<=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.ATTACK)
                    }
                    "def<=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.DEFENCE)
                    }
                    "spa<=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPECIAL_ATTACK)
                    }
                    "spd<=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPECIAL_DEFENCE)
                    }
                    "spe<=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPEED)
                    }
                    //IV >= X
                    "hp>=${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.HP)
                    }
                    "atk>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.ATTACK)
                    }
                    "def>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.DEFENCE)
                    }
                    "spa>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPECIAL_ATTACK)
                    }
                    "spd>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPECIAL_DEFENCE)
                    }
                    "spe>=${try{filter.substring(5)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreaterOrEqual(pokemon, filter.substring(5).toIntOrNull() ?: 0, Stats.SPEED)
                    }
                    //IV > X
                    "hp>${try{filter.substring(3)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(3).toIntOrNull() ?: 0, Stats.HP)
                    }
                    "atk>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.ATTACK)
                    }
                    "def>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.DEFENCE)
                    }
                    "spa>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPECIAL_ATTACK)
                    }
                    "spd>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPECIAL_DEFENCE)
                    }
                    "spe>${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPEED)
                    }
                    //IV < X
                    "hp<${try{filter.substring(3)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(3).toIntOrNull() ?: 0, Stats.HP)
                    }
                    "atk<${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.ATTACK)
                    }
                    "def<${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.DEFENCE)
                    }
                    "spa<${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPECIAL_ATTACK)
                    }
                    "spd<${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPECIAL_DEFENCE)
                    }
                    "spe<${try{filter.substring(4)}catch(e:Exception){null}}" -> PokemonFilter { pokemon ->
                        !ivGreater(pokemon, filter.substring(4).toIntOrNull() ?: 0, Stats.SPEED)
                    }
                    //X amount of perfect IVs
                    "perfect", "6x" -> PokemonFilter { pokemon -> ivPerfect(pokemon,6) }
                    "5x" -> PokemonFilter { pokemon -> ivPerfect(pokemon, 5) }
                    "4x" -> PokemonFilter { pokemon -> ivPerfect(pokemon, 4) }
                    "3x" -> PokemonFilter { pokemon -> ivPerfect(pokemon, 3) }
                    "2x" -> PokemonFilter { pokemon -> ivPerfect(pokemon, 2) }
                    "1x" -> PokemonFilter { pokemon -> ivPerfect(pokemon, 1) }
                    //IV % Total
                    "%>${try{filter.substring(2)}catch(e:Exception){null}}" -> PokemonFilter{ pokemon ->
                        ivSum(pokemon, filter.substring(2).toIntOrNull() ?: 0)
                    }
                    "%<${try{filter.substring(2)}catch(e:Exception){null}}" -> PokemonFilter{ pokemon ->
                        !ivSum(pokemon, filter.substring(2).toIntOrNull() ?: 0)
                    }
                    //Quick 5x31 1x0
                    "0hp" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.HP)}
                    "0atk" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.ATTACK)}
                    "0def" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.DEFENCE)}
                    "0spa" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.SPECIAL_ATTACK)}
                    "0spd" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.SPECIAL_DEFENCE)}
                    "0spe" -> PokemonFilter { pokemon -> iv5X(pokemon, Stats.SPEED)}
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
        private fun ivGreater(pokemon: Pokemon, x: Int, s: Stat): Boolean{
            return pokemon.ivs[s]!! > x
        }

        private fun ivGreaterOrEqual(pokemon: Pokemon, x: Int, s: Stat): Boolean{
            return pokemon.ivs[s]!! >= x
        }

        private fun iv5X(pokemon: Pokemon, s: Stat): Boolean{
            var total = 0
            for(i in pokemon.ivs){
                total += i.value
            }
            return pokemon.ivs[s]!! == 0 && total == 155
        }

        private fun ivPerfect(pokemon: Pokemon, amt: Int): Boolean{
            var count = 0
            for(s in pokemon.ivs){
                if(s.value == 31){
                    count++
                }
            }
            return count>=amt
        }

        private fun ivSum(pokemon: Pokemon, p: Int): Boolean{
            var total= 0f

            for(s in pokemon.ivs){
                total += s.value
            }
            total = (total/186f)*100
            return p<=total
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
