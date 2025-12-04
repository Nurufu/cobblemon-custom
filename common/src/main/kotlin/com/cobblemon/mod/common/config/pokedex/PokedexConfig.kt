/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.config.pokedex

import com.cobblemon.mod.common.api.pokedex.trackeddata.CountTypeCaughtGlobalTrackedData
import com.cobblemon.mod.common.api.pokedex.trackeddata.FormTrackedData
import com.cobblemon.mod.common.api.pokedex.trackeddata.GlobalTrackedData
import com.cobblemon.mod.common.api.pokedex.trackeddata.SpeciesTrackedData

/**
 * Determines what stats are tracked in the pokedex. Should be read from file eventually
 *
 * @author Apion
 * @since February 24, 2024
 */
object PokedexConfig {
    val global = setOf<GlobalTrackedData>(
        CountTypeCaughtGlobalTrackedData("fire")
    )
    val species = emptySet<SpeciesTrackedData>()
    val form = emptySet<FormTrackedData>()
}