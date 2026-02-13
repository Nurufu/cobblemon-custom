/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import net.minecraft.util.math.Vec3d

class CrabrawlerModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame{
    override val rootPart = root.registerChildWithAllChildren("crabrawler")
    override val head = getPart("head")

    override val cryAnimation = CryProvider { bedrockStateful("crabrawler", "cry") }


    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3d(-0.25, -0.5, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(0.0, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var portrait: Pose

    override fun registerPoses() {
        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "portrait")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.PROFILE,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("crabrawler", "ground_idle"),
                bedrock("crabrawler", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("crabrawler", "faint") else null
}