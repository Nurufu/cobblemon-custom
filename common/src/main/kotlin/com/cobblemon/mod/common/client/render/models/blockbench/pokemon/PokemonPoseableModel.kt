/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

/**
 * A poseable model for a Pokémon. Just handles the state accessor to the [PokemonClientDelegate].
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
abstract class PokemonPoseableModel : PoseableEntityModel<PokemonEntity>() {
    override val isForLivingEntityRenderer = true
    override fun getState(entity: PokemonEntity) = entity.delegate as PokemonClientDelegate

    /** Registers the same configuration for both left and right shoulder poses. */
    fun <F : ModelFrame> registerShoulderPoses(
        transformTicks: Int = 30,
        idleAnimations: Array<StatelessAnimation<PokemonEntity, out F>>,
        transformedParts: Array<ModelPartTransformation> = emptyArray()
    ) {
        registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            transformTicks = transformTicks,
            idleAnimations = idleAnimations,
            transformedParts = transformedParts
        )

        registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            transformTicks = transformTicks,
            idleAnimations = idleAnimations,
            transformedParts = transformedParts
        )
    }

    @Transient
    open var portraitScale: Float = 1F
    @Transient
    open var portraitTranslation: Vec3d = Vec3d.ZERO

    @Transient
    open var profileScale: Float = 1F
    @Transient
    open var profileTranslation: Vec3d = Vec3d.ZERO

    open fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>
    ): StatefulAnimation<PokemonEntity, ModelFrame>? = null

    open fun getEatAnimation(
        pokemonEntity: PokemonEntity,
        state: PosableState<PokemonEntity>
    ): StatefulAnimation<PokemonEntity, ModelFrame>? = null

    override fun getOverlayTexture(entity: Entity?): Int {
        return if (entity is PokemonEntity) {
            OverlayTexture.packUv(
                OverlayTexture.getU(0F),
                OverlayTexture.getV(entity.hurtTime > 0)
            )
        } else  {
            OverlayTexture.DEFAULT_UV
        }
    }

    open val cryAnimation: CryProvider = CryProvider { _, _ -> null }

    override fun setupEntityTypeContext(entity: PokemonEntity?) {
        entity?.let {
            context.put(RenderContext.SCALE, it.pokemon.form.baseScale)
            context.put(RenderContext.SPECIES, it.pokemon.species.resourceIdentifier)
            context.put(RenderContext.ASPECTS, it.pokemon.aspects)
            PokemonModelRepository.getTexture(it.pokemon.species.resourceIdentifier, it.pokemon.aspects, 0f).let { texture -> context.put(RenderContext.TEXTURE, texture) }
        }
    }
}

typealias PokemonPose = Pose<PokemonEntity, ModelFrame>

@FunctionalInterface
fun interface CryProvider {
    operator fun invoke(entity: PokemonEntity, state: PosableState<PokemonEntity>): StatefulAnimation<PokemonEntity, ModelFrame>?
}