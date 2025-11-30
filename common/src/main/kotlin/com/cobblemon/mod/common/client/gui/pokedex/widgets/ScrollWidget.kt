/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class ScrollWidget(pX: Int, pY: Int
): SoundlessWidget(pX, pY, ScrollWidget.WIDTH, ScrollWidget.HEIGHT, Text.literal("ScrollWidget")) {
    companion object {
        private const val WIDTH = 100
        private const val HEIGHT = 100
    }
    override fun renderButton(context: DrawContext, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        drawScaledText(
            context = context,
            text = lang("test"),
            x = x + 10,
            y = y + 10
        )
    }
}