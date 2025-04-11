/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.interaction.ExperienceCandyUseEvent
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.experience.CandyExperienceSource
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.interactive.CandyItem.Calculator
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * An experience candy item.
 * See Bulbapedias [RareCandy](https://bulbapedia.bulbagarden.net/wiki/Rare_Candy) & [ExperienceCandy](https://bulbapedia.bulbagarden.net/wiki/Exp._Candy) articles.
 *
 * @property calculator The [Calculator] that will resolve the amount of experience to give.
 *
 * @author Licious
 * @since May 5th, 2022
 */
class CandyItem(val calculator: Calculator) : CobblemonItem(Settings()), PokemonSelectingItem {
    override val bagItem = null

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user is ServerPlayerEntity) {
            return use(user, user.getStackInHand(hand))
        }
        return TypedActionResult.success(user.getStackInHand(hand))
    }

    override fun applyToPokemon(player: ServerPlayerEntity, stack: ItemStack, pokemon: Pokemon): TypedActionResult<ItemStack>? {
        val experience = this.calculator.calculate(player, pokemon)
        CobblemonEvents.EXPERIENCE_CANDY_USE_PRE.postThen(
                event = ExperienceCandyUseEvent.Pre(player, pokemon, this, experience, experience),
                ifSucceeded = { preEvent ->
                    val finalExperience = preEvent.experienceYield
                    val source = CandyExperienceSource(player, stack)
                    val result = pokemon.addExperienceWithPlayer(player, source, finalExperience)
                    // We do this just so we can post the event once the item has been consumed if needed instead of repeating the even post
                    var returnValue = false
                    if (result.experienceAdded > 0) {
                        if (!player.isCreative) {
                            stack.decrement(1)
                        }
                        returnValue = true
                    }
                    pokemon.entity?.playSound(CobblemonSounds.MEDICINE_CANDY_USE, 1F, 1F)
                    CobblemonEvents.EXPERIENCE_CANDY_USE_POST.post(ExperienceCandyUseEvent.Post(player, pokemon, this, result))

                    return if (returnValue)
                        TypedActionResult.success(stack)
                    else
                        TypedActionResult.fail(stack)
                }
        )
        return TypedActionResult.fail(stack)
    }

    override fun canUseOnPokemon(pokemon: Pokemon): Boolean {
        return pokemon.isPlayerOwned()
    }

    /**
     * Functional interface responsible for resolving the experience a candy will yield.
     *
     * @author Licious
     * @since March 5th, 2022
     */
    fun interface Calculator {

        /**
         * Resolves the experience the [CandyItem] will give.
         *
         * @param player The [ServerPlayerEntity] using the candy.
         * @param pokemon The [Pokemon] receiving experience.
         * @return The experience that will be received
         */
        fun calculate(player: ServerPlayerEntity, pokemon: Pokemon): Int

    }

    companion object {
        const val DEFAULT_XS_CANDY_YIELD = 100
        const val DEFAULT_S_CANDY_YIELD = 800
        const val DEFAULT_M_CANDY_YIELD = 3000
        const val DEFAULT_L_CANDY_YIELD = 10000
        const val DEFAULT_XL_CANDY_YIELD = 30000
    }
}