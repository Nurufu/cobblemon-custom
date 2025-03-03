/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DonphanModel (root: ModelPart) : PokemonPoseableModel(), QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("donphan")

    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3d(-0.85, -0.3, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3d(-0.1, 0.73, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var battle_idle: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("donphan", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("donphan", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("donphan", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            condition = { !it.isBattling },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("donphan", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("donphan", "ground_walk")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                bedrock("donphan", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("donphan", "faint") else null
}