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

class MasquerainModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("masquerain")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3d(-0.45, -1.0, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3d(0.0, 0.7, 0.0)

    lateinit var walk: PokemonPose
    lateinit var standing: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("masquerain", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("masquerain", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("masquerain", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("masquerain", "ground_walk")
            )
        )
    }
}