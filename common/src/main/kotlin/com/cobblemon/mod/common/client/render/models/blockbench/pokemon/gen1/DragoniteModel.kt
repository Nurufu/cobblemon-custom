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
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isSubmergedInWater
import com.cobblemon.mod.common.util.isTouchingWater
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DragoniteModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dragonite")
    override val head = getPart("head")

    override var portraitScale = 1.2F
    override var portraitTranslation = Vec3d(-0.2, 2.6, 0.0)

    override var profileScale = 0.41F
    override var profileTranslation = Vec3d(0.0, 1.1, -6.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var wateridle: Pose
    lateinit var waterswim: Pose
    lateinit var surfacewateridle: Pose
    lateinit var surfacewaterswim: Pose
    lateinit var battleidle: Pose
    lateinit var sleep: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose

    override val cryAnimation = CryProvider { bedrockStateful("dragonite", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dragonite", "blink")}
        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            animations = arrayOf(
                bedrock("dragonite", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            condition = { !it.isBattling},
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling},
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 10F),
                bedrock("dragonite", "battle_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "air_fly")
            )
        )

        wateridle = registerPose(
            poseName = "wateridle",
            poseType = PoseType.FLOAT,
            condition = { it.isSubmergedInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "water_idle")
            )
        )

        waterswim = registerPose(
            poseName = "waterswim",
            poseType = PoseType.SWIM,
            condition = { it.isSubmergedInWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "water_swim")
            )
        )

        surfacewateridle = registerPose(
            poseName = "surfacewateridle",
            poseTypes = PoseType.STANDING_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "surfacewater_idle")
            )
        )

        surfacewaterswim = registerPose(
            poseName = "surfacewaterswim",
            poseTypes = PoseType.MOVING_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(maxPitch = 15F),
                bedrock("dragonite", "surfacewater_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dragonite", "faint") else null
}