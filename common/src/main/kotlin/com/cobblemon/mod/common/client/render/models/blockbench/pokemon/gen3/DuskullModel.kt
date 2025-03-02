/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DuskullModel (root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("duskull")

    override var portraitScale = 1.6F
    override var portraitTranslation = Vec3d(-0.25, -0.77, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3d(0.0, 0.35, 0.0)

    lateinit var hover: PokemonPose
    lateinit var fly: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("duskull", "cry") }

    override fun registerPoses() {

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("duskull", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                bedrock("duskull", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                bedrock("duskull", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.UI_POSES + PoseType.HOVER + PoseType.FLOAT,
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                bedrock("duskull", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM, PoseType.WALK),
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                bedrock("duskull", "air_fly")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.MOVING_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                bedrock("duskull", "battle_idle")
            )

        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(hover, fly, sleep, standing, walk, battleidle)) bedrockStateful("duskull", "faint") else null
}