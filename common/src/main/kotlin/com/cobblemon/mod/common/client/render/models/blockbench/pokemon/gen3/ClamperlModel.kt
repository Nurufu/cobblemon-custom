/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ClamperlModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("clamperl")
    override val head = getPart("head")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3d(0.0, -2.15, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3d(0.0, 0.5, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walking: PokemonPose
    lateinit var floating: PokemonPose
    lateinit var swimming: PokemonPose
    lateinit var portrait: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("clamperl", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES - PoseType.PORTRAIT + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "ground_idle"),
                bedrock("clamperl", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseType = PoseType.FLOAT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "water_swim"),
                bedrock("clamperl", "water_idle")
            )
        )

        portrait = registerPose(
            poseName = "portrait",
            poseType = PoseType.PORTRAIT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("clamperl", "portrait")
            )
        )
    }
}