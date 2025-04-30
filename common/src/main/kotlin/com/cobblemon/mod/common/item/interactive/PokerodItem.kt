/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.client.sound.CancellableSoundInstance
import com.cobblemon.mod.common.duck.SoundManagerDuck
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.item.BerryItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import it.unimi.dsi.fastutil.objects.ObjectLists
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString


class PokerodItem(val pokeRodId: Identifier, settings: Settings?) : FishingRodItem(settings) {

    companion object {
        private const val NBT_KEY_BAIT = "Bait"
        private const val NBT_KEY_BAIT_EFFECTS = "BaitEffects"
        private const val NBT_KEY_BOBBER = "Bobber"

        fun getBait(stack: ItemStack): ItemStack {
            val nbt = stack.orCreateNbt
            return if (nbt.contains(NBT_KEY_BAIT)) {
                ItemStack.fromNbt(nbt.getCompound(NBT_KEY_BAIT))
            } else {
                ItemStack.EMPTY
            }
        }

        fun setBait(stack: ItemStack, bait: ItemStack) {
            val nbt = stack.orCreateNbt
            nbt.put(NBT_KEY_BAIT, bait.writeNbt(NbtCompound()))
        }

        fun getBaitEffects(stack: ItemStack): List<FishingBait.Effect> {
            val nbt = stack.orCreateNbt
            val effects = mutableListOf<FishingBait.Effect>()
            if (nbt.contains(NBT_KEY_BAIT_EFFECTS)) {
                val nbtList = nbt.getList(NBT_KEY_BAIT_EFFECTS, 10) // 10 is the type for NbtCompound
                for (i in 0 until nbtList.size) {
                    effects.add(FishingBait.Effect.fromNbt(nbtList.getCompound(i)))
                }
            }
            return effects
        }

        fun setBaitEffects(stack: ItemStack, effects: List<FishingBait.Effect>) {
            val nbt = stack.orCreateNbt
            val nbtList = NbtList()
            for (effect in effects) {
                nbtList.add(effect.toNbt())
            }
            nbt.put(NBT_KEY_BAIT_EFFECTS, nbtList)
        }

        /*fun writeBobberToNbt(bobberEntity: PokeRodFishingBobberEntity, nbt: NbtCompound) {
            val bobberNbt = NbtCompound()
            bobberNbt.putUuid("uuid", bobberEntity.uuid)
            nbt.put(NBT_KEY_BOBBER, bobberNbt)
        }

        fun readBobberFromNbt(nbt: NbtCompound, world: World): PokeRodFishingBobberEntity? {
            if (!nbt.contains(NBT_KEY_BOBBER)) return null

            val bobberNbt = nbt.getCompound(NBT_KEY_BOBBER)
            val uuid = bobberNbt.getUuid("uuid")

            return PokeRodFishingBobberEntity(world, uuid)
        }*/

    }

    //var bait: ItemStack = ItemStack.EMPTY
    //var baitEffects: List<FishingBait.Effect>? = mutableListOf()

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        // if item in mainhand is berry item then don't do anything
        if (user.getStackInHand(Hand.MAIN_HAND).item is BerryItem)
            return TypedActionResult(ActionResult.FAIL, user.getStackInHand(hand))

        val itemStack = user.getStackInHand(hand)
        val offHandItem = user.getStackInHand(Hand.OFF_HAND)

        // if the item in the offhand is a bait item and the mainhand item is a pokerod then apply the bait
        if (!world.isClient && user.fishHook == null && FishingBaits.isFishingBait(offHandItem) && !ItemStack.areItemsEqual(offHandItem, getBait(itemStack)) && !user.isSneaking) {
            // if there already is bait on the bobber then drop it on the ground
            if (!getBait(itemStack).isEmpty) {
                user.dropStack(getBait(itemStack))
            }

            // set the bait and bait effects on the bobber
            setBait(itemStack, offHandItem.copyWithCount(1))
            setBaitEffects(itemStack, FishingBaits.getFromItemStack(getBait(itemStack))?.effects ?: emptyList())

            // remove 1 bait from the offhand
            offHandItem.decrement(1)

            // remove old bait tooltip from rod
            removeBaitTooltip(itemStack, world)

            // set new bait tooltip to rod
            setBaitTooltips(itemStack, world)
        }

        // if the user is sneaking when casting then remove the bait from the bobber
        if (!world.isClient && user.fishHook == null && user.isSneaking) {
            // If there is a bait on the bobber
            if (!getBait(itemStack).isEmpty) {
                // drop the stack of bait
                user.dropStack(getBait(itemStack))
                //set the bait and bait effects on the rod to be empty
                setBait(itemStack, ItemStack.EMPTY)
                setBaitEffects(itemStack, emptyList())

                // remove old bait tooltip from rod
                removeBaitTooltip(itemStack, world)
            }
        }

        val i: Int
        if (user.fishHook != null) { // if the bobber is out yet
            if (!world.isClient) {
                i = user.fishHook!!.use(itemStack)
                itemStack.damage(i, user) { p: PlayerEntity -> p.sendToolBreakStatus(hand) }
            }
            // stop sound of casting when reeling in
            //(MinecraftClient.getInstance().getSoundManager() as SoundManagerDuck).stopSounds(CobblemonSounds.FISHING_ROD_CAST.id, SoundCategory.PLAYERS)



            //(MinecraftClient.getInstance().getSoundManager()).stop

            world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_REEL_IN, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH)
        } else { // if the bobber is not out yet
            // play the Rod casting sound and set it
            world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))

            // create a SoundInstance for the casting sound to be also sent to the bobber
            val castingSoundInstance = PositionedSoundInstance(
                CobblemonSounds.FISHING_ROD_CAST,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f),
                world.random,
                user.x,
                user.y,
                user.z
            )

            if (!world.isClient) {
                i = EnchantmentHelper.getLure(itemStack)
                val j = EnchantmentHelper.getLuckOfTheSea(itemStack)

                /*// play the Rod casting sound and set it
                world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))

                // create a SoundInstance for the casting sound to be also sent to the bobber
                val castingSoundInstance = CancellableSoundInstance(
                        CobblemonSounds.FISHING_ROD_CAST,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f),
                        world.random,
                        user.x,
                        user.y,
                        user.z
                )*/

                val bobberEntity = PokeRodFishingBobberEntity(user, pokeRodId, getBait(itemStack)/*Registries.ITEM.getId(bait?.item)*/, world, j, i, castingSoundInstance)

                // Set the casting sound to the bobber entity
                //bobberEntity.castingSound = castingSoundInstance

                world.spawnEntity(bobberEntity)
                CobblemonCriteria.CAST_POKE_ROD.trigger(user as ServerPlayerEntity, !getBait(itemStack).isEmpty)


            }

            user.incrementStat(Stats.USED.getOrCreateStat(this))
            user.emitGameEvent(GameEvent.ITEM_INTERACT_START)
        }
        return TypedActionResult.success(itemStack, world.isClient())
    }

    override fun getEnchantability(): Int {
        return 1
    }

    // todo lang stuff for dynamic tooltips
    //lang("overflow_no_space", pc.name)

    fun setBaitTooltips(
        stack: ItemStack,
        world: World?
    ) {
        val rod = PokeRods.getPokeRod((stack.item as PokerodItem).pokeRodId) ?: return
        val ball = PokeBalls.getPokeBall(rod.pokeBallId) ?: return
        var tooltipList: MutableList<Text> = mutableListOf()
        //var rodTooltipData = stack.tooltipData.get()

        tooltipList.add(ball.item.name.copy().gray())
        // for every effect of the bait add a tooltip to the rod
        getBaitEffects(stack).forEach {
            val effectType = it.type.path.toString()
            val effectSubcategory: String? = it.subcategory?.path.toString()
            val effectChance = it.chance * 100 // chance of effect out of 100
            val effectValue = it.value.toInt()
            var subcategoryString: String? = null

            // convert subcategory depending on Effect Type to be used as a String variable in the lang file
            if (effectSubcategory != null) {
                if (effectType == "nature" || effectType == "ev" || effectType == "iv") {
                    subcategoryString = com.cobblemon.mod.common.api.pokemon.stats.Stats.getStat(effectSubcategory).name.replace("_"," ")
                }
                else if (effectType == "gender") {
                    subcategoryString = Gender.valueOf(effectSubcategory).name
                }
                else if (effectType == "tera") {
                    subcategoryString = ElementalTypes.get(effectSubcategory)?.name
                }
            }

            tooltipList.add(lang("fishing_bait_effects." + effectType + ".tooltip", effectChance, subcategoryString ?: "", effectValue))


        }
        val test = tooltipList

        //rodTooltipData = tooltipList // todo find some way to set the itemStack's tooltip to be this new tooltipList
    }

    fun removeBaitTooltip(
        stack: ItemStack,
        world: World?
    ) {
        val rod = PokeRods.getPokeRod((stack.item as PokerodItem).pokeRodId) ?: return
        val ball = PokeBalls.getPokeBall(rod.pokeBallId) ?: return
        var tooltipList: MutableList<Text> = mutableListOf()
        //var rodTooltipData = stack.tooltipData.get()

        tooltipList.add(ball.item.name.copy().gray())
        val test = tooltipList

        //rodTooltipData = tooltipList // todo find some way to set the itemStack's tooltip to be this new tooltipList
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val rod = PokeRods.getPokeRod((stack.item as PokerodItem).pokeRodId) ?: return
        val ball = PokeBalls.getPokeBall(rod.pokeBallId) ?: return
        tooltip.add(ball.item.name.copy().gray())

        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun getTranslationKey(): String {
        return "item.cobblemon.poke_rod"
    }

}
