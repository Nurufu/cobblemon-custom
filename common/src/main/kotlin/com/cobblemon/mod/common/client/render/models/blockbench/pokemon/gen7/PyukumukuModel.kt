/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import net.minecraft.util.math.Vec3d

class PyukumukuModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("pyukumuku")

    override val cryAnimation = CryProvider { bedrockStateful("pyukumuku", "cry") }


    override var portraitScale = 1.65F
    override var portraitTranslation = Vec3d(-0.1, -0.8, 0.0)
    override var profileScale = 1.0F
    override var profileTranslation = Vec3d(0.0, 0.2, 0.0)

//    lateinit var sleep: Pose
    lateinit var standing: Pose

    override fun registerPoses() {
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            animations = arrayOf(bedrock("pyukumuku", "sleep"))
//        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.ALL_POSES,// - PoseType.SLEEP,
            animations = emptyArray()// arrayOf(bedrock("pyukumuku", "ground_idle"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing)) bedrockStateful("pyukumuku", "faint") else null
}