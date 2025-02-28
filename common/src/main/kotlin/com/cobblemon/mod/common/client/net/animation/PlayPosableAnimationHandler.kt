/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.animation

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolve
import net.minecraft.client.MinecraftClient

object PlayPosableAnimationHandler : ClientNetworkPacketHandler<PlayPosableAnimationPacket> {
    override fun handle(packet: PlayPosableAnimationPacket, client: MinecraftClient) {
        val world = client.world ?: return
        val entity = world.getEntityById(packet.entityId) ?: return
        if (entity is Poseable) {
            val delegate = entity.delegate
            if (delegate is PoseableEntityState<*>) {
                for (expr in packet.expressions) {
                    delegate.runtime.resolve(expr.asExpression())
                }
                delegate.addFirstAnimation(packet.animation)
            }
        }
    }
}