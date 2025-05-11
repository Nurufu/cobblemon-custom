/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.pokeball

import com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PosablePokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokeBallModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

class PokeBallRenderer(context: EntityRendererFactory.Context) : EntityRenderer<EmptyPokeBallEntity>(context) {
    val model = PosablePokeBallModel().also {
        it.context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
    }
    override fun getTexture(pEntity: EmptyPokeBallEntity): Identifier {
        return PokeBallModelRepository.getTexture(pEntity.pokeBall.name, pEntity.aspects, (pEntity.delegate as EmptyPokeBallClientDelegate).animationSeconds)
    }

    override fun render(entity: EmptyPokeBallEntity, yaw: Float, partialTicks: Float, poseStack: MatrixStack, buffer: VertexConsumerProvider, packedLight: Int) {
        val model = PokeBallModelRepository.getPoser(entity.pokeBall.name, entity.aspects)
        this.model.posableModel = model
        this.model.posableModel.context = this.model.context
        this.model.setupEntityTypeContext(entity)
        this.model.context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
        poseStack.push()
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw))
        poseStack.scale(0.7F, -0.7F, -0.7F)
        val vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(buffer, RenderLayer.getEntityCutout(getTexture(entity)), false, false)

        val state = entity.delegate as EmptyPokeBallClientDelegate
        this.model.context.put(RenderContext.POSABLE_STATE, state)
        this.model.context.put(RenderContext.ASPECTS, entity.aspects)
        state.currentAspects = entity.aspects
        state.updatePartialTicks(partialTicks)
        model.setLayerContext(buffer, state, PokemonModelRepository.getLayers(entity.pokeBall.name, entity.aspects))
        this.model.setAngles(entity, 0f, 0f, entity.age + partialTicks, 0F, 0F)
        this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)

        model.green = 1F
        model.blue = 1F
        model.red = 1F

        model.resetLayerContext()

        poseStack.pop()
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight)
    }
}