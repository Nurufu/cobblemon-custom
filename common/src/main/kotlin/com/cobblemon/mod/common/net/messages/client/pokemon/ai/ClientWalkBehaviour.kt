package com.cobblemon.mod.common.net.messages.client.pokemon.ai

import com.cobblemon.mod.common.pokemon.ai.WalkBehaviour
import net.minecraft.network.PacketByteBuf

class ClientWalkBehaviour(
    val canWalk: Boolean = true,
    val avoidsLand: Boolean = false,
    var walkSpeed: Float = 0.35F
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
            buffer.readFloat()
        )
    }
}