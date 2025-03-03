/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class WishiwashiSchoolingModel (root: ModelPart) : PokemonPoseableModel(){
    override val rootPart = root.registerChildWithAllChildren("wishiwashi_school")

    override var portraitScale = 0.5F
    override var portraitTranslation = Vec3d(-0.4, 0.8, 0.0)

    override var profileScale = 0.2F
    override var profileTranslation = Vec3d(0.0, 1.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var floating: PokemonPose
    lateinit var swimming: PokemonPose
    lateinit var watersleep: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("wishiwashi_school", "cry") }

    val offsetY = -8.0
    override fun registerPoses() {
        watersleep = registerPose(
            poseType = PoseType.SLEEP,
            condition = { it.isTouchingWater },
            idleAnimations = arrayOf(bedrock("wishiwashi_school", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STANDING_POSES - PoseType.FLOAT + PoseType.UI_POSES,
            idleAnimations = arrayOf(
                bedrock("wishiwashi_school", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            idleAnimations = arrayOf(
                bedrock("wishiwashi_school", "water_swim")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(0.0, offsetY, 0.0)
            )
        )

        floating = registerPose(
            poseName = "floating",
            poseType = PoseType.FLOAT,
            idleAnimations = arrayOf(
                bedrock("wishiwashi_school", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                bedrock("wishiwashi_school", "water_swim")
            )
        )
    }
}