/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.api.gui.drawText
import com.cobblemon.mod.common.api.gui.drawTextJustifiedRight
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.CobblemonResources
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix3f
import org.joml.Matrix4f

fun renderImage(texture: Identifier, x: Double, y: Double, height: Double, width: Double) {
    val textureManager = MinecraftClient.getInstance().textureManager

    val buffer = Tessellator.getInstance().buffer
    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
    textureManager.bindTexture(texture)

    buffer.vertex(x, y + height, 0.0).texture(0f, 1f).next()
    buffer.vertex(x + width, y + height, 0.0).texture(1f, 1f).next()
    buffer.vertex(x + width, y, 0.0).texture(1f, 0f).next()
    buffer.vertex(x, y, 0.0).texture(0f, 0f).next()

    Tessellator.getInstance().draw()
}

fun renderScaledGuiItemIcon(itemStack: ItemStack, x: Double, y: Double, scale: Double = 1.0, zTranslation: Float = 100.0F, matrixStack: MatrixStack? = null) {
    val itemRenderer = MinecraftClient.getInstance().itemRenderer
    val textureManager = MinecraftClient.getInstance().textureManager
    val model = itemRenderer.getModel(itemStack, null, null, 0)

    textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false)
    RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
    RenderSystem.enableBlend()
    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    val modelViewStack = matrixStack ?: RenderSystem.getModelViewStack()
    modelViewStack.push()
    modelViewStack.translate(x, y, (zTranslation + 0).toDouble())
    modelViewStack.translate(8.0 * scale, 8.0 * scale, 0.0)
    modelViewStack.scale(1.0F, -1.0F, 1.0F)
    modelViewStack.scale(16.0F * scale.toFloat(), 16.0F * scale.toFloat(), 16.0F * scale.toFloat())
    RenderSystem.applyModelViewMatrix()

    val stack = matrixStack ?: MatrixStack()
    val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val bl = !model.isSideLit
    if (bl) DiffuseLighting.disableGuiDepthLighting()

    itemRenderer.renderItem(
        itemStack,
        ModelTransformationMode.GUI,
        false,
        stack,
        immediate,
        15728880,
        OverlayTexture.DEFAULT_UV,
        model
    )

    immediate.draw()
    RenderSystem.enableDepthTest()
    if (bl) DiffuseLighting.enableGuiDepthLighting()

    modelViewStack.pop()
    RenderSystem.applyModelViewMatrix()
}

fun getDepletableRedGreen(
    ratio: Float,
    yellowRatio: Float = 0.5F,
    redRatio: Float = 0.2F
): Pair<Float, Float> {
    val m = -2

    val r = if (ratio > redRatio) {
        m * ratio - m
    } else {
        1.0
    }

    val g = if (ratio > yellowRatio) {
        1.0
    } else if (ratio > redRatio) {
        ratio * 1 / yellowRatio
    } else {
        0.0
    }

    return r.toFloat() to g.toFloat()
}


fun drawScaledText(
    context: DrawContext,
    font: Identifier? = null,
    text: MutableText,
    x: Number,
    y: Number,
    scale: Float = 1F,
    opacity: Number = 1F,
    maxCharacterWidth: Int = Int.MAX_VALUE,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    centered: Boolean = false,
    shadow: Boolean = false,
    pMouseX: Int? = null,
    pMouseY: Int? = null
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }

    val textWidth = MinecraftClient.getInstance().textRenderer.getWidth(if (font != null) text.font(font) else text)
    val extraScale = if (textWidth < maxCharacterWidth) 1F else (maxCharacterWidth / textWidth.toFloat())
    val fontHeight = if (font == null) 5 else 6
    val matrices = context.matrices
    matrices.push()
    matrices.scale(scale * extraScale, scale * extraScale, 1F)
    val isHovered = drawText(
        context = context,
        font = font,
        text = text,
        x = x.toFloat() / (scale * extraScale),
        y = y.toFloat() / (scale * extraScale) + (1 - extraScale) * fontHeight * scale,
        centered = centered,
        colour = colour,
        shadow = shadow,
        pMouseX = pMouseX?.toFloat()?.div((scale * extraScale)),
        pMouseY = pMouseY?.toFloat()?.div(scale * extraScale)?.plus((1 - extraScale) * fontHeight * scale)
    )
    matrices.pop()
    // Draw tooltip that was created with onHover and is attached to the MutableText
    if (isHovered) {
        context.drawHoverEvent(MinecraftClient.getInstance().textRenderer, text.style, pMouseX!!, pMouseY!!)
    }
}

fun drawScaledText(
    context: DrawContext,
    text: OrderedText,
    x: Number,
    y: Number,
    scaleX: Float = 1F,
    scaleY: Float = 1F,
    opacity: Number = 1F,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    centered: Boolean = false,
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val matrixStack = context.matrices
    matrixStack.push()
    matrixStack.scale(scaleX, scaleY, 1F)
    drawText(
        context = context,
        text = text,
        x = x.toFloat() / scaleX,
        y = y.toFloat() / scaleY,
        centered = centered,
        colour = colour,
        shadow = shadow
    )
    matrixStack.pop()
}

fun drawScaledTextJustifiedRight(
    context: DrawContext,
    font: Identifier? = null,
    text: MutableText,
    x: Number,
    y: Number,
    scale: Float = 1F,
    opacity: Number = 1F,
    maxCharacterWidth: Int = Int.MAX_VALUE,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val textWidth = MinecraftClient.getInstance().textRenderer.getWidth(if (font != null) text.font(font) else text)
    val extraScale = if (textWidth < maxCharacterWidth) 1F else (maxCharacterWidth / textWidth.toFloat())
    val fontHeight = if (font == null) 5F else 6F
    val matrixStack = context.matrices
    matrixStack.push()
    matrixStack.scale(scale * extraScale, scale * extraScale, 1F)
    drawTextJustifiedRight(
        context = context,
        font = font,
        text = text,
        x = x.toFloat() / (scale * extraScale),
        y = y.toFloat() / (scale * extraScale) + (1F - extraScale) * fontHeight * scale,
        colour = colour,
        shadow = shadow
    )
    matrixStack.pop()
}

fun drawScaledTextJustifiedRight(
    context: DrawContext,
    text: MutableText,
    x: Number,
    y: Number,
    scaleX: Float = 1F,
    scaleY: Float = 1F,
    opacity: Number = 1F,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val matrixStack = context.matrices
    matrixStack.push()
    matrixStack.scale(scaleX, scaleY, 1F)
    drawTextJustifiedRight(
        context = context,
        text = text,
        x = x.toFloat() / scaleX,
        y = y.toFloat() / scaleY,
        colour = colour,
        shadow = shadow
    )
    matrixStack.pop()
}

fun renderBeaconBeam(
    matrixStack: MatrixStack,
    buffer: VertexConsumerProvider,
    textureLocation: Identifier = CobblemonResources.PHASE_BEAM,
    partialTicks: Float,
    totalLevelTime: Long,
    yOffset: Float = 0F,
    height: Float,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    beamRadius: Float,
    glowRadius: Float,
    glowAlpha: Float
) {
    val i = yOffset + height
    val beamRotation = Math.floorMod(totalLevelTime, 40).toFloat() + partialTicks
    matrixStack.push()
    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(beamRotation * 2.25f - 45.0f))
    var f9 = -beamRadius
    val f12 = -beamRadius
    renderPart(
        matrixStack,
        buffer.getBuffer(RenderLayer.getBeaconBeam(textureLocation, false)),
        red,
        green,
        blue,
        alpha,
        yOffset,
        i,
        0.0f,
        beamRadius,
        beamRadius,
        0.0f,
        f9,
        0.0f,
        0.0f,
        f12
    )
    // Undo the rotation so that the glow is at a rotated offset
    matrixStack.pop()
    val f6 = -glowRadius
    val f7 = -glowRadius
    val f8 = -glowRadius
    f9 = -glowRadius
    renderPart(
        matrixStack,
        buffer.getBuffer(RenderLayer.getBeaconBeam(textureLocation, true)),
        red,
        green,
        blue,
        glowAlpha,
        yOffset,
        i,
        f6,
        f7,
        glowRadius,
        f8,
        f9,
        glowRadius,
        glowRadius,
        glowRadius
    )
}

fun renderPart(
    matrixStack: MatrixStack,
    vertexBuffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    yMin: Float,
    yMax: Float,
    p_112164_: Float,
    p_112165_: Float,
    p_112166_: Float,
    p_112167_: Float,
    p_112168_: Float,
    p_112169_: Float,
    p_112170_: Float,
    p_112171_: Float
) {
    val pose = matrixStack.peek()
    val matrix4f = pose.positionMatrix
    val matrix3f = pose.normalMatrix
    renderQuad(
        matrix4f,
        matrix3f,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112164_,
        p_112165_,
        p_112166_,
        p_112167_
    )
    renderQuad(
        matrix4f,
        matrix3f,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112170_,
        p_112171_,
        p_112168_,
        p_112169_
    )
    renderQuad(
        matrix4f,
        matrix3f,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112166_,
        p_112167_,
        p_112170_,
        p_112171_
    )
    renderQuad(
        matrix4f,
        matrix3f,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112168_,
        p_112169_,
        p_112164_,
        p_112165_
    )
}

fun renderQuad(
    matrixPos: Matrix4f,
    matrixNormal: Matrix3f,
    buffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    yMin: Float,
    yMax: Float,
    x1: Float,
    z1: Float,
    x2: Float,
    z2: Float
) {
    addVertex(matrixPos, matrixNormal, buffer, red, green, blue, alpha, yMax, x1, z1, 1F, 0F)
    addVertex(matrixPos, matrixNormal, buffer, red, green, blue, alpha, yMin, x1, z1, 1F, 1F)
    addVertex(matrixPos, matrixNormal, buffer, red, green, blue, alpha, yMin, x2, z2, 0F, 1F)
    addVertex(matrixPos, matrixNormal, buffer, red, green, blue, alpha, yMax, x2, z2, 0F, 0F)
}

fun addVertex(
    matrixPos: Matrix4f,
    matrixNormal: Matrix3f,
    buffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    y: Float,
    x: Float,
    z: Float,
    texU: Float,
    texV: Float
) {
    buffer
        .vertex(matrixPos, x, y, z)
        .color(red, green, blue, alpha)
        .texture(texU, texV)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(15728880)
        .normal(matrixNormal, 0.0f, 1.0f, 0.0f)
        .next()
}