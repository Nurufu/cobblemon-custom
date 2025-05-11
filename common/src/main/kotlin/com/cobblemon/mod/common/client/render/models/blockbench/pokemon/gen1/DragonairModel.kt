/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isSubmergedInWater
import com.cobblemon.mod.common.util.isTouchingWater
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DragonairModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dragonair")
    override val head = getPart("head")

    override var portraitScale = 2.3F
    override var portraitTranslation = Vec3d(-0.02, 1.58, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(0.1, 0.9, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var water_idle: CobblemonPose
    lateinit var water_swim: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var water_sleep: CobblemonPose
    lateinit var surface_swim: CobblemonPose
    lateinit var surface_float: CobblemonPose
    lateinit var battle_idle: CobblemonPose
    lateinit var hovering: CobblemonPose
    lateinit var flying: CobblemonPose

    val flyingoffset = -12

    override val cryAnimation = CryProvider { bedrockStateful("dragonair", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("dragonair", "blink") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { !it.isTouchingWater },
            animations = arrayOf(
                bedrock("dragonair", "sleep")
            )
        )

        water_sleep = registerPose(
            poseName = "water_sleep",
            poseType = PoseType.SLEEP,
            condition = { it.isTouchingWater },
            animations = arrayOf(
                bedrock("dragonair", "water_sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES - PoseType.FLOAT - PoseType.HOVER,
            quirks = arrayOf(blink),
            condition = { !it.isBattling && !it.isTouchingWater},
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("dragonair", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = MOVING_POSES - PoseType.SWIM - PoseType.FLY,
            quirks = arrayOf(blink),
            condition = {!it.isTouchingWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dragonair", "ground_walk")
            )
        )

        hovering = registerPose(
                poseName = "hovering",
                poseType = PoseType.HOVER,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformTicks = 10,
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dragonair", "water_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, flyingoffset)
                )
        )

        flying = registerPose(
                poseName = "flying",
                poseType = PoseType.FLY,
                quirks = arrayOf(blink),
                condition = { !it.isBattling },
                transformTicks = 10,
                animations = arrayOf(
                        singleBoneLook(),
                        bedrock("dragonair", "water_swim")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, flyingoffset)
                )
        )

        water_idle = registerPose(
            poseName = "water_idle",
            poseType = PoseType.FLOAT,
            condition = { it.isSubmergedInWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dragonair", "water_idle")
            )
        )

        water_swim = registerPose(
            poseName = "water_swim",
            poseType = PoseType.SWIM,
            condition = { it.isSubmergedInWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dragonair", "water_swim")
            )
        )

        surface_float = registerPose(
            poseName = "surface_float",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dragonair", "surfacewater_idle")
            )
        )

        surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            condition = { !it.isSubmergedInWater && it.isTouchingWater},
            transformTicks = 10,
            animations = arrayOf(
                bedrock("dragonair", "surfacewater_swim")
            )
        )

        battle_idle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("dragonair", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dragonair", "faint") else null
}