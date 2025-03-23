/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.mixin.accessor.EntryListWidgetAccessor
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.math.MathHelper

abstract class ScrollingWidget<T : AlwaysSelectedEntryListWidget.Entry<T>>(
    top: Int = 0,
    val left: Int = 0,
    width: Int = 10,
    height: Int = 10,
    slotHeight: Int = 10,
    val scrollBarWidth: Int = 5,
    val m: Int = 0
) : AlwaysSelectedEntryListWidget<T>(
    MinecraftClient.getInstance(),
    width, // Width
    height, // Height
    top, // Top
    slotHeight, // Slot Height,
    m
), CobblemonRenderable {
    override fun renderBackground(context: DrawContext) {}
    override fun drawSelectionHighlight(context: DrawContext, y: Int, entryWidth: Int, entryHeight: Int, borderColor: Int, fillColor: Int) {}
    override fun renderDecorations(context: DrawContext, mouseX: Int, mouseY: Int) {}


    init {
        updateSize(width, height, top, top + height)
    }

    final override fun updateSize(width: Int, height: Int, top: Int, bottom: Int) {
        this.width = width
        this.height = height
        this.top = top
        this.bottom = bottom
        this.right = left + width
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val asAccessor = this as EntryListWidgetAccessor
//        this.focused = if (this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) this.getEntryAtPosition(
//            mouseX.toDouble(),
//            mouseY.toDouble()
//        ) else null
        val renderBackground = false
        if (renderBackground) {
            context.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
            context.drawTexture(
                Screen.OPTIONS_BACKGROUND_TEXTURE,
                this.left,
                this.top,
                right.toFloat(),
                (this.bottom + scrollAmount.toInt()).toFloat(),
                this.right - this.left,
                this.bottom - this.top, 32, 32
            )
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        }

        val l1 = this.rowLeft
        val l = this.top + 4 - scrollAmount.toInt()

        this.enableScissor(context)
        if (asAccessor.renderHeader) {
            this.renderHeader(context, l1, l)
        }

        this.renderList(context, mouseX, mouseY, delta)
        context.disableScissor()

        val renderHorizontalShadows = false
        if (renderHorizontalShadows) {
            this.renderHorizontalShadows(context, mouseX, mouseY, delta)
        }

        val maxScroll = this.maxScroll
        if (maxScroll > 0) {
            renderScrollbar(context, mouseX, mouseY, delta)
        }

        this.renderDecorations(context, mouseX, mouseY)
        RenderSystem.disableBlend()
    }

    open fun renderScrollbar(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float){
        val xLeft = this.scrollbarPositionX
        val xRight = xLeft + scrollBarWidth

        val barHeight = this.bottom - this.top

        var j2 = ((barHeight * barHeight).toFloat() / this.maxPosition.toFloat()).toInt()
        j2 = MathHelper.clamp(j2, 32, barHeight - 8)
        var k1 = scrollAmount.toInt() * (barHeight - j2) / this.maxScroll + this.top
        if (k1 < this.top) {
            k1 = this.top
        }

        context.fill(xLeft, this.top, xRight, this.bottom, -16777216)
        context.fill(xLeft, k1, xRight, k1 + j2, -8355712)
        context.fill(xLeft ,k1, xRight - 1, k1 + j2 - 1, -4144960)
    }

    open fun renderHorizontalShadows(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float){
        context.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f)
        context.drawTexture(
            Screen.OPTIONS_BACKGROUND_TEXTURE,
            this.left, 0, 0.0f, 0.0f,
            this.width,
            this.top, 32, 32
        )
        context.drawTexture(
            Screen.OPTIONS_BACKGROUND_TEXTURE,
            this.left,
            this.bottom, 0.0f,
            bottom.toFloat(),
            this.width,
            this.height - this.bottom, 32, 32
        )
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        context.fillGradient(
            RenderLayer.getGuiOverlay(),
            this.left,
            this.top,
            this.right,
            this.top + 4, -16777216, 0, 0
        )
        context.fillGradient(
            RenderLayer.getGuiOverlay(),
            this.left,
            this.bottom - 4,
            this.right,
            this.bottom, 0, -16777216, 0
        )
    }

    override fun renderList(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val i = this.rowLeft
        val j = this.rowWidth
        val k = this.itemHeight
        val l = this.entryCount

        for (m in 0 until l) {
            val n = this.getRowTop(m)
            val o = this.getRowBottom(m)
            if (o >= this.top && n <= this.bottom) {
                this.renderEntry(context!!, mouseX, mouseY, delta, m, i, n, j, k)
            }
        }
    }

    override fun renderEntry(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        index: Int,
        x: Int,
        y: Int,
        entryWidth: Int,
        entryHeight: Int
    ) {
        val entry =  this.getEntry(index)
        entry.render(
            context, index, y, x, entryWidth, entryHeight, mouseX, mouseY,
            hoveredEntry == entry, delta
        )
    }

    override fun getRowLeft(): Int {
        return this.left
    }

    override fun getRowRight(): Int {
        return this.rowLeft + this.rowWidth
    }

    override fun getRowWidth(): Int {
        return this.width
    }

    override fun getRowTop(index: Int): Int {
        return this.top - scrollAmount.toInt() + (index * this.itemHeight)
    }

    override fun getRowBottom(index: Int): Int {
        return this.getRowTop(index) + this.itemHeight
    }

    override fun getScrollbarPositionX(): Int = this.left + this.width - this.scrollBarWidth

    abstract class Slot<T : Slot<T>>(): Entry<T>() {
        // Override render to show each individual element
    }
}