/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.starter.StarterHandler
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import net.minecraft.server.network.ServerPlayerEntity

open class CobblemonStarterHandler : StarterHandler {

    override fun getStarterList(player: ServerPlayerEntity) = Cobblemon.starterConfig.starters

    override fun handleJoin(player: ServerPlayerEntity) {}

    override fun requestStarterChoice(player: ServerPlayerEntity) {
        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        if (playerData.starterSelected) {
            playerData.sendToPlayer(player)
            player.sendMessage(lang("ui.starter.alreadyselected").red(), true)
        } else if (playerData.starterLocked) {
            player.sendMessage(lang("ui.starter.cannotchoose").red(), true)
        } else {
            OpenStarterUIPacket(getStarterList(player)).sendToPlayer(player)
            playerData.starterPrompted = true
            Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreType.GENERAL)
        }
    }

    override fun chooseStarter(player: ServerPlayerEntity, categoryName: String, index: Int) {
        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        if (playerData.starterSelected) {
            return player.sendMessage(lang("ui.starter.alreadyselected").red(), true)
        } else if (playerData.starterLocked) {
            return player.sendMessage(lang("ui.starter.cannotchoose").red(), true)
        }

        val category = getStarterList(player).find { it.name == categoryName } ?: return

        if (index > category.pokemon.size) {
            return
        }

        val properties = category.pokemon[index]
        val pokemon = properties.create()

        CobblemonEvents.STARTER_CHOSEN.postThen(StarterChosenEvent(player, properties, pokemon)) {
            Cobblemon.storage.getParty(player).add(
                it.pokemon.also {
                    playerData.starterSelected = true
                    playerData.starterUUID = it.uuid
                    if (player.world.gameRules.getBoolean(CobblemonGameRules.SHINY_STARTERS)) { pokemon.shiny = true }
                }
            )
            CobblemonCriteria.PICK_STARTER.trigger(player, pokemon)
            Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreType.GENERAL)
            playerData.sendToPlayer(player)
        }
    }

}