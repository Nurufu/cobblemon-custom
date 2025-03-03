/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isUuid
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d


class FalinksModel (root: ModelPart) : PokemonPoseableModel(){
    override val rootPart = root.registerChildWithAllChildren("falinks")

    override var portraitScale = 1.9F
    override var portraitTranslation = Vec3d(-1.0, -1.2, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3d(0.1, 0.9, 0.0)

    lateinit var standing: PokemonPose
    lateinit var battlestanding: PokemonPose
    lateinit var battlestanding2: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var uipose: PokemonPose
    lateinit var sleep: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("falinks", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("falinks", "blink") }
        val blink2 = quirk { bedrockStateful("falinks", "blink2") }
        val blink3 = quirk { bedrockStateful("falinks", "blink3") }
        val blink4 = quirk { bedrockStateful("falinks", "blink4") }
        val blink5 = quirk { bedrockStateful("falinks", "blink5") }
        val blink6 = quirk { bedrockStateful("falinks", "blink6") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("falinks", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                bedrock("falinks", "ground_idle")
            )
        )

        battlestanding2 = registerPose(
            poseName = "battlestanding2",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { it.isBattling && (it.uuid.mostSignificantBits % 2).toInt() == 0 },
            idleAnimations = arrayOf(
                bedrock("falinks", "battle_idle2")
            )
        )

        battlestanding = registerPose(
            poseName = "battlestanding",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                bedrock("falinks", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            condition = { !it.isBattling },
            idleAnimations = arrayOf(
                bedrock("falinks", "ground_walk")
            )
        )

        uipose = registerPose(
            poseName = "uipose",
            poseTypes = PoseType.UI_POSES,
            quirks = arrayOf(blink, blink2, blink3, blink4, blink5, blink6),
            idleAnimations = arrayOf(
                bedrock("falinks", "summary_idle")
            )
        )
    }
}