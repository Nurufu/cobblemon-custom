/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DusknoirModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("dusknoir")
    override val head = getPart("eye")

    override var portraitScale = 1.65F
    override var portraitTranslation = Vec3d(-0.9, 2.65, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3d(-0.2, 1.25, 0.0)

//    lateinit var hover: PokemonPose
//    lateinit var fly: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose

    var spoopytail = getPart("tail")

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("dusknoir", "cry") }

    override fun registerPoses() {
        //val blink = quirk { bedrockStateful("dusknoir", "blink") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("dusknoir", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("dusknoir", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("dusknoir", "ground_walk")
            )
        )

//        hover = registerPose(
//            poseName = "hover",
//            poseTypes = PoseType.UI_POSES + PoseType.HOVER + PoseType.FLOAT,
//            condition = { !it.isBattling },
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                bedrock("dusknoir", "air_idle")
//            )
//        )
//
//        fly = registerPose(
//            poseName = "fly",
//            poseTypes = setOf(PoseType.FLY, PoseType.SWIM, PoseType.WALK),
//            condition = { !it.isBattling },
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                bedrock("dusknoir", "air_fly")
//            )
//        )
//
        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                bedrock("dusknoir", "battle_idle")
            )
        )
    }
//
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(hover, fly, sleep, standing, walk, battleidle)) bedrockStateful("dusknoir", "faint") else null
}