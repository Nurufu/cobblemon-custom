package com.cobblemon.mod.common.net.messages.client.pokemon.ai

import com.cobblemon.mod.common.pokemon.ai.MoveBehaviour
import net.minecraft.network.PacketByteBuf

class ClientMoveBehaviour(
    val walk: ClientWalkBehaviour = ClientWalkBehaviour(),
    val swim: ClientSwimBehaviour = ClientSwimBehaviour(),
    val fly: ClientFlyBehaviour = ClientFlyBehaviour(),
    val stepHeight: Float = 0.6F,
    val wanderChance: Int = 120,
    val wanderSpeed: Double = 1.0,
    val canLook: Boolean = true,
    val looksAtEntities: Boolean = true
) {
    constructor(moveBehaviour: MoveBehaviour) : this(
        ClientWalkBehaviour(moveBehaviour.walk),
        ClientSwimBehaviour(moveBehaviour.swim),
        ClientFlyBehaviour(moveBehaviour.fly),
        moveBehaviour.stepHeight,
        moveBehaviour.wanderChance,
        moveBehaviour.wanderSpeed,
        moveBehaviour.canLook,
        moveBehaviour.looksAtEntities
    )

    fun encode(buffer: PacketByteBuf) {
        walk.encode(buffer)
        swim.encode(buffer)
        fly.encode(buffer)
        buffer.writeFloat(this.stepHeight)
        buffer.writeInt(this.wanderChance)
        buffer.writeDouble(this.wanderSpeed)
        buffer.writeBoolean(this.canLook)
        buffer.writeBoolean(this.looksAtEntities)
    }

    companion object {
        fun decode(buffer: PacketByteBuf) = ClientMoveBehaviour(
            ClientWalkBehaviour.decode(buffer),
            ClientSwimBehaviour.decode(buffer),
            ClientFlyBehaviour.decode(buffer),
            buffer.readFloat(),
            buffer.readInt(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readBoolean()
        )
    }
}