/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.util.getBlockStates
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

/**
 * A spawning context that was generated for a fishing action.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawningContext(
    cause: FishingSpawnCause,
    world: ServerWorld,
    pos: BlockPos,
    influences: MutableList<SpawningInfluence>
) : TriggerSpawningContext(
    cause = cause,
    world = world,
    position = pos,
    light = world.getLightLevel(pos),
    skyLight = world.getLightLevel(pos.up()),
    canSeeSky = world.isSkyVisibleAllowingSea(pos),
    influences = influences
) {
    val nearbyBlocks = world.getBlockStates(Box.of(pos.toVec3d(), 10.0, 10.0, 10.0))
    val nearbyBlockTypes: List<Block> by lazy { nearbyBlocks.mapNotNull { it.block }.distinct() }
    val rodStack = cause.rodStack
    val rodItem = cause.rodItem
    val rodBait = cause.bait
}