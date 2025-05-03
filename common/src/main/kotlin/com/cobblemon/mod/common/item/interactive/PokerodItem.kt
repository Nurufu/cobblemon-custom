/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive
import net.minecraft.item.BundleItem
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitConsumedEvent
import com.cobblemon.mod.common.api.events.fishing.BaitSetEvent
import com.cobblemon.mod.common.api.events.fishing.PokerodCastEvent
import com.cobblemon.mod.common.api.events.fishing.PokerodReelEvent
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.client.sound.CancellableSoundInstance
import com.cobblemon.mod.common.duck.SoundManagerDuck
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.item.BerryItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.playSoundServer
import com.ibm.icu.text.DecimalFormat
import it.unimi.dsi.fastutil.objects.ObjectLists
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerAbilities
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.StackReference
import net.minecraft.item.FishingRodItem
import net.minecraft.item.Item
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
import net.minecraft.screen.slot.Slot
import net.minecraft.util.ClickType
import net.minecraft.util.math.Vec3d

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

        fun consumeBait(stack: ItemStack){
            CobblemonEvents.BAIT_CONSUMED.postThen(BaitConsumedEvent(stack), { event -> }, {
                val baitStack = getBait(stack)
                val baitCount = baitStack.count
                if (baitCount == 1) {
                    stack.orCreateNbt.apply {
                        val b = ItemStack(baitStack.item, 0)
                        put(NBT_KEY_BAIT, b.writeNbt(NbtCompound()))
                    }
                    return
                }
                if (baitCount > 1) {
                    stack.orCreateNbt.apply {
                        val baitStack2 = ItemStack(baitStack.item, baitCount-1)
                        put(NBT_KEY_BAIT, baitStack2.writeNbt(NbtCompound()))
                    }
                }
            })
        }

        fun setBait(stack: ItemStack, bait: ItemStack) {
            CobblemonEvents.BAIT_SET.postThen(BaitSetEvent(stack, bait), { event -> }, {
                val nbt = stack.orCreateNbt
                nbt.put(NBT_KEY_BAIT, bait.writeNbt(NbtCompound()))
            })
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

    }

    override fun onClicked(
        itemStack: ItemStack,
        itemStack2: ItemStack,
        slot: Slot,
        clickAction: ClickType,
        player: PlayerEntity,
        slotAccess: StackReference
    ): Boolean {
        if (clickAction != ClickType.RIGHT)
            return false

        val baitStack = getBait(itemStack)

        CobblemonEvents.BAIT_SET_PRE.postThen(BaitSetEvent(itemStack, itemStack2), { event ->
            return event.isCanceled
        }, {

            // If not holding an item on cursor
            if (itemStack2.isEmpty) {
                // Retrieve bait onto cursor
                if (baitStack != ItemStack.EMPTY) {
                    playDetachSound(player)
                    setBait(itemStack, ItemStack.EMPTY)
                    slotAccess.set(baitStack.copy())
                    return true
                }
            }
            // If holding item on cursor
            else {

                // If item on cursor is a valid bait
                if (FishingBaits.getFromBaitItemStack(itemStack2) != null) {

                    // Add as much as possible
                    if (baitStack != ItemStack.EMPTY) {
                        if (baitStack.item == itemStack2.item) {
                            playAttachSound(player)
                            // Calculate how much bait to add
                            val diff = (baitStack.maxCount - baitStack.count).coerceIn(0, itemStack2.count)
                            itemStack2.decrement(diff)
                            baitStack.increment(diff)
                            setBait(itemStack, baitStack)
                            return true
                        }

                        // If Item on rod is different from cursor item, swap them
                        playAttachSound(player)
                        setBait(itemStack, itemStack2.copy())
                        slotAccess.set(baitStack.copy())
                        return true
                    }

                    // If no bait currently on rod, add all
                    playAttachSound(player)
                    setBait(itemStack, itemStack2.copy())
                    itemStack2.decrement(itemStack2.count)
                    return true
                }
            }
        })
        return false
    }



    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        // if item in mainhand is berry item then don't do anything
//        if (user.getItemInHand(InteractionHand.MAIN_HAND).item is BerryItem)
//            return InteractionResultHolder(
//                InteractionResult.FAIL,
//                user.getItemInHand(hand)
//            )

        val itemStack = user.mainHandStack
        val offHandItem = user.offHandStack
        val offHandBait = FishingBaits.getFromBaitItemStack(offHandItem)

        // if there already is bait on the bobber then drop it on the ground
        val baitOnRod = getBait(itemStack)

        // if the item in the offhand is a bait item and the mainhand item is a pokerod then apply the bait
//        if (!world.isClientSide && user.fishing == null && offHandBait != null && offHandBait != baitOnRod && !user.isShiftKeyDown) {
//
//            if (baitOnRod != null) {
//                val item = world.itemRegistry.get(baitOnRod.item)
//                if (item != null) {
//                    user.spawnAtLocation(ItemStack(item))
//                }
//            }
//
//            // set the bait and bait effects on the bobber
//            setBait(itemStack, offHandItem.copyWithCount(1))
//
//            // remove 1 bait from the offhand
//            offHandItem.shrink(1)
//
//            // remove old bait tooltip from rod
////            removeBaitTooltip(itemStack, world)
//
//            // set new bait tooltip to rod
////            setBaitTooltips(itemStack, world)
//        }

        // if the user is sneaking when casting then remove the bait from the bobber
//        if (!world.isClientSide && user.fishing == null && user.isShiftKeyDown) {
//            // If there is a bait on the bobber
//            if (baitOnRod != null) {
//                // drop the stack of bait
//                val item = world.itemRegistry.get(baitOnRod.item)
//                if (item != null) {
//                    user.spawnAtLocation(ItemStack(item))
//                }
//                //set the bait and bait effects on the rod to be empty
//                setBait(itemStack, ItemStack.EMPTY)
//
//                // remove old bait tooltip from rod
//                removeBaitTooltip(itemStack, world)
//            }
//        }

        // If rod is empty and offhand has bait, add bait from offhand
        if (!world.isClient() && user.fishHook == null && offHandBait != null && baitOnRod == null) {
            CobblemonEvents.BAIT_SET_PRE.postThen(BaitSetEvent(itemStack, offHandItem), { event ->
                return TypedActionResult.fail(itemStack)
            }, {

                playAttachSound(user)
                setBait(itemStack, offHandItem.copy())
                offHandItem.decrement(offHandItem.count)
            })
        }

        var i: Int
        if (user.fishHook != null) { // if the bobber is out yet
            if (!world.isClient()) {
                CobblemonEvents.POKEROD_REEL.postThen(
                    PokerodReelEvent(itemStack),
                    { event -> return TypedActionResult.fail(itemStack) },
                    { event ->
                    i = user.fishHook!!.use(itemStack)
                itemStack.damage(i, user) { p: PlayerEntity -> p.sendToolBreakStatus(hand) }
                        world.playSoundServer(
                            Vec3d(user.x,
                                user.y,
                                user.z),
                            CobblemonSounds.FISHING_ROD_REEL_IN,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f)
                        )
                    }
                )
            }


            // stop sound of casting when reeling in
            //(MinecraftClient.getInstance().getSoundManager() as SoundManagerDuck).stopSounds(CobblemonSounds.FISHING_ROD_CAST.id, SoundCategory.PLAYERS)



            //(MinecraftClient.getInstance().getSoundManager()).stop

            world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_REEL_IN, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH)
        } else { // if the bobber is not out yet
            // play the Rod casting sound and set it
            world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))

            if (!world.isClient()) {
                val lureLevel = EnchantmentHelper.getLure(itemStack)
                val luckLevel = EnchantmentHelper.getLuckOfTheSea(itemStack)


                val bobberEntity = PokeRodFishingBobberEntity(
                    user,
                    pokeRodId,
                    getBait(itemStack).copy() ?: ItemStack.EMPTY,
                    world,
                    luckLevel,
                    lureLevel,
                    itemStack
                )
                CobblemonEvents.POKEROD_CAST_PRE.postThen(
                    PokerodCastEvent.Pre(itemStack, bobberEntity, getBait(itemStack)),
                    { event -> return TypedActionResult.fail(itemStack) },
                    { event ->
                        world.spawnEntity(bobberEntity)
                        CobblemonCriteria.CAST_POKE_ROD.trigger(user as ServerPlayerEntity, baitOnRod != null)

                        CobblemonEvents.POKEROD_CAST_POST.post(
                            PokerodCastEvent.Post(itemStack, bobberEntity, getBait(itemStack))
                        )
                    }
                )
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this))
            user.emitGameEvent(GameEvent.ITEM_INTERACT_START)
        }

        return TypedActionResult.success(itemStack, world.isClient())
    }

    private fun playAttachSound(entity: PlayerEntity){
        entity.playSound(CobblemonSounds.FISHING_BAIT_ATTACH, 1F, 1F)
    }

    private fun playDetachSound(entity: PlayerEntity){
        entity.playSound(CobblemonSounds.FISHING_BAIT_DETACH, 1F, 1F)
    }

    override fun getEnchantability(): Int {
        return 1
    }

    override fun getTranslationKey(): String {
        return "item.cobblemon.poke_rod"
    }

}
