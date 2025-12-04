/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.api.pokedex.PokedexRecord
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Manages all types of [InstancedPlayerData]
 * Essentially, we have multiple types of data attached to a player that we might want to save in different formats/files/folders/dbs
 * To add a new type, add a new type in [PlayerInstancedDataStoreType],
 * create corresponding [InstancedPlayerData] and [ClientInstancedPlayerData] classes
 * Then add the type associated with a [PlayerInstancedDataFactory] (probably a [CachedPlayerDataStoreFactory]
 *
 * @author Apion
 * @since February 21, 2024
 */

class PlayerInstancedDataStoreManager {
    val factories = mutableMapOf<PlayerInstancedDataStoreType, PlayerInstancedDataFactory<*>>()
    val saveTasks = mutableMapOf<PlayerInstancedDataStoreType, ScheduledTask>()
    fun setFactory(factory: PlayerInstancedDataFactory<*>, dataType: PlayerInstancedDataStoreType) {
        factories[dataType] = factory
    }


    fun setup(server: MinecraftServer) {
        factories.values.forEach {
            it.setup(server)
        }
        //Should put this somewhere else
        saveTasks[PlayerInstancedDataStoreType.GENERAL] = ScheduledTask.Builder()
            .execute { saveAllOfOneType(PlayerInstancedDataStoreType.GENERAL) }
            .delay(30f)
            .interval(120f)
            .infiniteIterations()
            .tracker(ServerTaskTracker)
            .build()

        saveTasks[PlayerInstancedDataStoreType.POKEDEX] = ScheduledTask.Builder()
            .execute { saveAllOfOneType(PlayerInstancedDataStoreType.POKEDEX) }
            .delay(30f)
            .interval(120f)
            .infiniteIterations()
            .tracker(ServerTaskTracker)
            .build()
    }

    fun get(player: PlayerEntity, dataType: PlayerInstancedDataStoreType): InstancedPlayerData {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.getForPlayer(player)
    }
    fun saveAllOfOneType(dataType: PlayerInstancedDataStoreType) {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.saveAll()
    }

    fun saveSingle(playerData: InstancedPlayerData, dataType: PlayerInstancedDataStoreType) {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.saveSingle(playerData.uuid)
    }

    fun onPlayerDisconnect(player: ServerPlayerEntity) {
        factories.values.forEach {
            it.onPlayerDisconnect(player)
        }
    }

    fun syncAllToPlayer(player: ServerPlayerEntity) {
        factories.values.forEach {
            it.sendToPlayer(player)
        }
    }

    fun saveAllStores() {
        factories.values.forEach {
            it.saveAll()
        }
    }

    fun getGenericData(player: ServerPlayerEntity): GeneralPlayerData {
        return get(player, PlayerInstancedDataStoreType.GENERAL) as GeneralPlayerData
    }

    fun getPokedexData(player: ServerPlayerEntity): PokedexRecord {
        return get(player, PlayerInstancedDataStoreType.POKEDEX) as PokedexRecord
    }
}