/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.ai

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.pokemon.ai.WalkBehaviour
import com.cobblemon.mod.common.util.asExpressionLike
import net.minecraft.network.PacketByteBuf

class ClientWalkBehaviour(
    val canWalk: Boolean = true,
    val avoidsLand: Boolean = false,
    var walkSpeed: ExpressionLike = "0.35".asExpressionLike()
) {
    constructor(walkBehaviour: WalkBehaviour) : this(
        walkBehaviour.canWalk,
        walkBehaviour.avoidsLand,
        walkBehaviour.walkSpeed
    )

    fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(this.canWalk)
        buffer.writeBoolean(this.avoidsLand)
        buffer.writeString(this.walkSpeed.toString())
    }

    companion object {
        fun decode(buffer: PacketByteBuf) = ClientWalkBehaviour(
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readString().asExpressionLike()
        )
    }
}