/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class WoolooModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("wooloo")
    override val head = getPart("head")
    override val foreLeftLeg= getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    val wool = getPart("wool_shearable")

    override var portraitScale = 3.1F
    override var portraitTranslation = Vec3d(-0.85, -1.8, 0.0)
    override var profileScale = 0.9F
    override var profileTranslation = Vec3d(0.0, 0.4, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var shearedstanding: PokemonPose
    lateinit var shearedwalk: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("wooloo", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("wooloo", "blink") }
        standing = registerPose(
                poseName = "standing",
                poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
                transformTicks = 0,
                quirks = arrayOf(blink),
                condition = { DataKeys.HAS_BEEN_SHEARED !in it.aspects },
                transformedParts = arrayOf(
                        wool.createTransformation().withVisibility(visibility = true)
                ),
                idleAnimations = arrayOf(
                        singleBoneLook(),
                        bedrock("wooloo", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walking",
                poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
                transformTicks = 0,
                quirks = arrayOf(blink),
                condition = { DataKeys.HAS_BEEN_SHEARED !in it.aspects },
                transformedParts = arrayOf(
                        wool.createTransformation().withVisibility(visibility = true)
                ),
                idleAnimations = arrayOf(
                        singleBoneLook(),
                        bedrock("wooloo", "ground_walk")
                )
        )

        shearedstanding = registerPose(
                poseName = "shearedstanding",
                poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
                transformTicks = 0,
                quirks = arrayOf(blink),
                condition = { DataKeys.HAS_BEEN_SHEARED in it.aspects },
                transformedParts = arrayOf(
                        wool.createTransformation().withVisibility(visibility = false)
                ),
                idleAnimations = arrayOf(
                        singleBoneLook(),
                        bedrock("wooloo", "ground_idle")
                )
        )
        shearedwalk = registerPose(
                poseName = "shearedwalking",
                poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
                transformTicks = 0,
                quirks = arrayOf(blink),
                condition = { DataKeys.HAS_BEEN_SHEARED in it.aspects },
                transformedParts = arrayOf(
                        wool.createTransformation().withVisibility(visibility = false)
                ),
                idleAnimations = arrayOf(
                        singleBoneLook(),
                        bedrock("wooloo", "ground_walk")
                )
        )
    }

    override fun getEatAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>
    ) = bedrockStateful("wooloo", "eat")

}