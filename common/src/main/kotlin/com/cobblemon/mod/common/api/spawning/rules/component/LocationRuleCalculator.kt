/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asDimensionTypeMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asWorldMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveBoolean
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

/**
 * An early rule that filters possible locations that would go into a [SpawningContext].
 *
 * @author Hiroku
 * @since October 2nd, 2023
 */
class LocationRuleCalculator : SpawnRuleComponent {
    @Transient
    val runtime = MoLangRuntime().setup()
    @Transient
    private val reusableX = DoubleValue(0.0)
    @Transient
    private val reusableY = DoubleValue(0.0)
    @Transient
    private val reusableZ = DoubleValue(0.0)
    @Transient
    private val reusableContext = StringValue("")
    @Transient
    private lateinit var reusableWorldValue: ObjectValue<RegistryEntry<World>>
    @Transient
    private lateinit var reusableDimensionTypeValue: ObjectValue<RegistryEntry<DimensionType>>

    val allow = "true".asExpression()

    override fun isAllowedPosition(
        world: ServerWorld,
        pos: BlockPos,
        contextCalculator: SpawningContextCalculator<*, *>
    ): Boolean {
        reusableX.value = pos.x.toDouble()
        reusableY.value = pos.y.toDouble()
        reusableZ.value = pos.z.toDouble()
        reusableContext.value = contextCalculator.name

        if (!this::reusableWorldValue.isInitialized) {
            reusableWorldValue = world.registryManager.get(RegistryKeys.WORLD).getEntry(world.registryKey).get().asWorldMoLangValue()
        } else {
            reusableWorldValue.obj = world.registryManager.get(RegistryKeys.WORLD).getEntry(world.registryKey).get()
        }

        if (!this::reusableDimensionTypeValue.isInitialized) {
            reusableDimensionTypeValue = world.dimensionEntry.asDimensionTypeMoLangValue()
        } else {
            reusableDimensionTypeValue.obj = world.dimensionEntry
        }

        runtime.environment.setSimpleVariable("x", reusableX)
        runtime.environment.setSimpleVariable("y", reusableY)
        runtime.environment.setSimpleVariable("z", reusableZ)
        runtime.environment.setSimpleVariable("context", reusableContext)
        runtime.environment.setSimpleVariable("world", reusableWorldValue)
        runtime.environment.setSimpleVariable("dimension_type", reusableDimensionTypeValue)
        return runtime.resolveBoolean(allow)
    }
}