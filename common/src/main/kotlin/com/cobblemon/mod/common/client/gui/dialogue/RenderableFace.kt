/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.FloatingState
import com.cobblemon.mod.common.entity.PosableEntity
import java.util.UUID
import kotlin.math.atan
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.util.Identifier
import org.joml.Quaternionf

/**
 * Some time of face that can be rendered in a dialogue.
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
sealed interface RenderableFace {
    fun render(drawContext: DrawContext, partialTicks: Float)
}

class PlayerRenderableFace(val playerId: UUID) : RenderableFace {
    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val entity = MinecraftClient.getInstance().world?.getPlayerByUuid(playerId) ?: return
        // All of the maths below is shamelessly stolen from InventoryScreen.drawEntity.
        // the -20 and 5 divided by 40 are for configuring the yaw and pitch tilt of the body and head respectively.
        // For more information, pray for divine inspiration or something idk.
        val f = atan((-20 / 40.0f).toDouble()).toFloat()
        val g = atan((5 / 40.0f).toDouble()).toFloat()
        val quaternionf = Quaternionf().rotateZ(Math.PI.toFloat())
        val quaternionf2 = Quaternionf().rotateX(g * 20.0f * (Math.PI.toFloat() / 180))
        quaternionf.mul(quaternionf2)
        val oldBodyYaw = entity.bodyYaw
        val oldEntityYaw = entity.yaw
        val oldPitch = entity.pitch
        val oldPrevHeadYaw = entity.prevHeadYaw
        val oldHeadYaw = entity.headYaw
        // Modifies the entity for rendering based on our f and g values
        entity.bodyYaw = 180.0f + f * 20.0f
        entity.setYaw(180.0f + f * 40.0f)
        entity.setPitch(-g * 20.0f)
        entity.headYaw = entity.yaw
        entity.prevHeadYaw = entity.yaw
        val size = 37
        val xOffset = 0
        val yOffset = 75
        InventoryScreen.drawEntity(drawContext, xOffset, yOffset, size, quaternionf, quaternionf2, entity)
        // Resets the entity
        entity.bodyYaw = oldBodyYaw
        entity.setYaw(oldEntityYaw)
        entity.setPitch(oldPitch)
        entity.prevHeadYaw = oldPrevHeadYaw
        entity.headYaw = oldHeadYaw
    }
}

class ReferenceRenderableFace(val entity: PosableEntity): RenderableFace {
    val state = entity.delegate as PosableState
    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val state = this.state
        if (state is PokemonClientDelegate) {
            state.currentAspects = state.currentEntity.pokemon.aspects
            drawPosablePortrait(
                species = state.currentEntity.pokemon.species,
                aspects = state.currentEntity.pokemon.aspects,
                matrixStack = drawContext.matrices,
                state = state,
                partialTicks = 0F // It's already being rendered potentially so we don't need to tick the state.
            )
        }
    }
}

class ArtificialRenderableFace(
    val modelType: String,
    val identifier: Identifier,
    val aspects: Set<String>
): RenderableFace {
    val species = PokemonSpecies.getByIdentifier(identifier) ?: run {
        Cobblemon.LOGGER.error("Unable to find species for $identifier for a dialogue face. Defaulting to first species.")
        PokemonSpecies.species.first()
    }
    val state = FloatingState()

    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val state = this.state
        state.currentAspects = aspects
        if (modelType == "pokemon") {
            drawPosablePortrait(
                species = species,
                aspects = aspects,
                matrixStack = drawContext.matrices,
                state = state,
                partialTicks = partialTicks
            )
        }
    }
}