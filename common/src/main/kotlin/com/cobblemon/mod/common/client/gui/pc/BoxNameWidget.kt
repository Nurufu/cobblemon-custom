/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.storage.ClientBox
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestRenamePCBoxPacket
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class BoxNameWidget(
    private val pX: Int,
    pY: Int,
    text: MutableText = "BoxNameWidget".text(),
    private val pcGui: PCGUI,
    private val storageWidget: StorageWidget,
): TextFieldWidget(MinecraftClient.getInstance().textRenderer, pX, pY, 0, HEIGHT, text) {

    companion object {
        const val HEIGHT = 14
    }

    init {
        setMaxLength(21)
        update()
        setChangedListener { update() }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (cursor != text.length) setCursorToEnd()

        drawScaledText(
            context = context,
            text = text(),
            x = x,
            y = y
        )
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        update()
        if (!focused) {
            RequestRenamePCBoxPacket(pcGui.pc.uuid, storageWidget.box, text).sendToServer()
            pcGui.pc.boxes[storageWidget.box].name = if (text.isBlank()) null else Text.literal(text).bold()
            text = ""
        }
    }

    private fun text(): MutableText {
        return (if (isFocused) {
            "${text}_".text()
        } else if (text.isEmpty()) {
            getBox().name?: Text.translatable("cobblemon.ui.pc.box.title", storageWidget.box + 1)
        } else {
            text.text()
        }).bold().font(CobblemonResources.DEFAULT_LARGE)
    }

    fun getBox(): ClientBox {
        return pcGui.pc.boxes[storageWidget.box]
    }

    fun update() {
        width = MinecraftClient.getInstance().textRenderer.getWidth(text())
        x = pX - width / 2
    }
}
