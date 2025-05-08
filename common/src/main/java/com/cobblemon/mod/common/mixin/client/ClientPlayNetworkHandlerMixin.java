package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds;
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /**
     * This redirect is required to change the translated key passed to the player. Ride Pokemon use a different keybind, so we have to make sure that they are receiving the correct input!
     */
    @Redirect(
            method = "onEntityPassengersSet", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;")
    )
    private MutableText showDismountMessage(String key, Object[] args, @Local(ordinal = 0) Entity entity) {
        return (entity instanceof RideablePokemonEntity) ? Text.translatable(
                "cobblemon.mount.onboard",
                CobblemonKeyBinds.INSTANCE.getSEND_OUT_POKEMON().getBoundKeyLocalizedText(),
                entity.getDisplayName()
        ) : Text.translatable(key, args);
    }
}