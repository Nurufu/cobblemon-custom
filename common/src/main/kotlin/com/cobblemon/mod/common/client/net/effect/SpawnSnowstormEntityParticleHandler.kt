/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity

object SpawnSnowstormEntityParticleHandler : ClientNetworkPacketHandler<SpawnSnowstormEntityParticlePacket> {
    override fun handle(packet: SpawnSnowstormEntityParticlePacket, client: MinecraftClient) {
        val world = MinecraftClient.getInstance().world ?: return
        val effect = BedrockParticleEffectRepository.getEffect(packet.effectId) ?: return
        val entity = world.getEntityById(packet.entityId) as? PosableEntity ?: return
        entity as Entity
        val state = entity.delegate as PosableState
        val locator = packet.locator.firstOrNull() {state.locatorStates[it] != null} ?: return
        val matrixWrapper = state.locatorStates[locator]!!

        val particleRuntime = MoLangRuntime().setup().setupClient()
        particleRuntime.environment.query.addFunction("entity") { state.runtime.environment.query }

        val storm = ParticleStorm(
            effect = effect,
            matrixWrapper = matrixWrapper,
            world = world,
            runtime = particleRuntime,
            sourceVelocity = { entity.velocity },
            sourceAlive = { !entity.isRemoved },
            sourceVisible = { !entity.isInvisible },
            entity = entity
        )

        storm.spawn()
    }
}