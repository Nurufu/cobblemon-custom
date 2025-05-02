/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.entity.boat.CobblemonBoatEntity
import com.cobblemon.mod.common.entity.boat.CobblemonChestBoatEntity
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.platform.PlatformRegistry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object CobblemonEntities : PlatformRegistry<Registry<EntityType<*>>, RegistryKey<Registry<EntityType<*>>>, EntityType<*>>() {


    override val registry: Registry<EntityType<*>> = Registries.ENTITY_TYPE
    override val registryKey: RegistryKey<Registry<EntityType<*>>> = RegistryKeys.ENTITY_TYPE
    @JvmField
    val POKEMON_KEY = cobblemonResource("pokemon")
    @JvmField
    val POKEMON: EntityType<PokemonEntity> = this.create(
        POKEMON_KEY.path,
        EntityType.Builder.create({ _, world -> PokemonEntity(world) }, SpawnGroup.CREATURE)
            .build(POKEMON_KEY.toString())
    )

    @JvmField
    val EMPTY_POKEBALL_KEY = cobblemonResource("empty_pokeball")
    @JvmField
    val EMPTY_POKEBALL: EntityType<EmptyPokeBallEntity> = this.create(
        EMPTY_POKEBALL_KEY.path,
        EntityType.Builder.create({ _, world -> EmptyPokeBallEntity(PokeBalls.POKE_BALL, world) }, SpawnGroup.MISC)
            .build(EMPTY_POKEBALL_KEY.toString())
    )

    @JvmField
    val BOAT_KEY = cobblemonResource("boat")
    @JvmField
    val BOAT: EntityType<CobblemonBoatEntity> = this.create(
        BOAT_KEY.path,
        EntityType.Builder.create(::CobblemonBoatEntity, SpawnGroup.MISC).setDimensions(1.375F, 0.5625F).maxTrackingRange(10)
            .build(BOAT_KEY.toString())
    )

    @JvmField
    val CHEST_BOAT_KEY = cobblemonResource("chest_boat")
    @JvmField
    val CHEST_BOAT: EntityType<CobblemonChestBoatEntity> = this.create(
        CHEST_BOAT_KEY.path,
        EntityType.Builder.create(::CobblemonChestBoatEntity, SpawnGroup.MISC).setDimensions(1.375F, 0.5625F).maxTrackingRange(10)
            .build(CHEST_BOAT_KEY.toString())
    )

    @JvmField
    val POKE_BOBBER_KEY = cobblemonResource("poke_bobber")
    @JvmField
    val POKE_BOBBER: EntityType<PokeRodFishingBobberEntity> = this.create(
        POKE_BOBBER_KEY.path,
        EntityType.Builder.create(::PokeRodFishingBobberEntity, SpawnGroup.MISC).setDimensions(0.5F,0.5F).maxTrackingRange(10)
            .build(POKE_BOBBER_KEY.toString())
    )

    @JvmField
    val GENERIC_BEDROCK_ENTITY_KEY = cobblemonResource("generic_bedrock")
    @JvmField
    val GENERIC_BEDROCK_ENTITY: EntityType<GenericBedrockEntity> = this.create(
        GENERIC_BEDROCK_ENTITY_KEY.path,
        EntityType.Builder.create({ _, world -> GenericBedrockEntity(world) }, SpawnGroup.MISC)
            .build(GENERIC_BEDROCK_ENTITY_KEY.toString())
    )

    fun registerAttributes(consumer: (EntityType<out LivingEntity>, DefaultAttributeContainer.Builder) -> Unit) {
        consumer(POKEMON, PokemonEntity.createAttributes())
    }

}