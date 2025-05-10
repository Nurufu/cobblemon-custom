/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.ALL_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class KakunaModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("kakuna")
    override val head = getPart("head")

    override var portraitScale = 1.7F
    override var portraitTranslation = Vec3d(0.1, -0.4, 0.0)
    override var profileScale = 0.96F
    override var profileTranslation = Vec3d(0.0, 0.35, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("kakuna", "cry") }

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("kakuna", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = ALL_POSES - PoseType.SLEEP,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kakuna", "ground_idle")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>,
    ) = bedrockStateful("kakuna", "faint")
}