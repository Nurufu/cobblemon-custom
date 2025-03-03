/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class LickitungModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("lickitung")
    override val head = getPart("head")
    override val leftLeg = getPart("left_leg")
    override val rightLeg = getPart("right_leg")
    override val leftArm = getPart("left_upper_arm")
    override val rightArm = getPart("right_upper_arm")

    override var portraitScale = 2.1F
    override var portraitTranslation = Vec3d(-0.1, 0.0, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3d(0.0, 0.55, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("lickitung", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("lickitung", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("lickitung", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("lickitung", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("lickitung", "battle_idle")
            )

        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("lickitung", "faint") else null
}