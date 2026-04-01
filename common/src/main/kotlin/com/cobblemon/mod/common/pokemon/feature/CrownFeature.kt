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

const val CROWNED = "crowned"

object CrownFeatureHandler {
    fun updateCrown(pokemon: Pokemon) {
        val feature = pokemon.getFeature<FlagSpeciesFeature>(CROWNED) ?: return
        feature.enabled = pokemon.heldItem().isOf(CobblemonItems.RUSTED_SWORD) || pokemon.heldItem().isOf(CobblemonItems.RUSTED_SHIELD)
        pokemon.updateAspects()
        pokemon.markFeatureDirty(feature)
    }
}