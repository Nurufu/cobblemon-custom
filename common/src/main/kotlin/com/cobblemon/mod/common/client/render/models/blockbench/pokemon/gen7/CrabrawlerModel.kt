/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class CrabrawlerModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame{
    override val rootPart = root.registerChildWithAllChildren("crabrawler")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3d(-0.25, -0.5, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(0.0, 0.7, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var portrait: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("crabrawler", "cry") }

    override fun registerPoses() {
        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "portrait")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.PROFILE,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "ground_idle"),
                bedrock("crabrawler", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("crabrawler", "faint") else null
}