/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ShuckleModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("shuckle")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3d(-0.7, 0.1, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3d(0.0, 0.8, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("shuckle", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("shuckle", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("shuckle", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            transformTicks = 5,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("shuckle", "ground_walk")
            )
        )

        sleep = registerPose(
            poseName = "sleep",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            transformTicks = 5,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("shuckle", "sleep")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("shuckle", "faint") else null
}