/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.battleRequest

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.Text

class BattleRequestNavigationButton(
    pX: Int, pY: Int,
    private val clickHeight: Float = HEIGHT,
    private val forward: Boolean,
    onPress: PressAction
): ButtonWidget(pX, pY, (WIDTH * SCALE).toInt(), (clickHeight * SCALE).toInt(), Text.literal("Navigation"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    companion object {
        const val WIDTH = 9F
        const val HEIGHT = 16F
        const val SCALE = 0.5F
        private val forwardButtonResource = cobblemonResource("textures/gui/interact/request/arrow_right.png")
        private val backwardsButtonResource = cobblemonResource("textures/gui/interact/request/arrow_left.png")
    }

    override fun render(context: DrawContext, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val hovered = (isHovered(pMouseX.toDouble(), pMouseY.toDouble()))
        val matrices = context.matrices
        blitk(
            matrixStack = matrices,
            x = x / SCALE,
            y = (y + (clickHeight - HEIGHT) / 4) / SCALE,
            texture = if (forward) forwardButtonResource else backwardsButtonResource,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (hovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in ((y.toFloat())..(y.toFloat() + (clickHeight * SCALE)))
}