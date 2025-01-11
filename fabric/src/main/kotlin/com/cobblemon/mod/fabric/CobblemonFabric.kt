/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import com.cobblemon.mod.common.loot.LootInjector
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.platform.events.*
import com.cobblemon.mod.common.sherds.CobblemonSherds
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.didSleep
import com.cobblemon.mod.common.util.endsWith
import com.cobblemon.mod.common.world.CobblemonStructures
import com.cobblemon.mod.common.world.feature.CobblemonFeatures
import com.cobblemon.mod.common.world.placementmodifier.CobblemonPlacementModifierTypes
import com.cobblemon.mod.common.world.predicate.CobblemonBlockPredicates
import com.cobblemon.mod.common.world.structureprocessors.CobblemonProcessorTypes
import com.cobblemon.mod.common.world.structureprocessors.CobblemonStructureProcessorListOverrides
import com.cobblemon.mod.fabric.net.CobblemonFabricNetworkManager
import com.cobblemon.mod.fabric.permission.FabricPermissionValidator
import com.mojang.brigadier.arguments.ArgumentType
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.trade.TradeOfferHelper
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.item.ItemConvertible
import net.minecraft.particle.DefaultParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import net.minecraft.world.GameRules
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.PlacedFeature
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import kotlin.reflect.KClass


object CobblemonFabric : CobblemonImplementation {

    override val modAPI = ModAPI.FABRIC
    private var server: MinecraftServer? = null

    override val networkManager: NetworkManager = CobblemonFabricNetworkManager
    fun initialize() {
        Cobblemon.preInitialize(this)
        this.networkManager.registerServerBound()
        Cobblemon.initialize()
        //This has to be registered elsewhere on forge so we cant do it in common
        CobblemonSherds.registerSherds()
        CobblemonBlockPredicates.touch()
        CobblemonPlacementModifierTypes.touch()
        CobblemonProcessorTypes.touch()
        CobblemonActivities.activities.forEach { Registry.register(Registries.ACTIVITY, cobblemonResource(it.id), it) }
        EntitySleepEvents.STOP_SLEEPING.register { playerEntity, _ ->
            if (playerEntity !is ServerPlayerEntity) {
                return@register
            }

            playerEntity.didSleep()
        }

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, isLogin ->
            if (isLogin) {
                Cobblemon.dataProvider.sync(player)
            }
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            PlatformEvents.SERVER_STARTING.post(ServerEvent.Starting(server))
            CobblemonStructures.registerJigsaws(server)
            CobblemonStructureProcessorListOverrides.register(server)
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server -> PlatformEvents.SERVER_STARTED.post(ServerEvent.Started(server)) }
        ServerLifecycleEvents.SERVER_STOPPING.register { server -> PlatformEvents.SERVER_STOPPING.post(ServerEvent.Stopping(server)) }
        ServerLifecycleEvents.SERVER_STOPPED.register { server -> PlatformEvents.SERVER_STOPPED.post(ServerEvent.Stopped(server)) }
        ServerTickEvents.START_SERVER_TICK.register { server -> PlatformEvents.SERVER_TICK_PRE.post(ServerTickEvent.Pre(server)) }
        ServerTickEvents.END_SERVER_TICK.register { server -> PlatformEvents.SERVER_TICK_POST.post(ServerTickEvent.Post(server)) }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> PlatformEvents.SERVER_PLAYER_LOGIN.post(ServerPlayerEvent.Login(handler.player)) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> PlatformEvents.SERVER_PLAYER_LOGOUT.post(ServerPlayerEvent.Logout(handler.player)) }
        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, _, _ ->
            if (entity is ServerPlayerEntity) {
                PlatformEvents.PLAYER_DEATH.postThen(
                    event = ServerPlayerEvent.Death(entity),
                    ifSucceeded = {},
                    ifCanceled = { return@register false }
                )
            }
            return@register true
        }

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ ->
            PlatformEvents.CHANGE_DIMENSION.post(ChangeDimensionEvent(player))
        }


        UseBlockCallback.EVENT.register { player, _, hand, hitResult ->
            val serverPlayer = player as? ServerPlayerEntity ?: return@register ActionResult.PASS
            PlatformEvents.RIGHT_CLICK_BLOCK.postThen(
                event = ServerPlayerEvent.RightClickBlock(serverPlayer, hitResult.blockPos, hand, hitResult.side),
                ifSucceeded = {},
                ifCanceled = { return@register ActionResult.FAIL }
            )
            return@register ActionResult.PASS
        }

        UseEntityCallback.EVENT.register { player, _, hand, entity, _ ->
            val item = player.getStackInHand(hand)
            val serverPlayer = player as? ServerPlayerEntity ?: return@register ActionResult.PASS

            PlatformEvents.RIGHT_CLICK_ENTITY.postThen(
                event = ServerPlayerEvent.RightClickEntity(serverPlayer, item, hand, entity),
                ifSucceeded = {},
                ifCanceled = { return@register ActionResult.FAIL }
            )

            return@register ActionResult.PASS
        }

        LootTableEvents.MODIFY.register { _, _, id, tableBuilder, _ ->
            LootInjector.attemptInjection(id, tableBuilder::pool)
        }

        CommandRegistrationCallback.EVENT.register(CobblemonCommands::register)

        this.attemptModCompat()
    }

    override fun isModInstalled(id: String) = FabricLoader.getInstance().isModLoaded(id)

    override fun environment(): Environment {
        return when(FabricLoader.getInstance().environmentType) {
            EnvType.CLIENT -> Environment.CLIENT
            EnvType.SERVER -> Environment.SERVER
            else -> throw IllegalStateException("Fabric implementation cannot resolve environment yet")
        }
    }

    override fun registerPermissionValidator() {
        if (this.isModInstalled("fabric-permissions-api-v0")) {
            Cobblemon.permissionValidator = FabricPermissionValidator()
        }
    }

    override fun registerSoundEvents() {
        CobblemonSounds.register { identifier, sound -> Registry.register(CobblemonSounds.registry, identifier, sound) }
    }

    override fun registerItems() {
        CobblemonItems.register { identifier, item -> Registry.register(CobblemonItems.registry, identifier, item) }
        CobblemonItemGroups.register { provider ->
            Registry.register(Registries.ITEM_GROUP, provider.key, FabricItemGroup.builder()
                .displayName(provider.displayName)
                .icon(provider.displayIconProvider)
                .entries(provider.entryCollector)
                .build())
        }

        CobblemonItemGroups.injectorKeys().forEach { key ->
            ItemGroupEvents.modifyEntriesEvent(key).register { content ->
                val fabricInjector = FabricItemGroupInjector(content)
                CobblemonItemGroups.inject(key, fabricInjector)
            }
        }
        CobblemonTradeOffers.tradeOffersForAll().forEach { tradeOffer -> TradeOfferHelper.registerVillagerOffers(tradeOffer.profession, tradeOffer.requiredLevel) { factories -> factories.addAll(tradeOffer.tradeOffers) } }
        // 1 = common trades, 2 = rare, it has no concept of levels
        CobblemonTradeOffers.resolveWanderingTradeOffers().forEach { tradeOffer -> TradeOfferHelper.registerWanderingTraderOffers(if (tradeOffer.isRareTrade) 2 else 1) { factories -> factories.addAll(tradeOffer.tradeOffers) } }
    }

    override fun registerBlocks() {
        CobblemonBlocks.register { identifier, item -> Registry.register(CobblemonBlocks.registry, identifier, item) }
        CobblemonBlocks.strippedBlocks().forEach(StrippableBlockRegistry::register)
    }

    override fun registerEntityTypes() {
        CobblemonEntities.register { identifier, type -> Registry.register(CobblemonEntities.registry, identifier, type) }
    }

    override fun registerEntityAttributes() {
        CobblemonEntities.registerAttributes { entityType, builder -> FabricDefaultAttributeRegistry.register(entityType, builder) }
    }

    override fun registerBlockEntityTypes() {
        CobblemonBlockEntities.register { identifier, type -> Registry.register(CobblemonBlockEntities.registry, identifier, type) }
    }

    override fun registerWorldGenFeatures() {
        CobblemonFeatures.register { identifier, feature -> Registry.register(CobblemonFeatures.registry, identifier, feature) }
    }

    override fun registerParticles() {
        CobblemonParticles.register { identifier, particleType -> Registry.register(CobblemonParticles.registry, identifier, particleType) }
    }

    override fun addFeatureToWorldGen(feature: RegistryKey<PlacedFeature>, step: GenerationStep.Feature, validTag: TagKey<Biome>?) {
        val predicate: (BiomeSelectionContext) -> Boolean = { context -> validTag == null || context.hasTag(validTag) }
        BiomeModifications.addFeature(predicate, step, feature)
    }

    override fun <A : ArgumentType<*>, T : ArgumentSerializer.ArgumentTypeProperties<A>> registerCommandArgument(identifier: Identifier, argumentClass: KClass<A>, serializer: ArgumentSerializer<A, T>) {
        ArgumentTypeRegistry.registerArgumentType(identifier, argumentClass.java, serializer)
    }

    override fun <T : GameRules.Rule<T>> registerGameRule(name: String, category: GameRules.Category, type: GameRules.Type<T>): GameRules.Key<T> = GameRuleRegistry.register(name, category, type)

    override fun <T : Criterion<*>> registerCriteria(criteria: T): T = Criteria.register(criteria)

    override fun registerResourceReloader(identifier: Identifier, reloader: ResourceReloader, type: ResourceType, dependencies: Collection<Identifier>) {
        ResourceManagerHelper.get(type).registerReloadListener(CobblemonReloadListener(identifier, reloader, dependencies))
    }

    override fun server(): MinecraftServer? = if (this.environment() == Environment.CLIENT) MinecraftClient.getInstance().server else this.server

    override fun <T> reloadJsonRegistry(registry: JsonDataRegistry<T>, manager: ResourceManager): HashMap<Identifier, T> {
        val data = hashMapOf<Identifier, T>()

        if (!Cobblemon.isDedicatedServer) {
            manager.findResources(registry.resourcePath) { path -> path.endsWith(JsonDataRegistry.JSON_EXTENSION) }.forEach { (identifier, resource) ->
                if (identifier.namespace == "pixelmon") {
                    return@forEach
                }

                resource.inputStream.use { stream ->
                    stream.bufferedReader().use { reader ->
                        val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                        try {
                            data[resolvedIdentifier] = registry.gson.fromJson(reader, registry.typeToken.type)
                        } catch (exception: Exception) {
                            throw ExecutionException("Error loading JSON for data: $identifier", exception)
                        }
                    }
                }
            }
        } else {
            // Currently in Fabric API, the ResourceManager does not work as expected when using findResources.
            // It will treat built-in resources as priority over datapack resources.
            manager.findAllResources(registry.resourcePath) { path -> path.endsWith(JsonDataRegistry.JSON_EXTENSION) }.forEach { (identifier, resources) ->
                if (identifier.namespace == "pixelmon") {
                    return@forEach
                }

                if (resources.isEmpty()) {
                    return@forEach
                }

                val orderedResources = if (resources.size > 1) {
                    val sorted = resources.sortedBy { it.resourcePackName.replace("file/", "") }.toMutableList()
                    val fabric = sorted.find { it.resourcePackName == "fabric" }

                    if (fabric != null) {
                        sorted.remove(fabric)
                        sorted.add(fabric)
                    }
                    sorted
                } else {
                    resources
                }

                orderedResources[0].inputStream.use { stream ->
                    stream.bufferedReader().use { reader ->
                        val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                        try {
                            data[resolvedIdentifier] = registry.gson.fromJson(reader, registry.typeToken.type)
                        } catch (exception: Exception) {
                            throw ExecutionException("Error loading JSON for data: $identifier", exception)
                        }
                    }
                }
            }
        }
        return data
    }

    override fun registerCompostable(item: ItemConvertible, chance: Float) {
        CompostingChanceRegistry.INSTANCE.add(item, chance)
    }

    override fun registerBuiltinResourcePack(id: Identifier, title: Text, activationBehaviour: ResourcePackActivationBehaviour) {
        val mod = FabricLoader.getInstance().getModContainer(Cobblemon.MODID).get()
        val resourcePackActivationType = when (activationBehaviour) {
            ResourcePackActivationBehaviour.NORMAL -> ResourcePackActivationType.NORMAL
            ResourcePackActivationBehaviour.DEFAULT_ENABLED -> ResourcePackActivationType.DEFAULT_ENABLED
            ResourcePackActivationBehaviour.ALWAYS_ENABLED -> ResourcePackActivationType.ALWAYS_ENABLED
        }
        ResourceManagerHelper.registerBuiltinResourcePack(id, mod, title, resourcePackActivationType)
    }

    private fun attemptModCompat() {
        if (this.isModInstalled("adorn")) {
            registerBuiltinResourcePack(cobblemonResource("adorncompatibility"), Text.literal("Adorn Compatibility"), ResourcePackActivationBehaviour.ALWAYS_ENABLED)
        }
    }

    private class CobblemonReloadListener(private val identifier: Identifier, private val reloader: ResourceReloader, private val dependencies: Collection<Identifier>) : IdentifiableResourceReloadListener {

        override fun reload(synchronizer: ResourceReloader.Synchronizer, manager: ResourceManager, prepareProfiler: Profiler, applyProfiler: Profiler, prepareExecutor: Executor, applyExecutor: Executor): CompletableFuture<Void> = this.reloader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)

        override fun getFabricId(): Identifier = this.identifier

        override fun getName(): String = this.reloader.name

        override fun getFabricDependencies(): MutableCollection<Identifier> = this.dependencies.toMutableList()
    }

    @Suppress("UnstableApiUsage")
    private class FabricItemGroupInjector(private val fabricItemGroupEntries: FabricItemGroupEntries) : CobblemonItemGroups.Injector {
        override fun putFirst(item: ItemConvertible) {
            this.fabricItemGroupEntries.prepend(item)
        }

        override fun putBefore(item: ItemConvertible, target: ItemConvertible) {
            this.fabricItemGroupEntries.addBefore(target, item)
        }

        override fun putAfter(item: ItemConvertible, target: ItemConvertible) {
            this.fabricItemGroupEntries.addAfter(target, item)
        }

        override fun putLast(item: ItemConvertible) {
            this.fabricItemGroupEntries.add(item)
        }

    }

}
