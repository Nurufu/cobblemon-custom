/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class DescriptionWidget(val x: Int, val y: Int): InfoTextScrollWidget(pX = x, pY = y) {
    companion object {
        private val unknownIcon = cobblemonResource("textures/gui/pokedex/pokedex_slot_unknown.png")
    }

    var showPlaceholder: Boolean = false

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Text.translatable("cobblemon.ui.pokedex.info.description").bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        if (showPlaceholder) {
            blitk(
                matrixStack = context.matrices,
                texture = unknownIcon,
                x = pX + 65.5,
                y = pY + 16,
                width = 8,
                height = 10
            )
        } else {
            super.render(context, mouseX, mouseY, delta)
        }
    }
}