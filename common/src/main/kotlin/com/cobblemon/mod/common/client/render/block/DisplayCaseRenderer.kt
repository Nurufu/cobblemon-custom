/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.block.DisplayCaseBlock
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.item.PokemonItem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.BannerItem
import net.minecraft.item.BedItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtInt
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import net.minecraft.world.World

class DisplayCaseRenderer(ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<DisplayCaseBlockEntity> {
    val context = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
    }
    val coinPouchStack: ItemStack by lazy { ItemStack(CobblemonItems.RELIC_COIN_POUCH).also { it.setSubNbt("CustomModelData", NbtInt.of(1)) } }
    override fun render(
        entity: DisplayCaseBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val stack: ItemStack = if (entity.getStack().isOf(CobblemonItems.RELIC_COIN_POUCH)) {
            coinPouchStack
        } else {
            entity.getStack()
        }
        val world = entity.world ?: return
        val posType = getPositioningType(stack, world)
        val blockState = if (entity.world != null) entity.cachedState
            else (CobblemonBlocks.DISPLAY_CASE.defaultState.with(DisplayCaseBlock.ITEM_DIRECTION, Direction.NORTH))
        val yRot = if (posType == PositioningType.ITEM_MODEL) blockState.get(DisplayCaseBlock.ITEM_DIRECTION).opposite.asRotation()
            else blockState.get(DisplayCaseBlock.ITEM_DIRECTION).asRotation()

        if (stack.item is PokemonItem) {
            renderPokemon(
                matrices,
                vertexConsumers,
                light,
                stack,
                yRot
            )
            return
        }

        matrices.push()
        matrices.translate(0.5f, 0.4f, 0.5f)

        matrices.scale(posType.scaleX, posType.scaleY, posType.scaleZ)
        matrices.translate(posType.transX, posType.transY, posType.transZ)

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yRot))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(posType.rotY))

        MinecraftClient.getInstance().itemRenderer.renderItem(
            stack,
            ModelTransformationMode.GROUND,
            light,
            overlay,
            matrices,
            vertexConsumers,
            entity.world,
            0
        )

        matrices.pop()

    }

    private fun renderPokemon(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        stack: ItemStack,
        yRot: Float
    ) {
        val item = stack.item as? PokemonItem ?: return
        val (species, aspects) = item.getSpeciesAndAspects(stack) ?: return
        val model = PokemonModelRepository.getPoser(species.resourceIdentifier, aspects)
        val texture = PokemonModelRepository.getTexture(species.resourceIdentifier, aspects, 0F)
        val renderLayer = RenderLayer.getEntityCutout(texture)//model.getLayer(texture)
        val tint = item.tint(stack)
        val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(renderLayer)
        val scale = 0.25f

        matrices.push()
        matrices.scale(1f, -1f, -1f)
        matrices.translate(0.5f, -0.69f, -0.5f)
        matrices.scale(scale, scale, scale)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot))

        val state = FloatingState()
        state.currentAspects = aspects
        model.context = context
        context.put(RenderContext.SCALE, scale)
        context.put(RenderContext.SPECIES, species.resourceIdentifier)
        context.put(RenderContext.ASPECTS, aspects)
        context.put(RenderContext.TEXTURE, texture)
        context.put(RenderContext.POSABLE_STATE, state)
        state.currentPose = model.getFirstSuitablePose(state, PoseType.PROFILE).poseName


        model.applyAnimations(
            entity = null,
            state = state,
            limbSwing = 0F,
            limbSwingAmount = 0F,
            ageInTicks = 0F,
            headYaw = 0F,
            headPitch = 0F
        )


        model.withLayerContext(vertexConsumers, state, PokemonModelRepository.getLayers(species.resourceIdentifier, aspects)) {
            model.render(context, matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, tint.x, tint.y, tint.z, tint.w)
        }

        matrices.pop()
    }

    companion object {
        private val mobHeads = listOf<Item>(
            Items.SKELETON_SKULL,
            Items.WITHER_SKELETON_SKULL,
            Items.ZOMBIE_HEAD,
            Items.PIGLIN_HEAD,
            Items.PLAYER_HEAD,
            Items.DRAGON_HEAD,
            Items.CREEPER_HEAD
        )

        private fun getPositioningType(stack: ItemStack, world: World) = when {
            mobHeads.contains(stack.item) -> PositioningType.MOB_HEAD
            stack.item is BedItem -> PositioningType.BED
            stack.item is BannerItem -> PositioningType.BANNER
            stack.item is PokeBallItem -> PositioningType.POKE_BALL
            stack.item == CobblemonItems.RELIC_COIN_POUCH -> PositioningType.COIN_POUCH
            stack.item == CobblemonItems.PASTURE -> PositioningType.PASTURE
            stack.item == CobblemonItems.POKEMON_MODEL -> PositioningType.ITEM_MODEL
            stack.item == Items.SHIELD -> PositioningType.SHIELD
            stack.item == Items.DECORATED_POT -> PositioningType.MOB_HEAD
            MinecraftClient.getInstance().itemRenderer.getModel(stack, world, null, 0).hasDepth() -> PositioningType.BLOCK_MODEL
            else -> PositioningType.ITEM_MODEL
        }
    }

    private enum class PositioningType(
        val scaleX: Float, val scaleY: Float, val scaleZ: Float,
        val transX: Float, val transY: Float, val transZ: Float,
        val rotY: Float = 0f
    ) {
        POKE_BALL(1f, 1f, 1f, 0f, 0.04f, 0f),
        BLOCK_MODEL(1f, 1f, 1f, 0f, -0.15f, 0f),
        ITEM_MODEL(1f, 1f, 1f, 0f, 0.04f, 0f),
        BED(1f, 1f, 1f, 0f, -0.02f, 0f),
        BANNER(1f, 1f, 1f, 0f, -0.02f, 0f, 180f),
        MOB_HEAD(1f, 1f, 1f, 0f, -0.025f, 0f, 180f),
        SHIELD(1f, 1f, 1f, 0f, -0.045f, 0f, 180f),
        PASTURE(1f, 1f, 1f, 0f, 0.0375f, 0f),
        COIN_POUCH(1f, 1f, 1f, 0f, 0.415f, 0f)
    }
}