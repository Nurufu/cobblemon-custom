/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.gui.PCBoxWallpaperRepository
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier


class WallpapersScrollingWidget(pX: Int, pY: Int, val pcGui: PCGUI, val storageWidget: StorageWidget) :
    AlwaysSelectedEntryListWidget<WallpapersScrollingWidget.Entry?>(
        MinecraftClient.getInstance(),
        68,
        46,
        pY,
        pY + 150,
        ENTRY_HEIGHT
    ) {
    var _visible: Boolean = false

    init {
        centerListVertically = false
        setRenderBackground(false)
        setRenderHeader(false, 0)
        setRenderHorizontalShadows(false)
        this.top = pY-6
        setLeftPos(pX+68)
    }

    fun setVisible(visible: Boolean) {
        if (visible) {
            for (wallpaper in PCBoxWallpaperRepository.wallpapers) {
                addEntry(Entry(wallpaper))
            }
            this._visible = true
        } else {
            clearEntries()
            this._visible = false
        }
    }

    fun isVisible(): Boolean {
        return _visible
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseOver(mouseX, mouseY) && hoveredEntry != null) {
//            Utils.playSound(CobblemonSounds.PC_CLICK)
            hoveredEntry!!.mouseClicked(mouseX, mouseY, button)
            return true
        }
        return false
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (_visible) {
            blitk(
                matrixStack = context.matrices,
                texture = cobblemonResource("textures/gui/pc/wallpaper_scroll_background.png"),
                x = left,
                y = top-1,
                width = 80,
                height= 158
            )
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.wallpapers").bold(),
                x = left + 29,
                y = top - 12.5,
                centered = true,
                shadow = true
            )
            super.render(context, mouseX, mouseY, delta)
        }
    }

    override fun renderList(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        for (i in 0..<entryCount) {
            val top = getRowTop(i) + 2
            val bottom = top + ENTRY_HEIGHT
            if (bottom >= this.top && top <= this.bottom - 2) {
                this.renderEntry(
                    context, mouseX, mouseY, delta, i,
                    this.left + (this.width / 2) - (56 / 2), top, ENTRY_WIDTH, ENTRY_HEIGHT
                )
            }
        }
    }

    override fun getScrollbarPositionX(): Int {
        return this.left + this.width+5
    }

    inner class Entry(protected val wallpaper: Identifier) :
        AlwaysSelectedEntryListWidget.Entry<Entry?>() {
        override fun render(
            context: DrawContext,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            if (hovered) {
                val textRenderer = MinecraftClient.getInstance().textRenderer
                if (y + textRenderer.fontHeight + 2 > this@WallpapersScrollingWidget.top) {
                    //context.drawTooltip(textRenderer, Text.of(wallpaper.toString()), x, y + textRenderer.fontHeight + 2)
                }
                RenderSystem.disableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f)
            }

            context.drawTexture(
                wallpaper,
                x, y,
                56, 56,
                0f, 0f,
                56,
                36,
                56,
                36
            )

            RenderSystem.enableBlend()
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if(this@WallpapersScrollingWidget._visible && isMouseOver(mouseX, mouseY)){
                RequestChangePCBoxWallpaperPacket(pcGui.pc.uuid, storageWidget.box, wallpaper).sendToServer()
                pcGui.pc.boxes[storageWidget.box].wallpaper = wallpaper

                return true
            }
            return false
        }

        override fun getNarration(): Text {
            return Text.empty()
        }

    }

    companion object {
        protected const val ENTRY_WIDTH: Int = 46
        protected const val ENTRY_HEIGHT: Int = 56
    }
}