package com.cobblemon.mod.common.net.messages.client.pokemon.ai

import com.cobblemon.mod.common.pokemon.ai.FlyBehaviour
import net.minecraft.network.PacketByteBuf

class ClientFlyBehaviour(
    val canFly: Boolean = false,
    val flySpeedHorizontal: Float = 0.3F
) {
    constructor(flyBehaviour: FlyBehaviour) : this(
        flyBehaviour.canFly,
        flyBehaviour.flySpeedHorizontal
    )

    fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(this.canFly)
        buffer.writeString(this.flySpeedHorizontal.toString())
    }

    companion object {
        fun decode(buffer: PacketByteBuf) = ClientFlyBehaviour(
            buffer.readBoolean(),
            buffer.readFloat()
        )
    }
}