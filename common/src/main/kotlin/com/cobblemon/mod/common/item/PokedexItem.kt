/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.ui.PokedexUIPacket
import com.cobblemon.mod.common.util.isLookingAt
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Box
import net.minecraft.world.World
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import org.lwjgl.opengl.GL11
import org.lwjgl.glfw.GLFW
import net.minecraft.client.render.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.block.BlockState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.hud.InGameHud
import net.minecraft.client.gui.hud.InGameOverlayRenderer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos

class PokedexItem(val type: String) : CobblemonItem(Settings()) {

    @JvmField
    var zoomLevel: Double = 1.0
    var isScanning = false
    var attackKeyHeldTicks = 0 // to help with detecting pressed and held states of the attack button

//    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
//        return 72000  // (vanilla bows use 72000 ticks -> 1 hour of hold time)
//    }

    override fun postHit(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?): Boolean {
        return !isScanning && super.postHit(stack, target, attacker)
    }

    override fun postMine(stack: ItemStack?, world: World?, state: BlockState?, pos: BlockPos?, miner: LivingEntity?): Boolean {
        return !isScanning && super.postMine(stack, world, state, pos, miner)
    }

    override fun canMine(state: BlockState?, world: World?, pos: BlockPos?, miner: PlayerEntity?): Boolean {
        return !isScanning && super.canMine(state, world, pos, miner)
    }

    override fun use(world: World, player: PlayerEntity, usedHand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(usedHand)

        if (player !is ServerPlayerEntity) return TypedActionResult.success(itemStack, world.isClient)

        player.setCurrentHand(usedHand) // Start using the item

        /*if (player.isSneaking) {
            isScanning = true
            //return TypedActionResult.consume(itemStack)
        }
        else {
            openPokdexGUI(player)
        }*/

        return TypedActionResult.fail(itemStack)


        /*// Check if the player is interacting with a Pokémon
        val entity = player.world
                .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
                .filter { player.isLookingAt(it, stepDistance = 0.1F) }
                .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {
                PokedexUIPacket(type).sendToPlayer(player)
            }
            player.playSoundToPlayer(CobblemonSounds.POKEDEX_SHOW, SoundCategory.PLAYERS, 1F, 1F)
        } else {
            inUse = true
            zoomLevel = 1.0
            changeFOV(70.0)
            player.setCurrentHand(usedHand) // Start using the item
            return TypedActionResult.consume(itemStack)
        }*/

        //return TypedActionResult.success(itemStack)
    }

    fun openPokdexGUI(player: ServerPlayerEntity) {
        // Check if the player is interacting with a Pokémon
        val entity = player.world
            .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                playSound(CobblemonSounds.POKEDEX_SCAN)
                //player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {
                PokedexUIPacket(type).sendToPlayer(player)
            }
            playSound(CobblemonSounds.POKEDEX_OPEN)
            //player.playSoundToPlayer(CobblemonSounds.POKEDEX_OPEN, SoundCategory.PLAYERS, 1F, 1F)
        }
    }

    override fun usageTick(world: World?, user: LivingEntity?, stack: ItemStack?, remainingUseTicks: Int) {
        if (user is PlayerEntity) {
            // todo if the item has been used for more than 1 second activate scanning mode
            if (getMaxUseTime(stack) - remainingUseTicks > 2) {
                // play the Scanner Open sound only once
                if (isScanning == false)
                    playSound(CobblemonSounds.POKEDEX_SCAN_OPEN)

                isScanning = true

                // todo try to make it so that the player is able to walk normal speed while in scanner mode
                //user.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 3, 1, true, false, false)) // Remove slowness effect
            }
        }

        super.usageTick(world, user, stack, remainingUseTicks)
    }

    override fun onStoppedUsing(stack: ItemStack?, world: World, entity: LivingEntity, timeLeft: Int) {
        // todo if less than a second then open GUI
        if (entity is ServerPlayerEntity && (getMaxUseTime(stack) - timeLeft) <= 2) {
            openPokdexGUI(entity)
        } else { // any other amount of time assume scanning mode was active
            isScanning = false
            zoomLevel = 1.0
            attackKeyHeldTicks = 0
            changeFOV(70.0)
            playSound(CobblemonSounds.POKEDEX_SCAN_CLOSE)

            // todo if solution is found to boost player speed during scanning mode, you might need to end it here
            //entity.removeStatusEffect(StatusEffects.SPEED) // Remove slowness effect
        }

        super.onStoppedUsing(stack, world, entity, timeLeft)
    }

    // todo idk if we ever will need this since it is only fired when maxUseTime is reached whcich for this item is currently at 3 hours
    /*override fun finishUsing(stack: ItemStack?, world: World?, entity: LivingEntity?): ItemStack? {
        *//*if (timeLeft < 20) {
            // todo if less than a second then open GUI
            openPokdexGUI(entity as ServerPlayerEntity)
        }*//*

        if (world != null) {
            if (world.isClient && entity is PlayerEntity) {
                inUse = false
                isScanning = false
                zoomLevel = 1.0
                changeFOV(70.0)
            }
        }

        super.finishUsing(stack, world, entity)
    }*/

    // todo maybe add a feature to scan objects like blocks for more info?
    /*override fun useOnBlock() {

    }*/

    fun changeFOV(fov: Double) {
        val client = MinecraftClient.getInstance()
        val oldFov = fov.toInt()
        val newFov = (fov / zoomLevel).coerceIn(30.0, 110.0).toInt()

        if (newFov != oldFov) {
            playSound(CobblemonSounds.POKEDEX_ZOOM_INCREMENT)
        }

        // logging for testing
        //println("Setting FOV to: $newFov with zoomLevel: $zoomLevel")

        client.options.fov.setValue(newFov)
    }

    @Environment(EnvType.CLIENT)
    fun renderPhotodexOverlay(drawContext: DrawContext, scale: Float) {
        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        // Texture dimensions
        val textureWidth = 345
        val textureHeight = 207

        // Calculate centered position
        val x = (screenWidth - textureWidth) / 2
        val y = (screenHeight - textureHeight) / 2

        val pokedexScannerOverlayTexture = cobblemonResource("textures/gui/pokedex/pokedex_scanner.png")

        RenderSystem.enableBlend()
        drawContext.drawTexture(pokedexScannerOverlayTexture, x, y, -90, 0.0f, 0.0f, textureWidth, textureHeight, textureWidth, textureHeight)
        RenderSystem.disableBlend()

        drawContext.fill(RenderLayer.getGuiOverlay(), 0, y + textureHeight, screenWidth, screenHeight, -90, -16777216)
        drawContext.fill(RenderLayer.getGuiOverlay(), 0, 0, screenWidth, y, -90, -16777216)
        drawContext.fill(RenderLayer.getGuiOverlay(), 0, y, x, y + textureHeight, -90, -16777216)
        drawContext.fill(RenderLayer.getGuiOverlay(), x + textureWidth, y, screenWidth, y + textureHeight, -90, -16777216)
    }

    @Environment(EnvType.CLIENT)
    fun onRenderOverlay(drawContext: DrawContext) {
        if (isScanning) {
            renderPhotodexOverlay(drawContext, 1.0f)
        }
    }

    @Environment(EnvType.CLIENT)
    fun onMouseClick() {
        if (isScanning) {
            MinecraftClient.getInstance().player?.let {
                //println("You have taken a picture")
                playSound(CobblemonSounds.POKEDEX_SNAP_PICTURE)
                detectPokemon(it.world, it, Hand.MAIN_HAND)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun onMouseHeld() {
        if (isScanning) {
            MinecraftClient.getInstance().player?.let {
                //println("You are scanning")
                playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
                detectPokemon(it.world, it, Hand.MAIN_HAND)
            }
        }
    }

    private fun detectPokemon(world: World, user: PlayerEntity, hand: Hand) {
        if (isScanning) {
            val eyePos = user.getCameraPosVec(1.0F)
            val lookVec = user.getRotationVec(1.0F)
            val maxDistance = 100.0  // Set this to how far you want the ray to go
            var closestEntity: Entity? = null
            var closestDistance = maxDistance

            // Define a large bounding box around the player
            val boundingBox = Box(
                user.x - 200.0, user.y - 200.0, user.z - 200.0,
                user.x + 200.0, user.y + 200.0, user.z + 200.0
            )

            // Get all entities within the bounding box
            val entities = user.world.getEntitiesByClass(Entity::class.java, boundingBox) { it != user }

            for (entity in entities) {
                val entityBox: Box = entity.boundingBox
                val intersection = entityBox.raycast(eyePos, eyePos.add(lookVec.multiply(maxDistance)))

                if (intersection.isPresent) {
                    val distanceToEntity = eyePos.distanceTo(intersection.get())
                    if (distanceToEntity < closestDistance) {
                        closestEntity = entity
                        closestDistance = distanceToEntity
                    }
                }
            }

            if (closestEntity != null && closestEntity is PokemonEntity) {
                user.sendMessage(Text.of("You have scanned a: ${closestEntity.pokemon.species.name}"))
            }
        }
    }

    fun playSound(soundEvent: SoundEvent) {
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }

    /*@Environment(EnvType.CLIENT)
    private fun registerInputHandlers() {
        val windowHandle = MinecraftClient.getInstance().window.handle

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
                    MinecraftClient.getInstance().player?.let {
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
        val windowHandle = MinecraftClient.getInstance().window.handle

        if (isScrollCallbackRegistered) {
            GLFW.glfwSetScrollCallback(windowHandle, null)?.free()
            isScrollCallbackRegistered = false
        }

        if (isMouseButtonCallbackRegistered) {
            GLFW.glfwSetMouseButtonCallback(windowHandle, null)?.free()
            isMouseButtonCallbackRegistered = false
        }
    }*/





    /*override fun use(world: World, player: PlayerEntity, usedHand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(usedHand)

        if (player !is ServerPlayerEntity) return TypedActionResult.success(itemStack, world.isClient)

        // Check if the player is interacting with a Pokémon
        val entity = player.world
            .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {
                PokedexUIPacket(type).sendToPlayer(player)
            }
            player.playSoundToPlayer(CobblemonSounds.POKEDEX_SHOW, SoundCategory.PLAYERS, 1F, 1F)
        }

        return TypedActionResult.success(itemStack, world.isClient)
    }*/
}