package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.gui.RideStaminaOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import com.cobblemon.mod.common.Cobblemon;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InGameHud.class)
public abstract class GuiMixin {
    /**
     * We hide the experience bar while rendering the stamina bar, to avoid funny overlapping sprites.
     */
    @Inject(
            method = "renderExperienceBar",
            at = @At("HEAD")
    )
    private void hideExperienceBarForRidePokemon(DrawContext context, int x, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        assert MinecraftClient.getInstance().player != null;
        if (Cobblemon.implementation.shouldRenderStaminaBar(player))
        {
            RideStaminaOverlay.render(context);
            return;
        };
    }
}
