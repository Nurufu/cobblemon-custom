/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.fossil.FossilModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.JsonPokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.entity.Entity

/**
 * Repository for models referenced by fossils, to render in the tank.
 */
object FossilModelRepository : VaryingModelRepository<Entity, FossilModel>() {
    override val title = "Fossil Pokémon"
    override val type = "fossils"
    override val variationDirectories: List<String> = listOf("bedrock/$type/variations")
    override val poserDirectories: List<String> = listOf("bedrock/$type/posers")
    override val modelDirectories: List<String> = listOf("bedrock/$type/models")
    override val animationDirectories: List<String> = listOf("bedrock/$type/animations")
    override val fallback = cobblemonResource("substitute")
    override val isForLivingEntityRenderer = false

    private val gson = GsonBuilder().create()
    override fun loadJsonPoser(json: String): (Bone) -> FossilModel {
        val jsonObject = gson.fromJson<JsonObject>(json)
        val animations = jsonObject.getAsJsonArray("animations")
        val maxScale = jsonObject.get("maxScale")?.asFloat ?: 1F
        val yTranslation = jsonObject.get("yTranslation")?.asFloat ?: 0F
        val yGrowthPoint = jsonObject.get("yGrowthPoint")?.asFloat ?: 0F

        return { bone ->
            val model = FossilModel(bone)
            model.maxScale = maxScale
            model.yTranslation = yTranslation
            model.yGrowthPoint = yGrowthPoint
            // Refactor this bullshit to not mention pokemon at all, it should be common to anything using animation factories.
            // Even better: move to molang functions, this is ass
            model.tankAnimations = animations.mapNotNull {
                val animString = it.asString
                val anim = animString.substringBefore("(")
                if (JsonPokemonPoseableModel.ANIMATION_FACTORIES.contains(anim)) {
                    return@mapNotNull JsonPokemonPoseableModel.ANIMATION_FACTORIES[anim]!!.stateless(model, animString)
                } else {
                    null
                }
            }.toTypedArray()

            // borrowed code from JsonPokemonPoseableModel's PoseAdapter Deserializer
            val tankQuirks = (jsonObject.get("quirks")?.asJsonArray ?: JsonArray()).map { json ->
                json as JsonObject
                val quirkAnimations: (state: PosableState<Entity>) -> List<StatefulAnimation<Entity, *>> = { _ ->
                    (json.get("animations")?.asJsonArray ?: JsonArray()).mapNotNull { animJson ->
                        val animString = animJson.asString

                        val anim = animString.substringBefore("(")

                        val animation = if (JsonPokemonPoseableModel.ANIMATION_FACTORIES.contains(anim)) {
                            JsonPokemonPoseableModel.ANIMATION_FACTORIES[anim]?.stateful(model, animString)
                        } else {
                            null
                        }
                        animation
                    }
                }
                val loopTimes = json.get("loopTimes")?.asInt ?: 1
                val minSeconds = json.get("minSeconds")?.asFloat ?: 8F
                val maxSeconds = json.get("maxSeconds")?.asFloat ?: 30F

                model.quirkMultiple(
                    secondsBetweenOccurrences = minSeconds to maxSeconds,
                    condition = { true },
                    loopTimes = 1..loopTimes,
                    animations = quirkAnimations
                )
            }
            model.tankQuirks = tankQuirks.toTypedArray()

            model
        }
    }

    override fun registerInBuiltPosers() {}
}