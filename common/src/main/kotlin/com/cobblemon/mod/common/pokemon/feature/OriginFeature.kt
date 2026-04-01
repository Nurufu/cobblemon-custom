/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.Items

const val ORIGIN = "origin"

object OriginFeatureHandler {
    fun updateOrigin(pokemon: Pokemon) {
        val feature = pokemon.getFeature<FlagSpeciesFeature>(ORIGIN) ?: return
        feature.enabled = pokemon.heldItem().isOf(CobblemonItems.ADAMANT_CRYSTAL) || pokemon.heldItem().isOf(CobblemonItems.LUSTROUS_GLOBE) || pokemon.heldItem().isOf(CobblemonItems.GRISEOUS_CORE)
        pokemon.updateAspects()
        pokemon.markFeatureDirty(feature)
    }
}