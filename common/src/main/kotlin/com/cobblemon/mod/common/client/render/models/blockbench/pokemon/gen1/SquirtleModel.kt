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
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.asExpressionLike
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class SquirtleModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("squirtle")
    override val head = getPart("head_ai")


    override var portraitScale = 2.02F
    override var portraitTranslation = Vec3d(-0.12, -0.21, 0.0)

    override var profileScale = 0.78F
    override var profileTranslation = Vec3d(-0.04, 0.57, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose
    lateinit var shoulderLeft: PokemonPose
    lateinit var shoulderRight: PokemonPose
    lateinit var floating: PokemonPose
    lateinit var swimming: PokemonPose
    lateinit var water_surface_idle: PokemonPose
    lateinit var water_surface_swim: PokemonPose

    val shoulderOffset = 5.5
    val wateroffset = -10

    override fun registerPoses() {
        animations["physical"] = "q.bedrock_primary('squirtle', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["special"] = "q.bedrock_primary('squirtle', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["status"] = "q.bedrock_primary('squirtle', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["recoil"] = "q.bedrock_stateful('squirtle', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('squirtle', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('squirtle', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("squirtle", "blink") }
        val quirkidle = quirk { bedrockStateful("squirtle", "quirk_ground_idle") }
        val quirkwater = quirk { bedrockStateful("squirtle", "quirk_water") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink),
            animations = mutableMapOf("faint" to faint),
            idleAnimations = arrayOf(bedrock("squirtle", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES - PoseType.HOVER + UI_POSES,
            quirks = arrayOf(blink, quirkidle),
            condition = { !it.isBattling && !it.isTouchingWater && !it.isSubmergedInWater},
            animations = mutableMapOf("faint" to faint),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "ground_idle")
            )
        )

        battleidle = registerPose(
            poseType = PoseType.STAND,
            poseName = "battle_standing",
            quirks = arrayOf(blink, quirkidle),
            condition = { it.isBattling },
            animations = mutableMapOf("faint" to faint),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink),
            condition = { !it.isTouchingWater && !it.isSubmergedInWater},
            animations = mutableMapOf("faint" to faint),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            condition = { it.isSubmergedInWater },
            animations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink, quirkwater),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            condition = { it.isSubmergedInWater },
            animations = mutableMapOf("faint" to faint),
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "water_swim"),
            )
        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            animations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink),
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isSubmergedInWater && it.isTouchingWater },
            animations = mutableMapOf("faint" to faint),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "shoulder_left")
            ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -2)
                )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "shoulder_right")
            ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -2)
                )
        )

    }
}