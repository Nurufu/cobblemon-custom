/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric

import com.cobblemon.mod.common.Cobblemon
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.DefaultParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


class FabricBootstrap : ModInitializer {

    override fun onInitialize() {
        CobblemonFabric.initialize()
    }

}