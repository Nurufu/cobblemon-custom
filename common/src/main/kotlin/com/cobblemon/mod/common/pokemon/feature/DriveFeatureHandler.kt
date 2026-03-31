/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.Items

const val DRIVE = "DRIVE"

object DriveFeatureHandler {
    fun updateDrive(pokemon: Pokemon) {
        if(pokemon.species.name == "Genesect" && pokemon.getFeature<StringSpeciesFeature>(DRIVE) == null) pokemon.features.add(
            StringSpeciesFeature(
                DRIVE,
                "none"
            ))
        val feature = pokemon.getFeature<StringSpeciesFeature>(DRIVE) ?: return
        //reset if holding air or drive doesn't match current type
        if(pokemon.heldItem().isOf(Items.AIR) || !compareDrive(pokemon)) {
            feature.value = "none"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
        }
            if (pokemon.heldItem().isOf(CobblemonItems.BURN_DRIVE)) feature.value = "burn"
            else if (pokemon.heldItem().isOf(CobblemonItems.DOUSE_DRIVE)) feature.value = "douse"
            else if (pokemon.heldItem().isOf(CobblemonItems.CHILL_DRIVE)) feature.value = "chill"
            else if (pokemon.heldItem().isOf(CobblemonItems.SHOCK_DRIVE)) feature.value = "shock"
            pokemon.updateAspects()
            pokemon.markFeatureDirty(feature)
    }

    fun compareDrive(pokemon: Pokemon): Boolean{
        val feature = pokemon.getFeature<StringSpeciesFeature>(DRIVE) ?: return false
        return if (pokemon.heldItem().isOf(CobblemonItems.BURN_DRIVE) && feature.value == "burn") true
        else if (pokemon.heldItem().isOf(CobblemonItems.DOUSE_DRIVE) && feature.value == "douse") true
        else if (pokemon.heldItem().isOf(CobblemonItems.CHILL_DRIVE) && feature.value == "chill") true
        else if (pokemon.heldItem().isOf(CobblemonItems.SHOCK_DRIVE) && feature.value == "shock") true
        else false
    }
}