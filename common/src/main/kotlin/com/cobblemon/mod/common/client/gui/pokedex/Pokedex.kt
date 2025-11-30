/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.client.gui.pokedex.widgets.ScrollWidget
import com.cobblemon.mod.common.client.storage.ClientPokedex
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class Pokedex private constructor(val pokedex: ClientPokedex) : Screen(Text.translatable("cobblemon.ui.pokedex.title")) {

    companion object {
        const val BASE_WIDTH = 331
        const val BASE_HEIGHT = 161
        private const val PORTRAIT_SIZE = 66
        private const val SCALE = 0.5F


        /**
         * Attempts to open this screen for a client.
         */
        fun open(pokedexList: ClientPokedex) {
            val mc = MinecraftClient.getInstance()
            val screen = Pokedex(pokedexList)
            mc.setScreen(screen)
        }
    }

    private var index = 0
    private lateinit var scrollScreen : ScrollWidget

    public override fun init() {
        super.init()
        clearChildren()

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        displayScroll()
        LOGGER.info("entries" + pokedex.pokedexEntries.toString())
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

        super.render(context, mouseX, mouseY, delta)
    }

    private fun displayScroll(){
        if (::scrollScreen.isInitialized) remove(scrollScreen)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        scrollScreen = ScrollWidget(x, y)

        addDrawableChild(scrollScreen)
    }

    override fun shouldPause(): Boolean = false
}