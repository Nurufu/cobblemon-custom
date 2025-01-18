/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.CobblemonNetwork.sendPacketToServer
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.keybind.CobblemonBlockingKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity

object PartySendBinding : CobblemonBlockingKeyBinding(
    "key.cobblemon.throwpartypokemon",
    InputUtil.Type.KEYSYM,
    InputUtil.GLFW_KEY_R,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    var canApplyChange = true
    var secondsSinceActioned = 0F

    fun actioned() {
        canApplyChange = false
        secondsSinceActioned = 0F
        wasDown = false
    }

    fun canAction() = canApplyChange

    override fun onTick() {
        if (secondsSinceActioned < 100) {
            secondsSinceActioned += MinecraftClient.getInstance().tickDelta
        }

        super.onTick()
    }

    override fun onRelease() {
        wasDown = false

        if (!canAction()) {
            canApplyChange = true
            return
        }

        canApplyChange = true
        val player = MinecraftClient.getInstance().player ?: return

        if (player.isSpectator) return

        val battle = CobblemonClient.battle
        if (battle != null) {
            battle.minimised = !battle.minimised
            if (!battle.minimised) {
                MinecraftClient.getInstance().setScreen(BattleGUI())
            }
            return
        }

        if (CobblemonClient.storage.selectedSlot != -1 && MinecraftClient.getInstance().currentScreen == null) {
            val pokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (pokemon != null && pokemon.currentHealth > 0) {
                val targetEntity = player.traceFirstEntityCollision(entityClass = LivingEntity::class.java, ignoreEntity = player)
                if (targetEntity == null || (targetEntity is PokemonEntity && targetEntity.ownerUuid == player.uuid)) {
                    sendPacketToServer(SendOutPokemonPacket(CobblemonClient.storage.selectedSlot))
                }
                else {
                    processEntityTarget(player, pokemon, targetEntity)
                }
            }
        }
    }

    private fun processEntityTarget(player: ClientPlayerEntity, pokemon: Pokemon, entity: LivingEntity) {
        when (entity) {
            is PlayerEntity -> {
                //This sends a packet to the server with the id of the player
                //The server sends a packet back that opens the player interaction menu with the proper options
                sendPacketToServer(RequestPlayerInteractionsPacket(entity.uuid, entity.id, pokemon.uuid))
            }
            is PokemonEntity -> {
                if (!entity.canBattle(player)) return
                sendPacketToServer(BattleChallengePacket(entity.id, pokemon.uuid, 0))
            }
        }
    }

    override fun onPress() {
    }
}