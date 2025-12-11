/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class PokedexItem(val type: PokedexTypes) : CobblemonItem(Settings().maxCount(1)) {

    override fun getMaxUseTime(stack: ItemStack): Int = 72000

    override fun use(
        world: World,
        player: PlayerEntity,
        usedHand: Hand
    ): TypedActionResult<ItemStack> {
        val itemStack = player.mainHandStack
        if (world.isClient && player is ClientPlayerEntity) {
            CobblemonClient.pokedexUsageContext.type = type
        }
        if (player !is ServerPlayerEntity) return TypedActionResult.success(itemStack)
        //Disables breaking blocks and damaging entities
        player.setCurrentHand(usedHand)
        return TypedActionResult.fail(itemStack)
    }

    override fun usageTick(
        world: World,
        user: LivingEntity,
        stack: ItemStack,
        remainingUseTicks: Int
    ) {
        if (world.isClient && user is ClientPlayerEntity) {
            val scanContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getMaxUseTime(stack) - remainingUseTicks
            scanContext.tick(user, ticksInUse, true)
        }
        super.usageTick(world, user, stack, remainingUseTicks)
    }

    override fun onStoppedUsing(
        stack: ItemStack,
        world: World,
        user: LivingEntity,
        remainingUseTicks: Int
    ) {
        if (world.isClient && user is ClientPlayerEntity) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getMaxUseTime(stack) - remainingUseTicks
            usageContext.stopUsing(user, ticksInUse)
        }

        super.onStoppedUsing(stack, world, user, remainingUseTicks)
    }
}
/*
@Environment(EnvType.CLIENT)
private fun registerInputHandlers() {
    val windowHandle = Minecraft.getInstance().window.handle

    if (!isScrollCallbackRegistered) {
        // Register scroll callback
        GLFW.glfwSetScrollCallback(windowHandle) { _, _, yOffset ->
            println("Scroll Callback Triggered: yOffset = $yOffset")

            if (yOffset != 0.0) {
                zoomLevel += yOffset * 0.05 // Smaller increment
                zoomLevel = zoomLevel.coerceIn(1.0, 4.0) // More controlled zoom range
                changeFOV(70 / zoomLevel)
            }
        }
        isScrollCallbackRegistered = true
    }

    if (!isMouseButtonCallbackRegistered) {
        // Register mouse button callback
        GLFW.glfwSetMouseButtonCallback(windowHandle) { _, button, action, _ ->
            if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
                println("Mouse Button 1 Left Pressed")
                Minecraft.getInstance().player?.let {
                    if (it.world.isClient) {
                        detectPokemon(it.world, it, Hand.MAIN_HAND)
                    }
                }
            } else if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
                println("Mouse Button 1 Left Released")
                // Implement your logic for release here
            }

            if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS) {
                println("Mouse Button 2 Right Pressed")
            } else if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_RELEASE) {
                println("Mouse Button 2 Right Released")
                inUse = false
                // Implement your logic for release here
            }
        }
        isMouseButtonCallbackRegistered = true
    }
}

private fun unregisterInputHandlers() {
    val windowHandle = Minecraft.getInstance().window.handle

    if (isScrollCallbackRegistered) {
        GLFW.glfwSetScrollCallback(windowHandle, null)?.free()
        isScrollCallbackRegistered = false
    }

    if (isMouseButtonCallbackRegistered) {
        GLFW.glfwSetMouseButtonCallback(windowHandle, null)?.free()
        isMouseButtonCallbackRegistered = false
    }
}
}
 */