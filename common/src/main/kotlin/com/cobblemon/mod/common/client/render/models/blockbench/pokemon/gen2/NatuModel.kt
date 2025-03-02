/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class NatuModel(root: ModelPart) : PokemonPoseableModel(), BipedFrame, HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("natu")
    override val head = getPart("torso")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.33F
    override var portraitTranslation = Vec3d(0.04, -1.66, 0.0)

    override var profileScale = 1.09F
    override var profileTranslation = Vec3d(0.0, 0.03, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose
    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("natu", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("natu", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("natu", "sleep")
            )
        )
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F, yawMultiplier = 0F),
                bedrock("natu", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(pitchMultiplier = 0.5F, yawMultiplier = 0F),
                bedrock("natu", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.8F, amplitudeMultiplier = 0.6F)
//                bedrock("natu", "ground_walk")
            )
        )
    }
}