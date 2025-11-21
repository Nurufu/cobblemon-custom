/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import org.joml.Vector4f

class PokemonItem : CobblemonItem(Settings().maxCount(1)) {

    override fun getName(stack: ItemStack): Text = this.species(stack)?.translatedName ?: super.getName(stack)

    fun asPokemon(stack: ItemStack): Pokemon? {
        val species = this.species(stack) ?: return null
        val aspects = this.aspects(stack) ?: setOf()
        return Pokemon().apply {
            this.species = species
            this.forcedAspects = aspects
        }
    }

    fun getSpeciesAndAspects(stack: ItemStack): Pair<Species, Set<String>>? {
        return (species(stack) ?: return null) to (aspects(stack) ?: setOf())
    }

    fun asRenderablePokemon(stack: ItemStack): RenderablePokemon? = this.asPokemon(stack)?.asRenderablePokemon()

    private fun species(stack: ItemStack): Species? {
        val nbt = stack.nbt ?: return null
        if (!nbt.contains(DataKeys.POKEMON_ITEM_SPECIES)) {
            return null
        }
        return try {
            val identifier = Identifier(nbt.getString(DataKeys.POKEMON_ITEM_SPECIES))
            PokemonSpecies.getByIdentifier(identifier)
        } catch (_: InvalidIdentifierException) {
            null
        }
    }

    private fun aspects(stack: ItemStack): Set<String>? {
        val nbt = stack.nbt ?: return null
        if (!nbt.contains(DataKeys.POKEMON_ITEM_ASPECTS)) {
            return null
        }
        return nbt.getList(DataKeys.POKEMON_ITEM_ASPECTS, NbtElement.STRING_TYPE.toInt())
            .map { it.asString() }
            .toSet()
    }

    fun tint(stack: ItemStack): Vector4f {
        val nbt = stack.nbt ?: return Vector4f(1.0F, 1.0F, 1.0F, 1.0F)

        val red = if (nbt.contains(DataKeys.POKEMON_ITEM_TINT_RED)) {
            nbt.getFloat(DataKeys.POKEMON_ITEM_TINT_RED)
        } else {
            1.0F
        }

        val green = if (nbt.contains(DataKeys.POKEMON_ITEM_TINT_GREEN)) {
            nbt.getFloat(DataKeys.POKEMON_ITEM_TINT_GREEN)
        } else {
            1.0F
        }

        val blue = if (nbt.contains(DataKeys.POKEMON_ITEM_TINT_BLUE)) {
            nbt.getFloat(DataKeys.POKEMON_ITEM_TINT_BLUE)
        } else {
            1.0F
        }

        val alpha = if (nbt.contains(DataKeys.POKEMON_ITEM_TINT_ALPHA)) {
            nbt.getFloat(DataKeys.POKEMON_ITEM_TINT_ALPHA)
        } else {
            1.0F
        }
        return Vector4f(red, green, blue, alpha)
    }

    companion object {

        @JvmOverloads
        @JvmStatic
        fun from(pokemon: Pokemon, count: Int = 1, tint: Vector4f? = null): ItemStack = from(pokemon.species, pokemon.aspects, count, tint)

        @JvmOverloads
        @JvmStatic
        fun from(properties: PokemonProperties, count: Int = 1, tint: Vector4f? = null): ItemStack = from(properties.create(), count, tint)

        @JvmOverloads
        @JvmStatic
        fun from(species: Species, vararg aspects: String, count: Int = 1, tint: Vector4f? = null): ItemStack = from(species, aspects.toSet(), count, tint)

        @JvmStatic
        fun from(species: Species, aspects: Set<String>, count: Int = 1, tint: Vector4f? = null): ItemStack {
            val stack = ItemStack(CobblemonItems.POKEMON_MODEL, count)
            stack.orCreateNbt.apply {
                putString(DataKeys.POKEMON_ITEM_SPECIES, species.resourceIdentifier.toString())
                val list = NbtList()
                aspects.forEach { aspect ->
                    list.add(NbtString.of(aspect))
                }
                put(DataKeys.POKEMON_ITEM_ASPECTS, list)

                if (tint != null) {
                    putFloat(DataKeys.POKEMON_ITEM_TINT_RED, tint.x)
                    putFloat(DataKeys.POKEMON_ITEM_TINT_GREEN, tint.y)
                    putFloat(DataKeys.POKEMON_ITEM_TINT_BLUE, tint.z)
                    putFloat(DataKeys.POKEMON_ITEM_TINT_ALPHA, tint.w)
                }
            }
            return stack
        }

    }

}