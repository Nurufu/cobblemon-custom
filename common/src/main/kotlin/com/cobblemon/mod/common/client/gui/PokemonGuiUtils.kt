/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f

fun drawProfilePokemon(
    renderablePokemon: RenderablePokemon,
    matrixStack: MatrixStack,
    rotation: Quaternionf,
    state: PosableState<PokemonEntity>?,
    partialTicks: Float,
    scale: Float = 20F
) = drawProfilePokemon(
    species = renderablePokemon.species.resourceIdentifier,
    aspects = renderablePokemon.aspects,
    matrixStack = matrixStack,
    rotation = rotation,
    state = state,
    partialTicks = partialTicks,
    scale = scale
)

fun drawProfilePokemon(
    species: Identifier,
    aspects: Set<String>,
    matrixStack: MatrixStack,
    rotation: Quaternionf,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F
) {
    val model = PokemonModelRepository.getPoser(species, aspects)
    val texture = PokemonModelRepository.getTexture(species, aspects, state.animationSeconds)

    model.context = context
    PokemonModelRepository.getTextureNoSubstitute(species, aspects, 0f).let { context.put(RenderContext.TEXTURE, it) }

    context.put(RenderContext.SCALE, PokemonSpecies.getByIdentifier(species)!!.getForm(aspects).baseScale)
    context.put(RenderContext.SPECIES, species)
    context.put(RenderContext.ASPECTS, aspects)
    context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.PROFILE)
    context.put(RenderContext.POSABLE_STATE, state)


    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    state.setPoseToFirstSuitable(PoseType.PROFILE)
    state.updatePartialTicks(partialTicks)
    model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)
    matrixStack.translate(model.profileTranslation.x, model.profileTranslation.y,  model.profileTranslation.z - 4.0)
    matrixStack.scale(model.profileScale, model.profileScale, 1 / model.profileScale)

    matrixStack.multiply(rotation)
    DiffuseLighting.method_34742()
    val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
    rotation.conjugate()
    entityRenderDispatcher.rotation = rotation
    entityRenderDispatcher.setRenderShadows(true)

    val bufferSource = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val buffer = bufferSource.getBuffer(renderType)
    val light1 = Vector3f(-1F, 1F, 1.0F)
    val light2 = Vector3f(1.3F, -1F, 1.0F)
    RenderSystem.setShaderLights(light1, light2)
    val packedLight = LightmapTextureManager.pack(11, 7)

    model.withLayerContext(bufferSource, state, PokemonModelRepository.getLayers(species, aspects)) {
        model.render(context, matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        bufferSource.draw()
    }
    model.setDefault()
    entityRenderDispatcher.setRenderShadows(true)
    DiffuseLighting.enableGuiDepthLighting()
}

