/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.gui

import java.util.stream.Collectors
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
class MultiLineLabelK(
    private val comps: List<TextWithWidth>,
    private val font: Identifier? = null
) {

    companion object {
        private val mcFont = MinecraftClient.getInstance().textRenderer

        fun create(component: Text, width: Number, maxLines: Number) = create(component, width, maxLines, null)

        fun create(component: Text, width: Number, maxLines: Number, font: Identifier?): MultiLineLabelK {
            return MultiLineLabelK(
                mcFont.textHandler.wrapLines(component, width.toInt(), Style.EMPTY).stream()
                    .limit(maxLines.toLong())
                    .map {
                    TextWithWidth(it, mcFont.getWidth(it))
                }.collect(Collectors.toList()),
                font = font
            )
        }
    }

    fun renderLeftAligned(
        context: DrawContext,
        x: Number, y: Number,
        YStartOffset: Number = 0,
        ySpacing: Number,
        colour: Int,
        scale: Float = 1F,
        shadow: Boolean = true
    ) {
        context.matrices.push()
        context.matrices.scale(scale, scale, 1F)
        comps.forEachIndexed { index, textWithWidth ->
            val yOffset = if (index == 0) YStartOffset else 0
            drawString(
                context = context,
                x = x.toFloat() / scale,
                y = (y.toFloat() + yOffset.toFloat() + ySpacing.toFloat() * index) / scale,
                colour = colour,
                shadow = shadow,
                text = textWithWidth.text.string,
                font = font
            )
        }
        context.matrices.pop()
    }

    class TextWithWidth internal constructor(val text: StringVisitable, val width: Int)
}