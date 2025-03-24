/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.PokemonSortMode
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.DEFAULT_NARRATION_SUPPLIER
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.Text
import kotlin.math.abs

class SortButton(
    pX: Int, pY: Int,
    onPress: PressAction,
    val storageWidget: StorageWidget
) : ButtonWidget(pX, pY, WIDTH, HEIGHT, Text.literal("SortButton"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    var sortMode = PokemonSortMode.NAME
    val storage = storageWidget

    companion object {
        const val WIDTH = 24
        const val HEIGHT = 11

        @OptIn(ExperimentalStdlibApi::class)
        private val modeResources = PokemonSortMode.entries.map { cobblemonResource("textures/gui/pc/pc_sort_${it.name.lowercase()}.png") }
        private val buttonResource = cobblemonResource("textures/gui/pc/pc_sort_button.png")
    }

    override fun render(context: net.minecraft.client.gui.DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.matrices,
            texture = buttonResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered()) HEIGHT else 0,
            textureHeight = HEIGHT * 2
        )

        blitk(
            matrixStack = context.matrices,
            texture = modeResources[sortMode.ordinal],
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT
        )
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        pDelta: Double
    ): Boolean {
        if (abs(pDelta) >= .5 && isMouseOver(mouseX, mouseY)) {
            val modes = PokemonSortMode.entries
            var mode = sortMode.ordinal + if (pDelta > 0) 1 else -1
            if (mode < 0) mode = modes.size - 1
            if (mode >= modes.size) mode = 0
            sortMode = modes[mode]
            if(pDelta.toInt() == 1) storage.box += 1 else storage.box -= 1
            return true
        }
        return false
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1.0F))
    }
}
