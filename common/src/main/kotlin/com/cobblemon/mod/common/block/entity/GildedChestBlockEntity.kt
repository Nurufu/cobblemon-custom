/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.block.chest.GildedChestBlock.Type
import com.cobblemon.mod.common.block.chest.GildedState
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.entity.ViewerCountManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.sound.SoundCategory
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class GildedChestBlockEntity(pos: BlockPos, state: BlockState, val type: Type = Type.RED) : LootableContainerBlockEntity(CobblemonBlockEntities.GILDED_CHEST, pos, state), SidedInventory {
    var inventoryContents: DefaultedList<ItemStack> = DefaultedList.ofSize(NUM_SLOTS, ItemStack.EMPTY)
    val poseableState: GildedState = GildedState()

    private val stateManager: ViewerCountManager = object : ViewerCountManager() {
        override fun onContainerOpen(world: World, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, CobblemonSounds.GILDED_CHEST_OPEN)
        }

        override fun onContainerClose(world: World, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, CobblemonSounds.GILDED_CHEST_CLOSE)
        }

        override fun onViewerCountUpdate(
            world: World,
            pos: BlockPos,
            state: BlockState,
            oldViewerCount: Int,
            newViewerCount: Int
        ) {
            this@GildedChestBlockEntity.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount)
        }

        override fun isPlayerViewing(player: PlayerEntity): Boolean {
            if (player.currentScreenHandler is GenericContainerScreenHandler) {
                val inventory = (player.currentScreenHandler as GenericContainerScreenHandler).inventory
                return inventory === this@GildedChestBlockEntity
            }
            return false
        }
    }

    override fun getType(): BlockEntityType<*> = CobblemonBlockEntities.GILDED_CHEST
    override fun size() = NUM_SLOTS

    override fun getContainerName() = Text.translatable("block.cobblemon.gilded_chest")
    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory?) =
        GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this)

    override fun onOpen(player: PlayerEntity) {
        if (!this.removed && !player.isSpectator && type != Type.FAKE) {
            stateManager.openContainer(player, this.getWorld(), this.getPos(), this.cachedState)
        }
    }

    override fun onClose(player: PlayerEntity) {
        if (!this.removed && !player.isSpectator) {
            stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.cachedState)
        }
    }

    override fun getAvailableSlots(side: Direction): IntArray {
        return if (type == Type.FAKE) IntArray(0) else IntArray(NUM_SLOTS) { it }
    }

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
        if (type == Type.FAKE) return false
        return dir != Direction.DOWN
    }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
        if (type == Type.FAKE) return false
        return dir == Direction.DOWN
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventoryContents

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventoryContents = list
    }

    companion object {
        val NUM_SLOTS = 27
        fun playSound(world: World, pos: BlockPos, state: BlockState, sound: net.minecraft.sound.SoundEvent) {
            var d = pos.x.toDouble() + 0.5
            val e = pos.y.toDouble() + 0.5
            var f = pos.z.toDouble() + 0.5
            val direction = state.get(Properties.HORIZONTAL_FACING)
            d += direction.offsetX.toDouble() * 0.5
            f += direction.offsetZ.toDouble() * 0.5
            world.playSound(
                null,
                d,
                e,
                f,
                sound,
                SoundCategory.BLOCKS,
                0.5f,
                world.random.nextFloat() * 0.1f + 0.9f
            )
        }
    }

    override fun onSyncedBlockEvent(type: Int, data: Int): Boolean {
        if (type == 1) {
            val isNowOpen = data > 0
            val wasOpen = poseableState.currentPose == "open"
            val model = poseableState.currentModel ?: return true
            if (isNowOpen && !wasOpen) {
                model.moveToPose(poseableState, model.poses["open"]!!)
            } else if (!isNowOpen && wasOpen) {
                model.moveToPose(poseableState, model.poses["closed"]!!)
            }
            return true
        }
        return super.onSyncedBlockEvent(type, data)
    }

    fun onViewerCountUpdate(world: World, pos: BlockPos, state: BlockState, oldViewerCount: Int, newViewerCount: Int) {
        val block = state.block
        world.addSyncedBlockEvent(pos, block, 1, newViewerCount)
    }
    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        if (!serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, inventoryContents)
        }
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        inventoryContents= DefaultedList.ofSize(
            size(), ItemStack.EMPTY
        )
        if (!deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, inventoryContents)
        }
    }

    fun onScheduledTick() {
        if (!this.removed) {
            stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.cachedState)
        }
    }
}