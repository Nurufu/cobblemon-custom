/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object CobblemonParticles : PlatformRegistry<Registry<ParticleType<*>>, RegistryKey<Registry<ParticleType<*>>>, ParticleType<*>>() {

    override val registry: Registry<ParticleType<*>> = Registries.PARTICLE_TYPE
    override val registryKey: RegistryKey<Registry<ParticleType<*>>> = RegistryKeys.PARTICLE_TYPE

    val SNOWSTORM_PARTICLE_TYPE = this.create("snowstorm", SnowstormParticleType())
}