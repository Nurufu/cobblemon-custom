package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.pokemon.ai.ClientMoveBehaviour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class)
public abstract class LocalPlayerMixin {
    @Unique
    @Final
    protected MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    public Input input;

    /**
     * This inject is necessary to pass client inputs to the Ride Pokemon. This ensures that we can operate the Pokemon on a mostly client-side level. Only isRideAscending is not set here, but we can get this from the player's jumping state.
     */
    @Inject(
            method = "tickMovement",
            at = @At(value = "TAIL")
    )
    private void setRidePokemonInputs(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof PokemonEntity pokemon && pokemon.isLogicalSideForUpdatingMovement()) {
            pokemon.setRideDescending(this.input.sneaking);
            pokemon.setRideSprinting(this.client.options.sprintKey.isPressed());
        }
    }
}