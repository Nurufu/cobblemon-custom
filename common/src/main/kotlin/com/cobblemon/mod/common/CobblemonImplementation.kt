/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.tags.CobbleRideTags
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.block.ComposterBlock
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.PlacedFeature
import kotlin.reflect.KClass

interface CobblemonImplementation {
    val modAPI: ModAPI

    /**
     *
     */
    val networkManager: NetworkManager

    /**
     * TODO
     *
     * @return
     */
    fun environment(): Environment

    /**
     * TODO
     *
     * @param id
     * @return
     */
    fun isModInstalled(id: String): Boolean

    /**
     * TODO
     *
     */
    fun registerPermissionValidator()

    /**
     * TODO
     *
     */
    fun registerSoundEvents()

    /**
     * TODO
     *
     */
    fun registerItems()

    /**
     * TODO
     *
     */
    fun registerBlocks()

    /**
     * TODO
     *
     */
    fun registerEntityTypes()

    /**
     * TODO
     *
     */
    fun registerEntityAttributes()

    /**
     * TODO
     *
     */
    fun registerBlockEntityTypes()

    /**
     * TODO
     *
     */
    fun registerWorldGenFeatures()

    fun registerParticles()

    /**
     * Add a feature to the current platform implementation.
     *
     * @param feature The [PlacedFeature] being added.
     * @param step The [GenerationStep.Feature] of this feature.
     * @param validTag The [TagKey] required by the [Biome] for this feature to generate in, if null all biomes are valid.
     */
    fun addFeatureToWorldGen(feature: RegistryKey<PlacedFeature>, step: GenerationStep.Feature, validTag: TagKey<Biome>?)

    /**
     * TODO
     *
     * @param A
     * @param T
     * @param identifier
     * @param argumentClass
     * @param serializer
     */
    fun <A : ArgumentType<*>, T : ArgumentSerializer.ArgumentTypeProperties<A>> registerCommandArgument(identifier: Identifier, argumentClass: KClass<A>, serializer: ArgumentSerializer<A, T>)

    /**
     * TODO
     *
     * @param T
     * @param name
     * @param category
     * @param type
     * @return
     */
    fun <T : GameRules.Rule<T>> registerGameRule(name: String, category: GameRules.Category, type: GameRules.Type<T>): GameRules.Key<T>

    /**
     * TODO
     *
     * @param T
     * @param criteria
     * @return
     */
    fun <T : Criterion<*>> registerCriteria(criteria: T): T

    /**
     * TODO
     *
     * @param identifier
     * @param reloader
     * @param type
     * @param dependencies
     */
    fun registerResourceReloader(identifier: Identifier, reloader: ResourceReloader, type: ResourceType, dependencies: Collection<Identifier>)

    /**
     * TODO
     *
     * @return
     */
    fun server(): MinecraftServer?

    /**
     * Handles platform independent reloading of a [JsonDataRegistry].
     *
     * @param registry The [JsonDataRegistry] to reload.
     * @param manager The [ResourceManager] to reload from.
     */
    fun <T> reloadJsonRegistry(registry: JsonDataRegistry<T>, manager: ResourceManager): HashMap<Identifier, T>

    /**
     * Registers an item to the [ComposterBlock].
     *
     * @param item The [ItemConvertible] being registered.
     * @param chance The chance % of increasing the composter level, 0 to 1 expected.
     */
    fun registerCompostable(item: ItemConvertible, chance: Float)

    /**
     * Registers a builtin resource pack.
     *
     * @param id The unique [Identifier] of this pack.
     * @param title The title displayed in the resource pack GUI, the description is still a part of the pack metadata.
     * @param activationBehaviour The [ResourcePackActivationBehaviour] for this pack.
     */
    fun registerBuiltinResourcePack(id: Identifier, title: Text, activationBehaviour: ResourcePackActivationBehaviour)

    fun canInteractToMount(player: PlayerEntity, hand: Hand, entity: PokemonEntity): Boolean {
        return !player.isSneaking && hand == Hand.MAIN_HAND
                && !player.getStackInHand(hand).isIn(CobbleRideTags.NO_MOUNT_ITEMS)
                && !(entity.isBattling && player.getStackInHand(hand).isIn(CobbleRideTags.NO_MOUNT_BATTLE_ITEMS))
    }

    fun shouldRenderStaminaBar(player: PlayerEntity): Boolean {
        return if (player.vehicle is RideablePokemonEntity) {
            val mount = (player.vehicle as RideablePokemonEntity)
            mount.canSprint && mount.canExhaust && mount.isLogicalSideForUpdatingMovement
        } else false
    }

}

enum class ResourcePackActivationBehaviour {

    /**
     * The resource pack will start disabled.
     */
    NORMAL,

    /**
     * The resource pack will start enabled.
     */
    DEFAULT_ENABLED,

    /**
     * The resource pack will always be enabled.
     * The user can reorder it but cannot remove it.
     */
    ALWAYS_ENABLED;

}

enum class ModAPI {
    FABRIC,
    FORGE
}

interface NetworkManager {

    fun registerClientBound()

    fun registerServerBound()

    fun <T: NetworkPacket<T>> createClientBound(identifier: Identifier, kClass: KClass<T>, encoder: (T, PacketByteBuf) -> Unit, decoder: (PacketByteBuf) -> T, handler: ClientNetworkPacketHandler<T>)

    fun <T: NetworkPacket<T>> createServerBound(identifier: Identifier, kClass: KClass<T>, encoder: (T, PacketByteBuf) -> Unit, decoder: (PacketByteBuf) -> T, handler: ServerNetworkPacketHandler<T>)

    fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket<*>)

    fun sendPacketToServer(packet: NetworkPacket<*>)

    fun <T : NetworkPacket<*>> asVanillaClientBound(packet: T): Packet<ClientPlayPacketListener>

}

enum class Environment {

    CLIENT,
    SERVER

}
