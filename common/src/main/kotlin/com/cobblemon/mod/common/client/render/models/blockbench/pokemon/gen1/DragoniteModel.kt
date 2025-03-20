/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DragoniteModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dragonite")
    override val head = getPart("head")

    override var portraitScale = 1.2F
    override var portraitTranslation = Vec3d(-0.2, 2.6, 0.0)

    override var profileScale = 0.41F
    override var profileTranslation = Vec3d(0.0, 1.1, -6.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var wateridle: PokemonPose
    lateinit var waterswim: PokemonPose
    lateinit var surfacewateridle: PokemonPose
    lateinit var surfacewaterswim: PokemonPose
    lateinit var battleidle: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var hover: PokemonPose
    lateinit var fly: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("dragonite", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dragonite", "blink")}
        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("dragonite", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            condition = { !it.isBattling},
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling},
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 10F),
                bedrock("dragonite", "battle_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "air_fly")
            )
        )

        wateridle = registerPose(
            poseName = "wateridle",
            poseType = PoseType.FLOAT,
            condition = { it.isSubmergedInWater },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "water_idle")
            )
        )

        waterswim = registerPose(
            poseName = "waterswim",
            poseType = PoseType.SWIM,
            condition = { it.isSubmergedInWater },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "water_swim")
            )
        )

        surfacewateridle = registerPose(
            poseName = "surfacewateridle",
            poseTypes = PoseType.STANDING_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "surfacewater_idle")
            )
        )

        surfacewaterswim = registerPose(
            poseName = "surfacewaterswim",
            poseTypes = PoseType.MOVING_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "surfacewater_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dragonite", "faint") else null
}