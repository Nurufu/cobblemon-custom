package com.cobblemon.mod.common.net.messages.client.pokemon.ai

import com.cobblemon.mod.common.pokemon.ai.SwimBehaviour
import net.minecraft.network.PacketByteBuf

class ClientSwimBehaviour(
    val avoidsWater: Boolean = false,
    val hurtByLava: Boolean = true,
    val canSwimInWater: Boolean = true,
    val canSwimInLava: Boolean = true,
    val swimSpeed: Float = 0.3F,
    val canBreatheUnderwater: Boolean = false,
    val canBreatheUnderlava: Boolean = false,
    val canWalkOnWater: Boolean = false,
    val canWalkOnLava: Boolean = false
) {
    constructor(swimBehaviour: SwimBehaviour) : this(
        swimBehaviour.avoidsWater,
        swimBehaviour.hurtByLava,
        swimBehaviour.canSwimInWater,
        swimBehaviour.canSwimInLava,
        swimBehaviour.swimSpeed,
        swimBehaviour.canBreatheUnderwater,
        swimBehaviour.canBreatheUnderlava,
        swimBehaviour.canWalkOnWater,
        swimBehaviour.canWalkOnLava
    )

    fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(this.avoidsWater)
        buffer.writeBoolean(this.hurtByLava)
        buffer.writeBoolean(this.canSwimInWater)
        buffer.writeBoolean(this.canSwimInLava)
        buffer.writeString(this.swimSpeed.toString())
        buffer.writeBoolean(this.canBreatheUnderwater)
        buffer.writeBoolean(this.canBreatheUnderlava)
        buffer.writeBoolean(this.canWalkOnWater)
        buffer.writeBoolean(this.canWalkOnLava)
    }

    companion object {
        fun decode(buffer: PacketByteBuf) = ClientSwimBehaviour(
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readFloat(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean()
        )
    }
}