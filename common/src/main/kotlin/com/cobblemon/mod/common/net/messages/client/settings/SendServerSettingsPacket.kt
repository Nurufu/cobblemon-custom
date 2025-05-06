package com.cobblemon.mod.common.net.messages.client.settings

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.starliteheart.cobbleride.common.util.rideableResource

class SendServerSettingsPacket internal constructor(
    val globalBaseSpeedModifier: Double,
    val globalLandSpeedModifier: Double,
    val globalWaterSpeedModifier: Double,
    val globalAirSpeedModifier: Double,
    val underwaterSpeedModifier: Double,
    val waterVerticalClimbSpeed: Double,
    val airVerticalClimbSpeed: Double,
    val rideSpeedLimit: Double,
    val isWaterBreathingShared: Boolean,
    val blacklistedDimensions: List<String>,
    val affectsSpeed: Boolean,
    val minStatThreshold: Int,
    val maxStatThreshold: Int,
    val minSpeedModifier: Double,
    val maxSpeedModifier: Double,
    val canSprint: Boolean,
    val rideSprintSpeed: Double,
    val canSprintOnLand: Boolean,
    val canSprintInWater: Boolean,
    val canSprintInAir: Boolean,
    val canExhaust: Boolean,
    val maxStamina: Int,
    val recoveryTime: Int,
    val recoveryDelay: Int,
    val exhaustionSpeed: Double,
    val exhaustionDuration: Double
) : NetworkPacket<SendServerSettingsPacket> {
    override val id: Identifier = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(Cobblemon.config.general.globalBaseSpeedModifier)
        buffer.writeDouble(Cobblemon.config.general.globalLandSpeedModifier)
        buffer.writeDouble(Cobblemon.config.general.globalWaterSpeedModifier)
        buffer.writeDouble(Cobblemon.config.general.globalAirSpeedModifier)
        buffer.writeDouble(Cobblemon.config.general.underwaterSpeedModifier)
        buffer.writeDouble(Cobblemon.config.general.waterVerticalClimbSpeed)
        buffer.writeDouble(Cobblemon.config.general.airVerticalClimbSpeed)
        buffer.writeDouble(Cobblemon.config.general.rideSpeedLimit)
        buffer.writeBoolean(Cobblemon.config.general.isWaterBreathingShared)
        buffer.writeCollection(Cobblemon.config.restrictions.blacklistedDimensions) { pb, value ->
            pb.writeString(
                value
            )
        }
        buffer.writeBoolean(Cobblemon.config.speedStat.affectsSpeed)
        buffer.writeInt(Cobblemon.config.speedStat.minStatThreshold)
        buffer.writeInt(Cobblemon.config.speedStat.maxStatThreshold)
        buffer.writeDouble(Cobblemon.config.speedStat.minSpeedModifier)
        buffer.writeDouble(Cobblemon.config.speedStat.maxSpeedModifier)
        buffer.writeBoolean(Cobblemon.config.sprinting.canSprint)
        buffer.writeDouble(Cobblemon.config.sprinting.rideSprintSpeed)
        buffer.writeBoolean(Cobblemon.config.sprinting.canSprintOnLand)
        buffer.writeBoolean(Cobblemon.config.sprinting.canSprintInWater)
        buffer.writeBoolean(Cobblemon.config.sprinting.canSprintInAir)
        buffer.writeBoolean(Cobblemon.config.sprinting.canExhaust)
        buffer.writeInt(Cobblemon.config.sprinting.maxStamina)
        buffer.writeInt(Cobblemon.config.sprinting.recoveryTime)
        buffer.writeInt(Cobblemon.config.sprinting.recoveryDelay)
        buffer.writeDouble(Cobblemon.config.sprinting.exhaustionSpeed)
        buffer.writeDouble(Cobblemon.config.sprinting.exhaustionDuration)
    }

    companion object {
        val ID = cobblemonResource("server_ride_settings")
        fun decode(buffer: PacketByteBuf) = SendServerSettingsPacket(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readList { it.readString() },
            buffer.readBoolean(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readDouble(),
            buffer.readDouble()
        )
    }
}