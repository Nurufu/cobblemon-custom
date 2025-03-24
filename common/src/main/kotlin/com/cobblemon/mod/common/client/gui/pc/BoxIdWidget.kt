/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.gui.drawText
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class BoxIdWidget(val gui: PCGUI, val storageWidget: StorageWidget) : Drawable {
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        var x = (gui.width - PCGUI.BASE_WIDTH) / 2.0
        var y = (gui.height - PCGUI.BASE_WIDTH) / 2.0

        x += 108.0
        y += 63.0

        context.drawTexture(
            cobblemonResource("textures/gui/pc/pc_box_id.png"),
            x.toInt(), y.toInt(),
            126,
            15,
            0f, 0f,
            120,
            15,
            120,
            15
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Text.literal("Box ID: ${storageWidget.box+1}").bold(),
            x = x+60,
            y = y+3,
            centered = true,
            shadow = true
        )
    }

}
