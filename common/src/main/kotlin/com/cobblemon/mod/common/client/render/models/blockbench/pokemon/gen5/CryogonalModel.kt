/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class CryogonalModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("cryogonal")
    override val head = getPart("body")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3d(0.05, 0.61, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(0.0, 0.9, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("cryogonal", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("cryogonal", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("cryogonal", "ground_idle")
            )
        )

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                idleAnimations = arrayOf(bedrock("cryogonal", "sleep"))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("cryogonal", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(
            pokemonEntity: PokemonEntity,
            state: PoseableEntityState<PokemonEntity>
    ) = if (state.isNotPosedIn(sleep)) bedrockStateful("cryogonal", "faint") else null
}