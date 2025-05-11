/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DoubladeModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("doublade")

    override var portraitScale = 3.0F
    override var portraitTranslation = Vec3d(-1.8, -0.6, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3d(-0.2, 0.7, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose

    override val cryAnimation = CryProvider { bedrockStateful("doublade", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("doublade", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("doublade", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("doublade", "ground_walk")
                )
        )
    }
}
