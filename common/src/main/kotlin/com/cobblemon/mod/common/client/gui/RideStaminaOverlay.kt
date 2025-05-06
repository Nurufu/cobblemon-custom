package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.rideableResource
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.hud.InGameHud

class RideStaminaOverlay : InGameHud(MinecraftClient.getInstance(), MinecraftClient.getInstance().itemRenderer) {

    companion object {
        @JvmStatic
        fun render(context: DrawContext) {
            val minecraft = MinecraftClient.getInstance()

            // Do not render if any other screen is open, or if gui should be hidden
            if (minecraft.currentScreen != null || minecraft.options.hudHidden) {
                return
            }

            // Do not render if we are not riding a Rideable Pokemon
            // Also do not render if either sprinting or exhaustion is disabled or if the rider is not controlling it
            val player = minecraft.player
            if (player == null || !Cobblemon.implementation.shouldRenderStaminaBar(player)) {
                return
            }

            // No dex
            // Do not render if we're using the Pokedex's scanning mode
//            if (pokedexUsageContext.scanningGuiOpen) {
//                return
//            }

            val ridePokemon = player.vehicle as PokemonEntity

            minecraft.profiler.push("staminaBar")
            val f = ridePokemon.sprintStaminaScale
            val j = (f * 183.0f).toInt()
            val k = context.scaledWindowHeight - 32 + 3
            val l = context.scaledWindowHeight / 2 - 91
            RenderSystem.enableBlend()
            context.drawTexture(rideableResource("hud/stamina_bar_background"), l, k, 0, 0, 182, 5)
            if (ridePokemon.isExhausted) {
                context.drawTexture(rideableResource("hud/stamina_bar_cooldown"), l, k, 0, 0, 182, 5)
            } else if (j > 0) {
                context.drawTexture(
                    rideableResource("hud/stamina_bar_progress"),
                    182, 5, 0F, 0F, l, k, j, 5
                )
            }

            RenderSystem.disableBlend()
            minecraft.profiler.pop()
        }
    }

}