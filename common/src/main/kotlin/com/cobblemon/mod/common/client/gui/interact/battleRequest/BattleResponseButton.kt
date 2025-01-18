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

class BattleResponseButton(
    pX: Int, pY: Int,
    private val accept: Boolean,
    onPress: PressAction
): ButtonWidget(pX, pY, (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), Text.literal("Navigation"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    companion object {
        private const val WIDTH = 34F
        private const val HEIGHT = 19F
        private const val SCALE = 1F
        private val acceptButtonResource = cobblemonResource("textures/gui/interact/request/button_request_accept.png")
        private val declineButtonResource = cobblemonResource("textures/gui/interact/request/button_request_decline.png")
        private val acceptIconResource = cobblemonResource("textures/gui/interact/request/icon_accept.png")
        private val declineIconResource = cobblemonResource("textures/gui/interact/request/icon_decline.png")
    }

    override fun render(context: DrawContext, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val hovered = (isHovered(pMouseX.toDouble(), pMouseY.toDouble()))
        val matrices = context.matrices
        blitk(
            matrixStack = matrices,
            x = x / SCALE,
            y = y / SCALE,
            texture = if (accept) acceptButtonResource else declineButtonResource,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (hovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )

        blitk(
            matrixStack = context.matrices,
            x = x / 0.5F + 24,
            y = y / 0.5F + 7,
            texture = if (accept) acceptIconResource else declineIconResource,
            width = 21,
            height = 26,
            textureHeight = 26,
            scale = 0.5F
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + (HEIGHT * SCALE)))
}