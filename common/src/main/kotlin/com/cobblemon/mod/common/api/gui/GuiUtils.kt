/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.gui

import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_DIAMETER
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix4f
import org.joml.Vector3f

@JvmOverloads
fun blitk(
    matrixStack: MatrixStack,
    texture: Identifier? = null,
    x: Number,
    y: Number,
    height: Number = 0,
    width: Number = 0,
    uOffset: Number = 0,
    vOffset: Number = 0,
    textureWidth: Number = width,
    textureHeight: Number = height,
    blitOffset: Number = 0,
    red: Number = 1,
    green: Number = 1,
    blue: Number = 1,
    alpha: Number = 1F,
    blend: Boolean = true,
    scale: Float = 1F
) {
    RenderSystem.setShader { GameRenderer.getPositionTexProgram() }
    texture?.run { RenderSystem.setShaderTexture(0, this) }
    if (blend) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
    }
    RenderSystem.setShaderColor(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat())
    matrixStack.push()
    matrixStack.scale(scale, scale, 1F)
    drawRectangle(
        matrixStack.peek().positionMatrix,
        x.toFloat(), y.toFloat(), x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(),
        blitOffset.toFloat(),
        uOffset.toFloat() / textureWidth.toFloat(), (uOffset.toFloat() + width.toFloat()) / textureWidth.toFloat(),
        vOffset.toFloat() / textureHeight.toFloat(), (vOffset.toFloat() + height.toFloat()) / textureHeight.toFloat()
    )
    matrixStack.pop()
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
}

fun drawRectangle(
    matrix: Matrix4f,
    x: Float,
    y: Float,
    endX: Float,
    endY: Float,
    blitOffset: Float,
    minU: Float,
    maxU: Float,
    minV: Float,
    maxV: Float
) {
    val bufferbuilder = Tessellator.getInstance().buffer
    bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
    bufferbuilder.vertex(matrix, x, endY, blitOffset).texture(minU, maxV).next()
    bufferbuilder.vertex(matrix, endX, endY, blitOffset).texture(maxU, maxV).next()
    bufferbuilder.vertex(matrix, endX, y, blitOffset).texture(maxU, minV).next()
    bufferbuilder.vertex(matrix, x, y, blitOffset).texture(minU, minV).next()
    // TODO: Figure out if this is correct replacement.
    // OLD: BufferRenderer.draw(bufferbuilder)
    BufferRenderer.drawWithGlobalProgram(bufferbuilder.end())
}

@JvmOverloads
fun drawCenteredText(
    context: DrawContext,
    font: Identifier? = null,
    text: Text,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true
) {
    val comp = (text as MutableText).let { if (font != null) it.font(font) else it }
    val textRenderer = MinecraftClient.getInstance().textRenderer
    context.drawText(textRenderer, comp, x.toInt() - textRenderer.getWidth(comp) / 2, y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawText(
    context: DrawContext,
    font: Identifier? = null,
    text: MutableText,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true,
    pMouseX: Number? = null,
    pMouseY: Number? = null
): Boolean {
    val comp = if (font == null) text else text.setStyle(text.style.withFont(font))
    val textRenderer = MinecraftClient.getInstance().textRenderer
    var x = x
    val width = textRenderer.getWidth(comp)
    if (centered) {
        x = x.toDouble() - width / 2
    }
    context.drawText(textRenderer, comp, x.toInt(), y.toInt(), colour, shadow)
    var isHovered = false
    if (pMouseY != null && pMouseX != null) {
        if (pMouseX.toInt() >= x.toInt() && pMouseX.toInt() <= x.toInt() + width &&
            pMouseY.toInt() >= y.toInt() && pMouseY.toInt() <= y.toInt() + textRenderer.fontHeight
        ) {
            isHovered = true
        }
    }
    return isHovered
}

@JvmOverloads
fun drawText(
    context: DrawContext,
    text: OrderedText,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true
) {
    val textRenderer = MinecraftClient.getInstance().textRenderer
    var tweakedX = x
    if (centered) {
        val width = textRenderer.getWidth(text)
        tweakedX = tweakedX.toDouble() - width / 2
    }
    context.drawText(textRenderer, text, tweakedX.toInt(), y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawString(
    context: DrawContext,
    text: String,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true,
    font: Identifier? = null
) {
    val comp = Text.literal(text).also {
        font?.run {
            it.getWithStyle(it.style.withFont(this))
        }
    }
    val textRenderer = MinecraftClient.getInstance().textRenderer
    context.drawText(textRenderer, comp, x.toInt(), y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawPosablePortrait(
    identifier: Identifier,
    aspects: Set<String>,
    matrixStack: MatrixStack,
    scale: Float = 13F,
    contextScale: Float = 1F,
    reversed: Boolean = false,
    state: PosableState,
    repository: VaryingModelRepository<*>,
    partialTicks: Float,
    limbSwing: Float = 0F,
    limbSwingAmount: Float = 0F,
    ageInTicks: Float = 0F,
    headYaw: Float = 0F,
    headPitch: Float = 0F
) {
    val model = repository.getPoser(identifier, aspects)
    state.currentAspects = aspects
    state.currentModel = model
    val texture = repository.getTexture(identifier, aspects, state.animationSeconds)

    val context = RenderContext()
    model.context = context
    repository.getTextureNoSubstitute(identifier, aspects, 0f).let { context.put(RenderContext.TEXTURE, it) }
    context.put(RenderContext.SCALE, contextScale)
    context.put(RenderContext.SPECIES, identifier)
    context.put(RenderContext.ASPECTS, aspects)
    context.put(RenderContext.POSABLE_STATE, state)

    val renderType = RenderLayer.getEntityCutout(texture)

    RenderSystem.applyModelViewMatrix()
    val quaternion1 = RotationAxis.POSITIVE_Y.rotationDegrees(-32F * if (reversed) -1F else 1F)
    val quaternion2 = RotationAxis.POSITIVE_X.rotationDegrees(5F)

    val originalPose = state.currentPose
    state.setPoseToFirstSuitable(PoseType.PORTRAIT)
    state.updatePartialTicks(partialTicks)
    model.applyAnimations(null, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
    originalPose?.let { state.setPose(it) }

    matrixStack.push()
    matrixStack.translate(0.0, PORTRAIT_DIAMETER.toDouble() + 2.0, 0.0)
    matrixStack.scale(scale, scale, -scale)
    matrixStack.translate(0.0, -PORTRAIT_DIAMETER / 18.0, 0.0)
    matrixStack.translate(model.portraitTranslation.x * if (reversed) -1F else 1F, model.portraitTranslation.y, model.portraitTranslation.z - 4)
    matrixStack.scale(model.portraitScale, model.portraitScale, 1 / model.portraitScale)
    matrixStack.multiply(quaternion1)
    matrixStack.multiply(quaternion2)

    val light1 = Vector3f(0.2F, 1.0F, -1.0F)
    val light2 = Vector3f(0.1F, 0.0F, 8.0F)
    RenderSystem.setShaderLights(light1, light2)
    quaternion1.conjugate()

    val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val buffer = immediate.getBuffer(renderType)
    val packedLight = LightmapTextureManager.pack(11, 7)

    model.withLayerContext(immediate, state, repository.getLayers(identifier, aspects)) {
        model.render(context, matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        immediate.draw()
    }

    matrixStack.pop()
    model.setDefault()

    DiffuseLighting.enableGuiDepthLighting()
}

fun drawProfile(
    repository: VaryingModelRepository<*>,
    resourceIdentifier: Identifier,
    aspects: Set<String>,
    matrixStack: MatrixStack,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F
) {
    val model = repository.getPoser(resourceIdentifier, aspects)
    val texture = repository.getTexture(resourceIdentifier, aspects, state.animationSeconds)

    val context = RenderContext()
    model.context = context
    repository.getTextureNoSubstitute(resourceIdentifier, aspects, 0f).let { context.put(RenderContext.TEXTURE, it) }
    context.put(RenderContext.SCALE, 1F)
    context.put(RenderContext.SPECIES, resourceIdentifier)
    context.put(RenderContext.ASPECTS, aspects)
    context.put(RenderContext.POSABLE_STATE, state)
    state.currentAspects = aspects
    state.currentModel = model

    val renderType = RenderLayer.getEntityCutout(texture)//model.getLayer(texture)

    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    state.setPoseToFirstSuitable(PoseType.PORTRAIT)
    state.updatePartialTicks(partialTicks)
    model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)
    matrixStack.translate(model.profileTranslation.x, model.profileTranslation.y,  model.profileTranslation.z - 4.0)
    matrixStack.scale(model.profileScale, model.profileScale, 1 / model.profileScale)
//    matrixStack.multiply(rotation)
    val quaternion1 = RotationAxis.POSITIVE_Y.rotationDegrees(-32F * if (false) -1F else 1F)
    val quaternion2 = RotationAxis.POSITIVE_X.rotationDegrees(5F)
    matrixStack.multiply(quaternion1)
    matrixStack.multiply(quaternion2)
    DiffuseLighting.method_34742()
    val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
    entityRenderDispatcher.setRenderShadows(true)

    val bufferSource = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val buffer = bufferSource.getBuffer(renderType)
    val light1 = Vector3f(-1F, 1F, 1.0F)
    val light2 = Vector3f(1.3F, -1F, 1.0F)
    RenderSystem.setShaderLights(light1, light2)
    val packedLight = LightmapTextureManager.pack(11, 7)

    model.withLayerContext(bufferSource, state, repository.getLayers(resourceIdentifier, aspects)) {
        model.render(context, matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        bufferSource.draw()
    }
    model.setDefault()
    entityRenderDispatcher.setRenderShadows(true)
    DiffuseLighting.enableGuiDepthLighting()
}