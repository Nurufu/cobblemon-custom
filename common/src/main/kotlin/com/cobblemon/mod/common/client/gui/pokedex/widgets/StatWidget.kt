/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class Pokedex(pX: Int, pY: Int): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Text.literal("Stat")) {
    companion object {
        const val WIDTH = 331
        const val HEIGHT = 161
    }

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        TODO("Not yet implemented")
    }
}