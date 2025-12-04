/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.util.toJsonArray
import com.google.gson.JsonObject
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

open class BattleCountableCriterionTrigger(identifier: Identifier, criterionClass: Class<BattleCountableCriterionCondition>) : SimpleCriterionTrigger<BattleCountableContext, BattleCountableCriterionCondition>(identifier, criterionClass) {

}

class BattleCountableCriterionCondition(id: Identifier, predicate: LootContextPredicate) : CountableCriterionCondition<BattleCountableContext>(id, predicate) {

    private var battleTypes = mutableListOf("any")

    override fun fromJson(json: JsonObject) {
        super.fromJson(json)
        if(!json.get("battle_types").isJsonNull) {
            battleTypes.clear()
            json.get("battle_types").asJsonArray.asList().forEach() {
                battleTypes.add(it.asString)
            }
        }
    }

    override fun toJson(json: JsonObject) {
        super.toJson(json)
        json.add("battle_types", battleTypes.toJsonArray())
    }

    override fun matches(player: ServerPlayerEntity, context: BattleCountableContext): Boolean {
        var typeCheck = false
        val advancementData = Cobblemon.playerDataManager.getGenericData(player).advancementData
        if (battleTypes.isEmpty() || battleTypes.contains("any")) {
            typeCheck = true
        }
        if (battleTypes.contains("pvp")) {
            typeCheck = context.battle.isPvP
            context.times = advancementData.totalPvPBattleVictoryCount
        }
        if (battleTypes.contains("pvw")) {
            typeCheck = context.battle.isPvW
            context.times = advancementData.totalPvWBattleVictoryCount
        }
        if (battleTypes.contains("pvn")) {
            typeCheck = context.battle.isPvN
            context.times = advancementData.totalPvWBattleVictoryCount
        }
        if (battleTypes.size > 1) {
            context.times = advancementData.totalBattleVictoryCount
        }
        return context.times >= count && typeCheck
    }
}

open class BattleCountableContext(times : Int, var battle : PokemonBattle) : CountableContext(times)