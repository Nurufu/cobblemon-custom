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
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.DEFAULT_NARRATION_SUPPLIER
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.Text

class WallpaperButton(
    pX: Int, pY: Int,
    onPress: PressAction,
    private val pcGui: PCGUI
) : ButtonWidget(pX, pY, WIDTH, HEIGHT, Text.literal("WallpaperButton"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    companion object {
        const val WIDTH = 24
        const val HEIGHT = 11

        private val buttonResource = cobblemonResource("textures/gui/pc/pc_wallpaper_button.png")
    }

    override fun render(context: net.minecraft.client.gui.DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.matrices,
            texture = buttonResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (this.visible || !pcGui.configuration.showParty) HEIGHT else 0,
            textureHeight = HEIGHT * 2
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1.0F))
    }
}
