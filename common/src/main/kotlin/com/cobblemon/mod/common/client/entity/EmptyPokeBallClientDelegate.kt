/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.entity.EntitySideDelegate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.client.render.pokeball.PokeBallPoseableState
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity.CaptureState
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity.CaptureState.NOT
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.entity.data.TrackedData

class EmptyPokeBallClientDelegate : PokeBallPoseableState(), EntitySideDelegate<EmptyPokeBallEntity> {
    override val stateEmitter: SettableObservable<CaptureState> = SettableObservable(NOT)
    override val shakeEmitter = SimpleObservable<Unit>()

    lateinit var currentEntity: EmptyPokeBallEntity
    override val schedulingTracker
        get() = getEntity().schedulingTracker

    override fun getEntity() = currentEntity

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
    }

    override fun initialize(entity: EmptyPokeBallEntity) {
        this.currentEntity = entity
        age = entity.age
        initSubscriptions()
        this.runtime.environment.getQueryStruct().addFunctions(mapOf(
            "pokeball_type" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokeBall.name.toString())
            }
        ))
    }

    override fun tick(entity: EmptyPokeBallEntity) {
        super.tick(entity)
        updateLocatorPosition(entity.pos)
        incrementAge(entity)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        when (data) {
            EmptyPokeBallEntity.CAPTURE_STATE -> stateEmitter.set(currentEntity.captureState)
            EmptyPokeBallEntity.SHAKE -> shakeEmitter.emit(Unit)
            //EmptyPokeBallEntity.ASPECTS -> currentAspects = currentEntity.aspects
        }
    }
}