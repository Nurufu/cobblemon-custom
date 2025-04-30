/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.google.gson.JsonObject
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class CastPokeRodCriterionCondition(id: Identifier, predicate: LootContextPredicate): SimpleCriterionCondition<Boolean>(id, predicate) {
    var hasBait: Boolean = false

    override fun toJson(json: JsonObject) {
        json.addProperty("hasBait", hasBait)
    }

    override fun fromJson(json: JsonObject) {
        hasBait = json.get("hasBait").asBoolean
    }

    override fun matches(player: ServerPlayerEntity, context: Boolean): Boolean {
        return hasBait == context
    }
}