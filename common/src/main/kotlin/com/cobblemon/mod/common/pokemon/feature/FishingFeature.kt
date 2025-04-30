/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature


import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon


/**
 * A fishing. You know the one.
 *
 * @author Hiroku
 * @since November 25th, 2022
 */

const val FISHED = "fished"

object FishedFeatureHandler {

    fun updateFished(pokemon: Pokemon, fished: Boolean?) {
        val feature = pokemon.getFeature<FlagSpeciesFeature>(FISHED) ?: return
        val currentFished = feature.enabled
        if (currentFished != fished && fished != null) {
            feature.enabled = fished
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
    }
}