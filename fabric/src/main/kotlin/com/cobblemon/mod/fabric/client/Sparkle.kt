/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.client

import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType

class Sparkle {
    companion object
    class Factory(spriteProvider: SpriteProvider) : EndRodParticle.Factory(spriteProvider) {
        override fun createParticle(
            defaultParticleType: DefaultParticleType?,
            clientWorld: ClientWorld?,
            d: Double,
            e: Double,
            f: Double,
            g: Double,
            h: Double,
            i: Double
        ): Particle? {
            return super.createParticle(defaultParticleType, clientWorld, d, e, f, g, h, i)
        }
    }
}