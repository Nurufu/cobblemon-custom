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