/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.entity

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity

interface PokemonSideDelegate : EntitySideDelegate<PokemonEntity> {
    fun changePokemon(pokemon: Pokemon)
    fun drop(source: DamageSource?) {}
    fun updatePostDeath() {}
    fun handleStatus(status: Byte) {}
    fun spawnShinyParticle(player: PlayerEntity) {}
}