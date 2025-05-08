/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.mixin.invoker.ClientPlayNetworkHandlerInvoker
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.network.NetworkThreadUtils
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.math.Vec3d

abstract class SpawnExtraDataEntityPacket<T: NetworkPacket<T>, E : Entity>(private val vanillaSpawnPacket: EntitySpawnS2CPacket) : NetworkPacket<T> {
    override fun encode(buffer: PacketByteBuf) {
        this.encodeEntityData(buffer)
        this.vanillaSpawnPacket.write(buffer)
    }

    abstract fun encodeEntityData(buffer: PacketByteBuf)

    abstract fun applyData(entity: E)

    abstract fun checkType(entity: Entity): Boolean

    fun spawnAndApply(client: MinecraftClient) {
        client.execute {
            val player = client.player ?: return@execute
            val world = player.world as? ClientWorld ?: return@execute
            // This is a copy pasta of ClientPlayNetworkHandler#onEntitySpawn
            // This exists due to us needing to do everything it does except spawn the entity in the world.
            // We invoke applyData then we add the entity to the world.
            NetworkThreadUtils.forceMainThread(this.vanillaSpawnPacket, player.networkHandler, client)
            val entityType = this.vanillaSpawnPacket.entityType
            val entity = entityType.create(world) ?: return@execute
            entity.onSpawnPacket(this.vanillaSpawnPacket)
            entity.velocity = Vec3d(this.vanillaSpawnPacket.velocityX, this.vanillaSpawnPacket.velocityY, this.vanillaSpawnPacket.velocityZ)
            // Cobblemon start
            if (this.checkType(entity)) {
                this.applyData(entity as E)
            }
            // Cobblemon end
            world.addEntity(this.vanillaSpawnPacket.id, entity)
            (player.networkHandler as ClientPlayNetworkHandlerInvoker).callPlaySpawnSound(entity)
        }
    }

    companion object {
        fun decodeVanillaPacket(buffer: PacketByteBuf) = EntitySpawnS2CPacket(buffer)
    }
}