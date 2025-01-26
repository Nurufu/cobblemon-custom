/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound

/**
 * Representing an Ability with all its attributes
 *
 * Can be extended to allow for custom attributes (be sure to overwrite the load and save methods)
 *
 * @author Qu
 * @since January 9th, 2022
 */
open class Ability internal constructor(var template: AbilityTemplate, forced: Boolean) {

    val name: String
        get() = template.name

    val displayName: String
        get() = template.displayName

    val description: String
        get() = template.description

    /**
     * This represents the last known index of this backing ability in the species data.
     * @see [Pokemon.updateAbility].
     */
    var forced: Boolean = forced
        internal set

    /**
     * This represents the last known index of this backing ability in the species data.
     *
     * @see [Pokemon.updateAbility].
     */
    var index: Int = -1
        internal set

    /**
     * The last known priority of this ability in the species data.
     *
     * @see [Pokemon.updateAbility].
     */
    var priority = Priority.LOWEST
        internal set

    open fun saveToNBT(nbt: NbtCompound): NbtCompound {
        nbt.putString(DataKeys.POKEMON_ABILITY_NAME, name)
        nbt.putBoolean(DataKeys.POKEMON_ABILITY_FORCED, forced)
        nbt.putInt(DataKeys.POKEMON_ABILITY_INDEX, index)
        nbt.putString(DataKeys.POKEMON_ABILITY_PRIORITY, priority.name)
        return nbt
    }

    open fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_ABILITY_NAME, name)
        json.addProperty(DataKeys.POKEMON_ABILITY_FORCED, forced)
        json.addProperty(DataKeys.POKEMON_ABILITY_INDEX, index)
        json.addProperty(DataKeys.POKEMON_ABILITY_PRIORITY, priority.name)
        return json
    }

    open fun loadFromNBT(nbt: NbtCompound): Ability {
        this.template = Abilities.getOrException(nbt.getString(DataKeys.POKEMON_ABILITY_NAME))
        this.forced = nbt.getBoolean(DataKeys.POKEMON_ABILITY_FORCED)
        if (nbt.contains(DataKeys.POKEMON_ABILITY_INDEX) && nbt.contains(DataKeys.POKEMON_ABILITY_PRIORITY)) {
            this.index = nbt.getInt(DataKeys.POKEMON_ABILITY_INDEX)
            this.priority = Priority.valueOf(nbt.getString(DataKeys.POKEMON_ABILITY_PRIORITY))
        }
        return this
    }

    open fun loadFromJSON(json: JsonObject): Ability {
        this.template = Abilities.getOrException(json.get(DataKeys.POKEMON_ABILITY_NAME).asString)
        this.forced = json.get(DataKeys.POKEMON_ABILITY_FORCED)?.asBoolean ?: false
        if (json.has(DataKeys.POKEMON_ABILITY_INDEX) && json.has(DataKeys.POKEMON_ABILITY_PRIORITY)) {
            this.index = json.get(DataKeys.POKEMON_ABILITY_INDEX).asInt
            this.priority = Priority.valueOf(json.get(DataKeys.POKEMON_ABILITY_PRIORITY).asString)
        }
        return this
    }

}