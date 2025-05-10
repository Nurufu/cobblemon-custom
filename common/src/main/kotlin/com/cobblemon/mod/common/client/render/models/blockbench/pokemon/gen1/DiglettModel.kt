/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DiglettModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("diglett")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3d(-0.06, -0.86, 0.0)

    override var profileScale = 0.9F
    override var profileTranslation = Vec3d(0.0, 0.32, 0.0)

    lateinit var stand: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose
    lateinit var sleep: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("diglett", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("diglett", "blink")}
        val quirk = quirk { bedrockStateful("diglett", "quirk_idle")}

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("diglett", "sleep"))
        )

        stand = registerPose(
            poseName = "stand",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink, quirk),
            idleAnimations = arrayOf(bedrock("diglett", "ground_idle"))
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(bedrock("diglett", "ground_walk"))
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = STATIONARY_POSES,
            quirks = arrayOf(blink, quirk),
            idleAnimations = arrayOf(bedrock("diglett", "battle_idle"))
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>
    ) = bedrockStateful("diglett", "faint")
}