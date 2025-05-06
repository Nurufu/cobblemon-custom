package com.cobblemon.mod.common.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    boolean getJumping();
}