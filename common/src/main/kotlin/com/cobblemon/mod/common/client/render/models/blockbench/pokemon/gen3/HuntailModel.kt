/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveSegment
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class HuntailModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("huntail")
    override val head = getPart("head")

    override var portraitScale = 2.6F
    override var portraitTranslation = Vec3d(-1.3, -2.5, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var floating: PokemonPose
    lateinit var swimming: PokemonPose

    private val tail = getPart("tail")
    private val tail2 = getPart("tail2")
    private val tail3 = getPart("tail3")
    private val tail4 = getPart("tail4")
    private val tail5 = getPart("tail5")
    private val tail6 = getPart("tail6")

    val tailSegment = WaveSegment(modelPart = tail, length = 5F)
    val tail2Segment = WaveSegment(modelPart = tail2, length = 5F)
    val tail3Segment = WaveSegment(modelPart = tail3, length = 5F)
    val tail4Segment = WaveSegment(modelPart = tail4, length = 5F)
    val tail5Segment = WaveSegment(modelPart = tail5, length = 5F)
    val tail6Segment = WaveSegment(modelPart = tail6, length = 5F)

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("huntail", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("huntail", "ground_idle"),
                WaveAnimation(
                    frame = this,
                    waveFunction = sineFunction(
                        period = 8F,
                        amplitude = 0.8F
                    ),
                    basedOnLimbSwing = true,
                    oscillationsScalar = 5F,
                    head = head,
                    rotationAxis = ModelPartTransformation.Y_AXIS,
                    motionAxis = ModelPartTransformation.X_AXIS,
                    moveHead = false,
                    headLength = 16F,
                    segments = arrayOf(
                        tailSegment,
                        tail2Segment,
                        tail3Segment,
                        tail4Segment,
                        tail5Segment,
                        tail6Segment
                    )
                )
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseTypes = PoseType.UI_POSES + PoseType.FLOAT,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("huntail", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("huntail", "water_swim"),
            )
        )
    }
}