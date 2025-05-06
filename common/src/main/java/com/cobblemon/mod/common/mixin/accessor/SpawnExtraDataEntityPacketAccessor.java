package com.cobblemon.mod.common.mixin.accessor;

import com.cobblemon.mod.common.net.messages.client.spawn.SpawnExtraDataEntityPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SpawnExtraDataEntityPacket.class)
public interface SpawnExtraDataEntityPacketAccessor {
    @Accessor
    EntitySpawnS2CPacket getVanillaSpawnPacket();
}