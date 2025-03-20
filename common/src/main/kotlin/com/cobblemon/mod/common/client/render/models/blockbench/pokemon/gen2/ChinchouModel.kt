/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ChinchouModel (root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("chinchou")

    override var portraitScale = 1.75F
    override var portraitTranslation = Vec3d(-0.3, -0.8, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3d(-0.05, 0.45, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var floating: PokemonPose
    lateinit var swimming: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var watersleep: PokemonPose
    lateinit var battleidle: PokemonPose
    lateinit var waterbattleidle: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("chinchou", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("chinchou", "blink")}
        val idleQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("chinchou", "ground_quirk")}
        val waterQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("chinchou", "water_idle_quirk")}

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            condition = { !it.isTouchingWater },
            idleAnimations = arrayOf(bedrock("chinchou", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            poseType = PoseType.SLEEP,
            condition = { it.isTouchingWater },
            idleAnimations = arrayOf(bedrock("chinchou", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
            condition = { !it.isBattling && !it.isTouchingWater && !it.isSubmergedInWater},
            quirks = arrayOf(blink, idleQuirk),
            idleAnimations = arrayOf(
                bedrock("chinchou", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 10,
            poseType = PoseType.WALK,
            condition = { !it.isTouchingWater && !it.isSubmergedInWater},
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("chinchou", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            condition = { it.isTouchingWater },
            quirks = arrayOf(blink, waterQuirk),
            idleAnimations = arrayOf(
                bedrock("chinchou", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            condition = { it.isTouchingWater },
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("chinchou", "water_swim"),
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && !it.isTouchingWater },
            idleAnimations = arrayOf(
                bedrock("chinchou", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "water_battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling && it.isTouchingWater },
            idleAnimations = arrayOf(
                bedrock("chinchou", "water_battle_idle")
            )
        )
    }
    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk, battleidle, sleep)) bedrockStateful("chinchou", "faint") else if (state.isPosedIn( waterbattleidle, watersleep, floating, swimming )) bedrockStateful("chinchou", "water_faint") else null
}