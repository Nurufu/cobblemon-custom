/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffectRegistry;
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /**
     * Minecraft is weird in that all entities seem(?) to move at the same speed in water, because movement speed isn't really factored into the way they move in water. The below inject makes sure that a Ride Pokemon's speed in liquid will be properly applied.
     */
    @Redirect(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"
            )
    )
    private void modifyFluidSpeed(LivingEntity instance, float v, Vec3d vec3) {
        if (instance instanceof RideablePokemonEntity) {
            // Since fluid movement is likely balanced around players, base player movement speed is used as a basis
            float speedRatio = instance.getMovementSpeed() / 0.1F;
            instance.updateVelocity(v * speedRatio, vec3);
        } else {
            instance.updateVelocity(v, vec3);
        }
    }


    @Inject(method = "onStatusEffectRemoved", at = @At(value = "TAIL"))
    private void cobblemon$onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        final LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity) {
            ShoulderEffectRegistry.INSTANCE.onEffectEnd((ServerPlayerEntity) entity);
        }
    }

}
