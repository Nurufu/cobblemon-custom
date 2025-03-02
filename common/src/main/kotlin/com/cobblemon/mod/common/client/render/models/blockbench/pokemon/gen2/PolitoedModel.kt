/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class PolitoedModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("politoed")
    override val head = getPart("head")

    override val leftLeg = getPart("leftleg")
    override val rightLeg = getPart("rightleg")

    override var portraitScale = 2.4F
    override var portraitTranslation = Vec3d(-0.04, -0.55, 0.0)

    override var profileScale = 1.1F
    override var profileTranslation = Vec3d(0.0, 0.1, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walking: PokemonPose
    lateinit var float: PokemonPose
    lateinit var swim: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("politoed", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "ground_idle")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = setOf(PoseType.SWIM, PoseType.FLY),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("politoed", "water_swim")
            )
        )
    }

    //    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("politoed", "faint") else null
}