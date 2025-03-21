/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ScrollingWidget
import com.cobblemon.mod.common.client.gui.pc.WallpapersScrollingWidget.WallpaperEntry
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.gui.PCBoxWallpaperRepository
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

class WallpapersScrollingWidget(
    pX: Int, val pY: Int,
    val pcGui: PCGUI,
    val storageWidget: StorageWidget,
) : ScrollingWidget<WallpaperEntry>(
    width = WIDTH,
    height = HEIGHT,
    left = pX,
    top = 0,
    slotHeight = SLOT_HEIGHT + SLOT_PADDING,
    scrollBarWidth = SCROLL_BAR_WIDTH
) {
    companion object {
        const val WIDTH = 68
        const val HEIGHT = 146
        const val SLOT_WIDTH = 56
        const val SLOT_HEIGHT = 50
        const val SLOT_PADDING = 4
        const val SCROLL_BAR_WIDTH = 3
        val ENTRY_HOVERED = cobblemonResource("textures/gui/pc/wallpaper_entry_hover.png")
        val BACKGROUND = cobblemonResource("textures/gui/pc/wallpaper_widget_background.png")
        var visible: Boolean = false
    }

    init {
        this.setVisible(false)
        this.top = pY
        createEntries()
    }

    fun isVisible(): Boolean {
        return visible
    }

    fun setVisible(b: Boolean): Unit{
        visible = b
    }

    override fun getScrollbarPositionX(): Int = left + width - 3

    override fun render(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        if(this.isVisible()) {
            blitk(
                matrixStack = context.matrices,
                texture = BACKGROUND,
                x = left - 1,
                y = top - 1,
                width = width + 1,
                height = height + 2
            )

            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.wallpapers").bold(),
                x = left + 23,
                y = top - 18.5,
                centered = true,
                shadow = true
            )
            context.enableScissor(left, top, left + width, top + height)
            super.render(context, mouseX, mouseY, delta)
            context.disableScissor()
        }
    }

    private fun createEntries() {
        clearEntries()
        for (wallpaper in PCBoxWallpaperRepository.wallpapers) {
            addEntry(WallpaperEntry(wallpaper))
            Cobblemon.LOGGER.info(wallpaper.toString())
        }
    }

    override fun getRowLeft(): Int {
        return this.left + SLOT_PADDING
    }

    override fun getRowWidth(): Int {
        return SLOT_WIDTH
    }

    override fun getRowRight(): Int {
        return this.rowLeft + SLOT_WIDTH
    }

    inner class WallpaperEntry(val wallpaper: Identifier) : Slot<WallpaperEntry>() {
        var i = 5
        override fun render(
            guiGraphics: DrawContext,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            hovering: Boolean,
            partialTick: Float
        ) {
            if (visible) {
                val matrices = guiGraphics.matrices
                blitk(
                    matrixStack = matrices,
                    texture = wallpaper,
                    x = left,
                    y = top,
                    width = width,
                    height = 40,
                )
                if (hovering) {
                    blitk(
                        matrixStack = matrices,
                        texture = ENTRY_HOVERED,
                        x = left,
                        y = top,
                        width = width,
                        height = height
                    )
                }
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (this@WallpapersScrollingWidget.isVisible() && isMouseOver(mouseX, mouseY)) {
                RequestChangePCBoxWallpaperPacket(pcGui.pc.uuid, storageWidget.box, wallpaper).sendToServer()
                pcGui.pc.boxes[storageWidget.box].wallpaper = wallpaper
                //SoundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1.0F))
                return true
            }
            return false
        }

        override fun getNarration(): MutableText {
            return wallpaper.toString().text()
        }
    }
}
