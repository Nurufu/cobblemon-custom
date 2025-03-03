/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.ALL_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class CarnivineModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame{
    override val rootPart = root.registerChildWithAllChildren("carnivine")
    override val head = getPart("head")

    override var portraitScale = 1.07F
    override var portraitTranslation = Vec3d(-0.42, 0.9, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3d(0.0, 0.98, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var hover: PokemonPose
    lateinit var flying: PokemonPose
    lateinit var battle_idle: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("carnivine", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("carnivine", "blink") }
        val idleQuirk = quirk { bedrockStateful("carnivine", "quirk_ground_idle") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES - PoseType.HOVER,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, idleQuirk),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("carnivine", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("carnivine", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformTicks = 10,
            quirks = arrayOf(blink, idleQuirk),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("carnivine", "air_idle")
            )
        )

        flying = registerPose(
            poseName = "flying",
            poseType = PoseType.FLY,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("carnivine", "air_fly")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("carnivine", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("carnivine", "faint") else null
}