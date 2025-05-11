/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.RestorationTankBlockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import com.cobblemon.mod.common.client.CobblemonBakingOverrides
import com.cobblemon.mod.common.client.render.models.blockbench.repository.FossilModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.parabolaFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.rerange
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.timeDilate
import com.cobblemon.mod.common.util.cobblemonResource
import kotlin.math.pow
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

class RestorationTankRenderer(ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<RestorationTankBlockEntity> {
    val context = RenderContext().also {
        it.put(RenderContext.DO_QUIRKS, true)
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.RESURRECTION_MACHINE)
    }

    override fun render(
        entity: RestorationTankBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (entity.multiblockStructure == null) {
            return
        }
        val struct = entity.multiblockStructure as FossilMultiblockStructure
        val connectionDir = struct.tankConnectorDirection
        // FYI, rendering models this way ignores the pivots set in the model, so set the pivots manually
        when (connectionDir) {
            Direction.NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0f), 0.5f, 0f, 0.5f)
            Direction.EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270f), 0.5f, 0f, 0.5f)
            Direction.SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f), 0.5f, 0f, 0.5f)
            Direction.WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f), 0.5f, 0f, 0.5f)
            else -> {}
        }

        val cutoutBuffer = vertexConsumers.getBuffer(RenderLayer.getCutout())
        if (connectionDir != null) {
            matrices.push()
            CONNECTOR_MODEL.getQuads(entity.cachedState, null, entity.world?.random).forEach { quad ->
                cutoutBuffer.quad(matrices.peek(), quad, 0.75f, 0.75f, 0.75f, light, OverlayTexture.DEFAULT_UV)
            }
            matrices.pop()
        }
        val fillLevel = struct.fillLevel
        if (fillLevel == 0 && !struct.hasCreatedPokemon) {
            return
        }

        if (struct.isRunning() or (struct.hasCreatedPokemon)) {
            renderFetus(entity, tickDelta, matrices, vertexConsumers, light, overlay)
        }

        matrices.push()
        val transparentBuffer = vertexConsumers.getBuffer(RenderLayer.getTranslucent())

        val fluidModel = if (struct.isRunning()) FLUID_MODELS[8]
        else if (struct.hasCreatedPokemon) FLUID_MODELS[7]
        else FLUID_MODELS[fillLevel.coerceAtMost(FLUID_MODELS.size - 1) - 1]
        fluidModel.getQuads(entity.cachedState, null, entity.world?.random).forEach { quad ->
            transparentBuffer?.quad(matrices.peek(), quad, 0.75f, 0.75f, 0.75f, light, OverlayTexture.DEFAULT_UV)
        }

        matrices.pop()
    }

    private fun renderFetus(
            entity: RestorationTankBlockEntity,
            tickDelta: Float,
            matrices: MatrixStack,
            vertexConsumers: VertexConsumerProvider,
            light: Int,
            overlay: Int
    ) {
        val struc = entity.multiblockStructure as? FossilMultiblockStructure ?: return
        val fossil = struc.resultingFossil ?: return
        val timeRemaining = struc.timeRemaining

        val tankBlockState = entity.world?.getBlockState(entity.pos) ?: return
        if (tankBlockState.block != CobblemonBlocks.RESTORATION_TANK) {
            // Block has been destroyed/replaced
            return
        }
        val tankDirection = tankBlockState.get(HorizontalFacingBlock.FACING)
        val struct = entity.multiblockStructure as FossilMultiblockStructure
        val connectionDir = struct.tankConnectorDirection

        val aspects = emptySet<String>()
        val state = struc.fossilState
        state.updatePartialTicks(tickDelta)

        val completionPercentage = (1 - timeRemaining / FossilMultiblockStructure.TIME_TO_TAKE.toFloat()).coerceIn(0F, 1F)
        val fossilFetusModel = FossilModelRepository.getPoser(fossil.identifier, aspects)
        fossilFetusModel.context = context

        val embryo1Scale = EMBRYO_CURVE_1(completionPercentage)
        val embryo2Scale = EMBRYO_CURVE_2(completionPercentage)
        val embryo3Scale = EMBRYO_CURVE_3(completionPercentage)
        val fossilScale = FOSSIL_CURVE(completionPercentage)

        val identifiersAndScales = listOf(
                Pair(EMBRYO_IDENTIFIERS[0], embryo1Scale),
                Pair(EMBRYO_IDENTIFIERS[1], embryo2Scale),
                Pair(EMBRYO_IDENTIFIERS[2], embryo3Scale),
                Pair(fossil.identifier, fossilScale)
        )

        identifiersAndScales.forEach { (identifier, scale) ->
            val model = FossilModelRepository.getPoser(identifier, aspects)
            val texture = FossilModelRepository.getTexture(identifier, aspects, state.animationSeconds)

            if (scale > 0F) {
                val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture))
                state.currentModel = model
                state.currentAspects = aspects
                state.setPoseToFirstSuitable()

                val scale = if (timeRemaining == 0) {
                    model.maxScale
                } else {
                    scale * model.maxScale
                }

                matrices.push()
                matrices.translate(0.5, 1.0 + fossilFetusModel.yTranslation,  0.5);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180F))
                if (tankDirection.rotateCounterclockwise(Direction.Axis.Y) == connectionDir) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90F))
                } else if (tankDirection == connectionDir) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F))
                } else if (tankDirection.opposite != connectionDir) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F))
                }

                model.context = context
                context.put(RenderContext.TEXTURE, texture)
                context.put(RenderContext.SPECIES, fossil.identifier)
                context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.RESURRECTION_MACHINE)
                context.put(RenderContext.POSABLE_STATE, state)

                matrices.push()
                matrices.scale(scale, scale, scale)
                matrices.translate(0.0, model.yGrowthPoint.toDouble(), 0.0)
                model.applyAnimations(
                    entity = null,
                    state = state,
                    headYaw = 0F,
                    headPitch = 0F,
                    limbSwing = 0F,
                    limbSwingAmount = 0F,
                    ageInTicks = state.animationSeconds * 20
                )
                model.render(context, matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f)
                model.withLayerContext(vertexConsumers, state, FossilModelRepository.getLayers(fossil.identifier, aspects)) {
                    model.render(context, matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
                }
                model.setDefault()
                matrices.pop()
                matrices.pop()
            }

        }
    }

    companion object {
        val FLUID_MODELS = listOf(
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_1.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_2.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_3.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_4.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_5.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_6.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_7.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_CHUNKED_8.getModel(),
            CobblemonBakingOverrides.RESTORATION_TANK_FLUID_BUBBLING.getModel()
        )

        val CONNECTOR_MODEL = CobblemonBakingOverrides.RESTORATION_TANK_CONNECTOR.getModel()

        val EMBRYO_IDENTIFIERS = listOf(
            cobblemonResource("embryo_stage1"),
            cobblemonResource("embryo_stage2"),
            cobblemonResource("embryo_stage3")
        )

        val EMBRYO_CURVE_1 = parabolaFunction(peak = 0.5F, period = 1F).rerange(0.0F, 0.8F).timeDilate(2.5F)
        val EMBRYO_CURVE_2 = parabolaFunction(peak = 0.9F, period = 1F).rerange(0.2F, 1.2F).timeDilate(2.5F)
        val EMBRYO_CURVE_3 = parabolaFunction(peak = 1F, period = 1F).rerange(0.6F, 1.4F).timeDilate(2.5F)
        val FOSSIL_CURVE: WaveFunction = { t: Float -> -0.4F * (t - 2.5F).pow(2) + 1F }.timeDilate(2.5F)

    }
}