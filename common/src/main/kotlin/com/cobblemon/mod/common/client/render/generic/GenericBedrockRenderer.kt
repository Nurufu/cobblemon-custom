/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.generic

import com.cobblemon.mod.common.client.entity.GenericBedrockClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.generic.PosableGenericEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.GenericBedrockEntityModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis

class GenericBedrockRenderer(context: EntityRendererFactory.Context) : EntityRenderer<GenericBedrockEntity>(context) {
    val model = PosableGenericEntityModel()
    override fun getTexture(entity: GenericBedrockEntity) = GenericBedrockEntityModelRepository.getTexture(entity.category, entity.aspects, (entity.delegate as GenericBedrockClientDelegate).animationSeconds)
    override fun render(entity: GenericBedrockEntity, yaw: Float, partialTicks: Float, poseStack: MatrixStack, buffer: VertexConsumerProvider, packedLight: Int) {
        if (entity.isInvisible) {
            return
        }

        val model = GenericBedrockEntityModelRepository.getPoser(entity.category, entity.aspects)
        this.model.posableModel = model
        poseStack.push()
        poseStack.scale(1.0F, -1.0F, 1.0F)
        poseStack.scale(entity.scale, entity.scale, entity.scale)
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw))
        val vertexConsumer = buffer.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)))

        val state = entity.delegate as GenericBedrockClientDelegate
        state.updatePartialTicks(partialTicks)
        model.setLayerContext(buffer, state, PokemonModelRepository.getLayers(entity.category, entity.aspects))
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