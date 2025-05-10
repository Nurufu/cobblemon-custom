/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class VespiquenModel (root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("vespiquen")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3d(-0.14, 0.8, 0.0)

    override var profileScale = 0.75F
    override var profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var hover: PokemonPose
    lateinit var fly: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("vespiquen", "cry") }

    override fun registerPoses() {
        val blink1 = quirk { bedrockStateful("vespiquen", "blink") }
        val wingsleep = quirk { bedrockStateful("vespiquen", "sleep_flap") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(wingsleep),
            idleAnimations = arrayOf(bedrock("vespiquen", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink1),
            idleAnimations = arrayOf(
                bedrock("vespiquen", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseTypes = PoseType.UI_POSES + PoseType.HOVER + PoseType.FLOAT,
            quirks = arrayOf(blink1),
            idleAnimations = arrayOf(
                bedrock("vespiquen", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM, PoseType.WALK),
            quirks = arrayOf(blink1),
            idleAnimations = arrayOf(
                bedrock("vespiquen", "air_fly")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>
    ) = if (state.isPosedIn(hover, fly, sleep, standing)) bedrockStateful("vespiquen", "faint") else null
}