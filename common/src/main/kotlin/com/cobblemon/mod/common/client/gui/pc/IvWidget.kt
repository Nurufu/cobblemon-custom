/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.cobblemon.mod.common.client.gui.pc
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.text.MutableText
import net.minecraft.text.Text


class IvWidget(val gui: PCGUI) : Drawable {
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        var x = (gui.width - PCGUI.BASE_WIDTH) / 2.0
        var y = (gui.height - PCGUI.BASE_HEIGHT) / 2.0

        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        x -= 52
        y += 31.0

        context.drawTexture(
            cobblemonResource("textures/gui/pc/iv_display.png"),
            x.toInt(), y.toInt(),
            52,
            98,
            0f, 0f,
            52,
            98,
            52,
            98
        )

        val pokemon = gui.previewPokemon
        if (pokemon != null) {
            x += 9.5
            y += 9.5
            val iVs = pokemon.ivs
            drawText(context, Text.translatable("iv_display.hp").red(), x, y, mouseX, mouseY)
            val hp = iVs[Stats.HP].toString()
            textColor(context, Text.literal(hp), x + (if (hp.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.HP])
            y += 15.0
            drawText(context, Text.translatable("iv_display.attack").gold(), x, y, mouseX, mouseY)
            val attack = iVs[Stats.ATTACK].toString()
            textColor(context, Text.literal(attack), x + (if (attack.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.ATTACK])
            y += 15.0
            drawText(context, Text.translatable("iv_display.defense").yellow(), x, y, mouseX, mouseY)
            val defense = iVs[Stats.DEFENCE].toString()
            textColor(context, Text.literal(defense), x + (if (defense.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.DEFENCE])
            y += 15.0
            drawText(context, Text.translatable("iv_display.sp_attack").blue(), x, y, mouseX, mouseY)
            val spAttack = iVs[Stats.SPECIAL_ATTACK].toString()
            textColor(context, Text.literal(spAttack), x + (if (spAttack.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.SPECIAL_ATTACK])
            y += 15.0
            drawText(context, Text.translatable("iv_display.sp_defense").green(), x, y, mouseX, mouseY)
            val spDef = iVs[Stats.SPECIAL_DEFENCE].toString()
            textColor(context, Text.literal(spDef), x + (if (spDef.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.SPECIAL_DEFENCE])
            y += 15.0
            drawText(context, Text.translatable("iv_display.speed").lightPurple(), x, y, mouseX, mouseY)
            val speed = iVs[Stats.SPEED].toString()
            textColor(context, Text.literal(speed), x + (if (speed.length == 1) 30 else 27), y, mouseX, mouseY, iVs[Stats.SPEED])
        }
    }

    fun textColor(context: DrawContext, text: MutableText?, x: Double, y: Double, mouseX: Int, mouseY: Int, iv: Int?){
        when (iv){
            in 1..6 -> if (text != null) {
                drawText(
                    context,
                    text.red(),
                    x,
                    y,
                    mouseX,
                    mouseY
                )
            }
            in 26..30 -> if (text != null) {
                drawText(
                    context,
                    text.green(),
                    x,
                    y,
                    mouseX,
                    mouseY
                )
            }
            0 -> if (text != null) {
                drawText(
                    context,
                    text.darkPurple(),
                    x,
                    y,
                    mouseX,
                    mouseY
                )
            }
            31 -> if(text !=null){
                drawText(
                    context,
                    text.yellow(),
                    x,
                    y,
                    mouseX,
                    mouseY
                )
            }
            else -> if(text !=null){
                drawText(
                    context,
                    text.white(),
                    x,
                    y,
                    mouseX,
                    mouseY
                )
            }
        }
    }

    fun drawText(context: DrawContext?, text: MutableText?, x: Double, y: Double, mouseX: Int, mouseY: Int) {
        if (context != null) {
            if (text != null) {
                drawScaledText(
                    context,
                    null,
                    text,
                    x,
                    y,
                    PCGUI.SCALE,
                    1,
                    Int.MAX_VALUE,
                    0x00FFFFFF,
                    false,
                    true,
                    mouseX,
                    mouseY
                )
            }
        }
    }
}