/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.condition.BucketPrecalculation
import com.cobblemon.mod.common.api.spawning.condition.ContextPrecalculation
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.data.CobblemonDataProvider

/**
 * A collection of all of Cobblemon's general-purpose [SpawnPool]s. These
 * are referenced by Cobblemon spawner implementations. Updating these will update
 * the spawns across the entire mod.
 *
 * @author Hiroku
 * @since February 10th, 2022
 */
object CobblemonSpawnPools {
    /** [SpawnPool] used for standard world spawning. */
    lateinit var WORLD_SPAWN_POOL: SpawnPool
    /** [SpawnPool] used for fishing. */
    lateinit var FISHING_SPAWN_POOL: SpawnPool

    fun load() {
        WORLD_SPAWN_POOL = CobblemonDataProvider.register(SpawnPool("world").addPrecalculators(ContextPrecalculation, BucketPrecalculation))
        FISHING_SPAWN_POOL = CobblemonDataProvider.register(SpawnPool("fishing").addPrecalculators(ContextPrecalculation, BucketPrecalculation))
    }
}