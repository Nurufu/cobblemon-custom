/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class TentacruelModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("tentacruel")

    override var portraitScale = 1.3F
    override var portraitTranslation = Vec3d(-0.3, 1.8, 0.0)

    override var profileScale = 0.55F
    override var profileTranslation = Vec3d(0.0, 1.1, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var swim: PokemonPose
    lateinit var float: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var watersleep: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("tentacruel", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("tentacruel", "blink")}
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacruel", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacruel", "ground_idle")
                //bedrock("tentacruel", "ground_walk")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacruel", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("tentacruel", "water_swim")
            )
        )

        sleep = registerPose(
                poseName = "sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isTouchingWater },
                idleAnimations = arrayOf(bedrock("tentacruel", "sleep"))
        )

        watersleep = registerPose(
                poseName = "watersleep",
                poseType = PoseType.SLEEP,
                condition = { it.isTouchingWater },
                idleAnimations = arrayOf(bedrock("tentacruel", "water_sleep"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("tentacruel", "faint") else null
}