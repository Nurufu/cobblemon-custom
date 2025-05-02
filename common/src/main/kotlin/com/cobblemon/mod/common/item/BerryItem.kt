/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import java.math.BigDecimal
import java.text.DecimalFormat

open class BerryItem(private val berryBlock: BerryBlock) : AliasedBlockItem(berryBlock, Settings()) {
    fun berry() = this.berryBlock.berry()
}