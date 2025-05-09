/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.fishing

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BobberBucketChosenEvent
import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.duck.SoundManagerDuck
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.loot.CobblemonLootTables
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.toBlockPos
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.EntityTrackingSoundInstance
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.particle.DefaultParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.FluidTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import kotlin.math.sqrt


class PokeRodFishingBobberEntity(type: EntityType<out PokeRodFishingBobberEntity>, world: World) : FishingBobberEntity(type, world) {

    private val velocityRandom = Random.create()
    private var caughtFish = false
    private var outOfOpenWaterTicks = 0

    // todo is this needed?
    //val HOOK_ENTITY_ID = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    //private val CAUGHT_FISH = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

    // todo if so replace these later
    //var HOOK_ENTITY_ID = 0
    //var CAUGHT_FISH = false

    private var removalTimer = 0
    private var hookCountdown = 0
    private var waitCountdown = 0
    private var fishTravelCountdown = 0
    private var fishAngle = 0f
    var inOpenWater = true
    private var hookedEntity: Entity? = null
    var state = State.FLYING
    var luckOfTheSeaLevel = 0
    var lureLevel = 0
    private var typeCaught= "ITEM"
    private var chosenBucket = Cobblemon.bestSpawner.config.buckets[0] // default to first rarity bucket
    private val pokemonSpawnChance = 85 // chance a Pokemon will be fished up % out of 100
    var pokeRodId: Identifier? = null
    var lineColor: String = "000000" // default line color is black
    var usedRod: Identifier? = null
    var bobberBait: ItemStack = ItemStack.EMPTY
    var isCast = false
    var lastSpinAngle: Float = 0f
    var randomPitch: Float = 0f
    var randomYaw: Float = 0f
    var lastBobberPos: Vec3d? = null
    private val castingSound = CobblemonSounds.FISHING_ROD_CAST
    var rodItem: ItemStack? = null

    constructor(thrower: PlayerEntity, pokeRodId: Identifier, bait: ItemStack, world: World, luckOfTheSea: Int, lure: Int, rodItemStack: ItemStack) : this(CobblemonEntities.POKE_BOBBER, world) {
        owner = thrower
        rodItem = rodItemStack
        luckOfTheSeaLevel = luckOfTheSea
        lureLevel = lure
        this.pokeRodId = pokeRodId
        this.bobberBait = bait
        dataTracker.set(POKEROD_ID, pokeRodId.toString())
        dataTracker.set(POKEBOBBER_BAIT, bobberBait)
        dataTracker.set(HOOK_ENTITY_ID, 0)
        dataTracker.set(CAUGHT_FISH, false)

        this.usedRod = pokeRodId

        val throwerPitch = thrower.pitch
        val throwerYaw = thrower.yaw
        val cosYaw = MathHelper.cos(-throwerYaw * 0.017453292f - 3.1415927f)
        val sinYaw = MathHelper.sin(-throwerYaw * 0.017453292f - 3.1415927f)
        val cosPitch = -MathHelper.cos(-throwerPitch * 0.017453292f)
        val sinPitch = MathHelper.sin(-throwerPitch * 0.017453292f)
        val posX = thrower.x - sinYaw.toDouble() * 0.3
        val posY = thrower.eyeY
        val posZ = thrower.z - cosYaw.toDouble() * 0.3
        this.refreshPositionAndAngles(posX, posY, posZ, throwerYaw, throwerPitch)
        var vec3d = Vec3d((-sinYaw).toDouble(), MathHelper.clamp(-(sinPitch / cosPitch), -5.0f, 5.0f).toDouble(), (-cosYaw).toDouble())
        val m = vec3d.length()
        vec3d = vec3d.multiply(0.6 / m + random.nextTriangular(0.5, 0.0103365), 0.6 / m + random.nextTriangular(0.5, 0.0103365), 0.6 / m + random.nextTriangular(0.5, 0.0103365))
        velocity = vec3d
        yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        pitch = (MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 57.2957763671875).toFloat()
        prevYaw = yaw
        prevPitch = pitch
    }

    override fun initDataTracker() {
        this.dataTracker.startTracking(HOOK_ENTITY_ID, 0)
        this.dataTracker.startTracking(CAUGHT_FISH, false)
        this.dataTracker.startTracking(POKEROD_ID, "")
        this.dataTracker.startTracking(POKEBOBBER_BAIT, ItemStack.EMPTY)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        if (HOOK_ENTITY_ID == data) {
            val i = getDataTracker().get(HOOK_ENTITY_ID) as Int
            this.hookedEntity = if (i > 0) world.getEntityById(i - 1) else null
        }

        if (CAUGHT_FISH == data) {
            this.caughtFish = (getDataTracker().get(CAUGHT_FISH) as Boolean)
            if (this.caughtFish) {
                this.setVelocity(velocity.x, (-0.4f * MathHelper.nextFloat(this.velocityRandom, 0.3f, 0.5f)).toDouble(), velocity.z)
            }
        }

        super.onTrackedDataSet(data)
    }

    fun calculateMinMaxCountdown(weight: Float): Pair<Int, Int> {
        // Constants for the target min and max values at weight extremes
        val minAtMaxWeight = 20
        val maxAtMaxWeight = 40
        val minAtMinWeight = 15
        val maxAtMinWeight = 20

        // Calculate factors for min and max based on the weight
        val minFactor = ((minAtMaxWeight - minAtMinWeight) / 100f) * weight + minAtMinWeight
        val maxFactor = ((maxAtMaxWeight - maxAtMinWeight) / 100f) * weight + maxAtMinWeight

        // Ensure min and max are within the needed bounds
        val min = minFactor.toInt().coerceIn(minAtMinWeight, minAtMaxWeight)
        val max = maxFactor.toInt().coerceIn(maxAtMinWeight, maxAtMaxWeight)

        return Pair(min, max)
    }

    fun chooseAdjustedSpawnBucket(buckets: List<SpawnBucket>, luckOfTheSeaLevel: Int): SpawnBucket {
        val baseIncreases = listOf(5.0F, 1.0F, 0.2F)  // Base increases for the first three buckets beyond the first
        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index == 0) {
                // Placeholder, will be recalculated
                0.0F
            } else {
                val increase = if (index < baseIncreases.size) baseIncreases[index] else baseIncreases.last() + (index - baseIncreases.size + 1) * 0.15F
                bucket.weight + increase * luckOfTheSeaLevel
            }
        }.toMutableList()

        // Recalculate the first bucket's weight to ensure the total is 100%
        val totalAdjustedWeight = adjustedWeights.sum() - adjustedWeights[0]  // Corrected to ensure the list contains Floats
        adjustedWeights[0] = 100.0F - totalAdjustedWeight + buckets[0].weight

        // Random selection based on adjusted weights
        val weightSum = adjustedWeights.sum()
        val chosenSum = kotlin.random.Random.nextDouble(weightSum.toDouble()).toFloat()  // Ensure usage of Random from kotlin.random package
        var sum = 0.0F
        adjustedWeights.forEachIndexed { index, weight ->
            sum += weight
            if (sum >= chosenSum) {
                return buckets[index]
            }
        }

        return buckets.first()  // Fallback
    }

    /*// make more generic version for other enchants. Pass in id of enchant
    fun getEnchantLevel(enchantment: Enchantment): Int {
        var pokerodEnchantments: NbtList? = null
        if (owner?.isPlayer == true) {
            pokerodEnchantments = (owner as PlayerEntity).mainHandStack?.enchantments
        }

        val luckOfTheSeaEnchantment = pokerodEnchantments?.filter {
            if (it is NbtCompound)
                if (it.getString("id") == Registries.ENCHANTMENT.getEntry(enchantment).key.toString())
                    return@filter true
            return@filter false
        }?.first()

        return (luckOfTheSeaEnchantment as? NbtCompound)?.getShort("lvl")?.toInt() ?: 0
    }*/

    fun isOpenOrWaterAround(pos: BlockPos): Boolean {
        var positionType = PositionType.INVALID
        for (i in -1..2) {
            val positionType2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2))
            when (positionType2) {
                PositionType.INVALID -> return false
                PositionType.ABOVE_WATER -> if (positionType == PositionType.INVALID) {
                    return false
                }

                PositionType.INSIDE_WATER -> if (positionType == PositionType.ABOVE_WATER) {
                    return false
                }

                else -> return false
            }
            positionType = positionType2
        }
        return true
    }

    /*private fun getPositionType(start: BlockPos, end: BlockPos): PokeRodFishingBobberEntity.PositionType? {
        return BlockPos.stream(start, end).map { pos: BlockPos? -> this.getPositionType(pos!!) }.reduce { positionType: PokeRodFishingBobberEntity.PositionType, positionType2: PokeRodFishingBobberEntity.PositionType -> if (positionType == positionType2) positionType else PokeRodFishingBobberEntity.PositionType.INVALID } as ((PositionType?, PositionType?) -> PositionType?)?.orElse(PokeRodFishingBobberEntity.PositionType.INVALID) as PokeRodFishingBobberEntity.PositionType
    }*/

    private fun getPositionType(start: BlockPos, end: BlockPos): PositionType? {
        return BlockPos.stream(start, end)
            .map { pos -> this.getPositionType(pos) }
            .reduce { positionType, positionType2 ->
                if (positionType == positionType2) positionType else PositionType.INVALID
            }.orElse(PositionType.INVALID)
    }

    private fun getPositionType(pos: BlockPos): PositionType {
        val blockState = world.getBlockState(pos)
        return if (!blockState.isAir && !blockState.isOf(Blocks.LILY_PAD)) {
            val fluidState = blockState.fluidState
            if (fluidState.isIn(FluidTags.WATER) && fluidState.isStill && blockState.getCollisionShape(world, pos).isEmpty) PositionType.INSIDE_WATER else PositionType.INVALID
        } else {
            PositionType.ABOVE_WATER
        }
    }

    enum class PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    private fun setPlayerFishHook(fishingBobber: FishingBobberEntity?) {
        val playerEntity = this.playerOwner
        if (playerEntity != null) {
            playerEntity.fishHook = fishingBobber
        }
    }

    enum class State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }

    // todo maybe custom behavior for fishing logic
    private fun tickFishingLogic(pos: BlockPos) {
        val serverWorld = world as ServerWorld
        var i = 1
        val blockPos = pos.up()

        if (random.nextFloat() < 0.25f && world.hasRain(blockPos)) {
            ++i
        }
        if (random.nextFloat() < 0.5f && !world.isSkyVisible(blockPos)) {
            --i
        }
        if (this.hookCountdown > 0) {
            --this.hookCountdown
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0
                this.fishTravelCountdown = 0
                //this.CAUGHT_FISH = false
                getDataTracker().set(CAUGHT_FISH, false)
            }
        } else if (this.fishTravelCountdown > 0) { // create a fish trail that leads to the bobber visually
            this.fishTravelCountdown -= i
            if (this.fishTravelCountdown > 0) {
                this.fishAngle += random.nextTriangular(0.0, 9.188).toFloat()
                val f = this.fishAngle * (Math.PI.toFloat() / 180)
                val g = MathHelper.sin(f)
                val h = MathHelper.cos(f)
                val offsetX = this.x + (g * this.fishTravelCountdown.toFloat() * 0.1f).toDouble()
                val offsetY = (MathHelper.floor(this.y).toFloat() + 1.0f).toDouble()
                val j = this.z + (h * this.fishTravelCountdown.toFloat() * 0.1f).toDouble()
                val blockState = serverWorld.getBlockState(BlockPos.ofFloored(offsetX, offsetY - 1.0, j))
                //val blockState = serverWorld.getBlockState(BlockPos.ofFloored(offsetX, (MathHelper.floor(this.y).toFloat() + 1.0f).toDouble().also { offsetY = it } - 1.0, this.z + (h * this.fishTravelCountdown.toFloat() * 0.1f).toDouble().also { j = it }))
                if (blockState.isOf(Blocks.WATER)) {
                    if (random.nextFloat() < 0.15f) {
                        // random bubble particles that spawn around
                        //serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.x, this.y, this.z, 3, g.toDouble(), 0.1, h.toDouble(), 0.0)
                        serverWorld.spawnParticles(ParticleTypes.BUBBLE, offsetX, offsetY - 0.1, j, 1, g.toDouble(), 0.1, h.toDouble(), 0.0)
                    }
                    val k = g * 0.04f
                    val l = h * 0.04f

                    // todo the fish trail that leads to the bobber
                    serverWorld.spawnParticles(ParticleTypes.FISHING, offsetX, offsetY, j, 0, l.toDouble(), 0.01, -k.toDouble(), 1.0)
                    serverWorld.spawnParticles(ParticleTypes.FISHING, offsetX, offsetY, j, 0, -l.toDouble(), 0.01, k.toDouble(), 1.0)
                    // create tiny splash particles for fishing trail
                    //particleEntityHandler(this, Identifier("cobblemon","bob_splash"))
                }
            } else {
                //playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f)
                // play bobber hook notification sound
                world.playSound(null, this.blockPos, CobblemonSounds.FISHING_NOTIFICATION, SoundCategory.BLOCKS, 1.0F, 1.0F)

                // create tiny splash particle when there is a bite
                particleEntityHandler(this, Identifier("cobblemon","bob_splash"))

                val m = this.y + 0.5
                serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.x, m, this.z, (1.0f + this.width * 20.0f).toInt(), this.width.toDouble(), 0.0, this.width.toDouble(), 0.2)
                serverWorld.spawnParticles(ParticleTypes.FISHING, this.x, m, this.z, (1.0f + this.width * 20.0f).toInt(), this.width.toDouble(), 0.0, this.width.toDouble(), 0.2)

                // check for chance to catch pokemon based on the bait
                if (MathHelper.nextInt(random, 0, 100) < getPokemonSpawnChance(bobberBait)) {
                    this.typeCaught = "POKEMON"

                    val buckets = Cobblemon.bestSpawner.config.buckets

                    // choose a spawn bucket according to weights no matter how many there are
                    chosenBucket = chooseAdjustedSpawnBucket(buckets, luckOfTheSeaLevel)
                    CobblemonEvents.BOBBER_BUCKET_CHOSEN.post(BobberBucketChosenEvent(chosenBucket, buckets, luckOfTheSeaLevel)) { event ->
                        chosenBucket = event.chosenBucket
                    }
                    val reactionMinMax = calculateMinMaxCountdown(chosenBucket.weight)

                    // set the hook reaction time to be based off the rarity of the bucket chosen
                    this.hookCountdown = MathHelper.nextInt(random, reactionMinMax.first, reactionMinMax.second)

                    //println("Player hooked a Pokemon from the bucket: " + chosenBucket.name)

                }
                else {
                    // todo caught item
                    this.typeCaught = "ITEM"
                    this.hookCountdown = MathHelper.nextInt(random, 20, 40)

                }

                //this.hookCountdown = MathHelper.nextInt(random, 20, 40)
                getDataTracker().set(CAUGHT_FISH, true)
                //this.CAUGHT_FISH = true
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i
            var f = 0.15f
            if (this.waitCountdown < 20) {
                f += (20 - this.waitCountdown).toFloat() * 0.05f
            } else if (this.waitCountdown < 40) {
                f += (40 - this.waitCountdown).toFloat() * 0.02f
            } else if (this.waitCountdown < 60) {
                f += (60 - this.waitCountdown).toFloat() * 0.01f
            }
            if (random.nextFloat() < f) {
                val g = MathHelper.nextFloat(this.random, 0.0f, 360.0f) * 0.017453292f
                val h = MathHelper.nextFloat(this.random, 25.0f, 60.0f)
                val d = this.x + (MathHelper.sin(g) * h).toDouble() * 0.1 // X
                val e = (MathHelper.floor(this.y).toFloat() + 1.0f).toDouble() // Y
                val j = this.z + (MathHelper.cos(g) * h).toDouble() * 0.1 // randomized Z value
                val blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j))
                if (blockState.isOf(Blocks.WATER)) {
                    serverWorld.spawnParticles<DefaultParticleType>(ParticleTypes.SPLASH, d, e, j, 2 + random.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0)
                }
            }
            if (this.waitCountdown <= 0) {
                this.fishAngle = MathHelper.nextFloat(random, 0.0f, 360.0f)
                this.fishTravelCountdown = MathHelper.nextInt(random, 20, 80)
            }
        } else {
            if (isCast != true) {
                // When bobber lands on the water for the first time
                world.playSound(null, this.blockPos, CobblemonSounds.FISHING_BOBBER_LAND, SoundCategory.NEUTRAL, 1.0F, 1.0F)

                // create tiny splash particle
                particleEntityHandler(this, Identifier("cobblemon","bob_splash"))

                isCast = true
            }

            // set the time it takes to wait for a hooked item or pokemon
            this.waitCountdown = MathHelper.nextInt(random, 100, 600)
            this.waitCountdown -= this.lureLevel * 20 * 5

            // check for the bait on the hook and see if the waitCountdown is reduced
            if (checkReduceBiteTime(bobberBait))
                this.waitCountdown = alterBiteTimeAttempt(this.waitCountdown, this.bobberBait)

        }
    }

    fun stopCastingAudio () {
        if(!this.world.isClient()) return
        owner?.let { MinecraftClient.getInstance().soundManager.stopSounds(CobblemonSounds.FISHING_ROD_CAST.id, SoundCategory.PLAYERS)}
    }

    private fun removeIfInvalid(player: PlayerEntity): Boolean {
        val itemStack = player.mainHandStack
        val itemStack2 = player.offHandStack
        val bl = Registries.ITEM[this.usedRod] == itemStack.item //(itemStack.item is PokerodItem) // todo make this work again so the line breaks when you swap items
        val bl2 = Registries.ITEM[this.usedRod] == itemStack2.item //(itemStack2.item is PokerodItem) // todo make this work again so the line breaks when you swap items
        if (player.isRemoved || !player.isAlive || !bl && !bl2 || this.squaredDistanceTo(player) > 1024.0) {
            if (world.isClient)
                stopCastingAudio() // remove casting audio when user switches to different item in hotbar
            discard()
            isCast = false
            return true
        }
        return false
    }

    private fun checkForCollision() {
        val hitResult = ProjectileUtil.getCollision(this) { entity: Entity -> this.canHit(entity) }
        onCollision(hitResult)
    }

    override fun canHit(entity: Entity): Boolean {
        return super.canHit(entity) || entity.isAlive && entity is ItemEntity
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        //super.onEntityHit(entityHitResult) // this calls the function in FishingBobberEntity which has null values which causes crash I think

        //        ProjectileEntity.onEntityHit(entityHitResult) // this is protected and cannot be reached
        if (!world.isClient) {
            this.updateHookedEntityId(entityHitResult.entity)
        }
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        this.velocity = velocity.normalize().multiply(blockHitResult.squaredDistanceTo(this))
    }

    private fun updateHookedEntityId(entity: Entity?) {
        this.hookedEntity = entity
        getDataTracker().set(HOOK_ENTITY_ID, if (entity == null) 0 else entity.id + 1)
        //this.HOOK_ENTITY_ID = if (entity == null) 0 else entity.id + 1
    }

    override fun tick() {
        velocityRandom.setSeed(getUuid().leastSignificantBits xor world.time)
        //super.tick()
        val playerEntity = this.playerOwner
        if (playerEntity == null) {
            discard()
        } else if (world.isClient || !removeIfInvalid(playerEntity)) {
            if (this.isOnGround) {
                ++removalTimer
                if (removalTimer >= 1200) {
                    discard()
                    return
                }
            } else {
                removalTimer = 0
            }
            var f = 0.0f
            val blockPos = blockPos
            val fluidState = world.getFluidState(blockPos)
            if (fluidState.isIn(FluidTags.WATER)) {
                f = fluidState.getHeight(world, blockPos)
            }
            val bl = f > 0.0f

            // pause audio if the bobber is not moving anymore
            if (lastBobberPos == pos) {
                // stop audio for the rod casting if the position has not changed
                if (world.isClient)
                    stopCastingAudio()
            }
            lastBobberPos = pos

            if (state == State.FLYING) {
                if (hookedEntity != null) {
                    velocity = Vec3d.ZERO
                    state = State.HOOKED_IN_ENTITY
                    return
                }
                if (bl) {
                    velocity = velocity.multiply(0.3, 0.2, 0.3)
                    state = State.BOBBING
                    return
                }
                checkForCollision()
            } else {
                if (state == State.HOOKED_IN_ENTITY) {
                    if (hookedEntity != null) {
                        if (!hookedEntity!!.isRemoved && hookedEntity!!.world.registryKey === world.registryKey) {
                            this.setPosition(hookedEntity!!.x, hookedEntity!!.getBodyY(0.8), hookedEntity!!.z)
                        } else {
                            updateHookedEntityId(null as Entity?)
                            state = State.FLYING
                        }
                    }
                    return
                }
                if (state == State.BOBBING) {
                    // stop casting audio once it lands in water
                    if (castingSound != null && world.isClient) {
                        stopCastingAudio()
                    }

                    val vec3d = velocity
                    var d = this.y + vec3d.y - blockPos.y.toDouble() - f.toDouble()
                    if (Math.abs(d) < 0.01) {
                        d += Math.signum(d) * 0.1
                    }
                    this.setVelocity(vec3d.x * 0.9, vec3d.y - d * random.nextFloat().toDouble() * 0.2, vec3d.z * 0.9)
                    inOpenWater = if (hookCountdown <= 0 && fishTravelCountdown <= 0) {
                        true
                    } else {
                        inOpenWater && outOfOpenWaterTicks < 10 && isOpenOrWaterAround(blockPos)
                    }
                    if (bl) {
                        outOfOpenWaterTicks = Math.max(0, outOfOpenWaterTicks - 1)
                        if (caughtFish) {
                            velocity = velocity.add(0.0, -0.1 * velocityRandom.nextFloat().toDouble() * velocityRandom.nextFloat().toDouble(), 0.0)
                        }
                        if (!world.isClient) {
                            tickFishingLogic(blockPos)
                        }
                    } else {
                        outOfOpenWaterTicks = Math.min(10, outOfOpenWaterTicks + 1)
                    }
                }
            }
            if (!fluidState.isIn(FluidTags.WATER)) {
                velocity = velocity.add(0.0, -0.03, 0.0)
            }
            move(MovementType.SELF, velocity)
            this.updateRotation()
            if (state == State.FLYING && (this.isOnGround || horizontalCollision)) {
                velocity = Vec3d.ZERO
            }
            velocity = velocity.multiply(0.92)
            refreshPosition()
        }
    }

    override fun use(usedItem: ItemStack): Int {
        // when reelimg in prematurely stop casting audio if it is playing
        if (world.isClient)
            stopCastingAudio()

        val playerEntity = this.playerOwner
        isCast = false

        return if (!world.isClient && playerEntity != null && !removeIfInvalid(playerEntity)) {
            isCast = false
            var i = 0
            if (this.hookedEntity != null) {
                pullHookedEntity(this.hookedEntity)
                Criteria.FISHING_ROD_HOOKED.trigger(playerEntity as ServerPlayerEntity?, usedItem, this, emptyList())
                world.sendEntityStatus(this, 31.toByte())
                i = if (this.hookedEntity is ItemEntity) 3 else 5
            } else if (this.hookCountdown > 0) {
                // check if thing caught was an item
                if (this.typeCaught == "ITEM") {
                    val lootContextParameterSet = LootContextParameterSet.Builder(world as ServerWorld).add(LootContextParameters.ORIGIN, pos).add(LootContextParameters.TOOL, usedItem).add(LootContextParameters.THIS_ENTITY, this).luck(this.luckOfTheSeaLevel.toFloat() + playerEntity.luck).build(LootContextTypes.FISHING)
                    val lootTable = world.server!!.lootManager.getLootTable(CobblemonLootTables.FISHING_GAMEPLAY)
                    val list: List<ItemStack> = lootTable.generateLoot(lootContextParameterSet)
                    Criteria.FISHING_ROD_HOOKED.trigger(playerEntity as ServerPlayerEntity?, usedItem, this, list)
                    val var7: Iterator<*> = list.iterator()
                    while (var7.hasNext()) {
                        val itemStack = var7.next() as ItemStack
                        val itemEntity = ItemEntity(world, this.x, this.y, this.z, itemStack)
                        val d = playerEntity.getX() - this.x
                        val e = playerEntity.getY() - this.y
                        val f = playerEntity.getZ() - this.z
                        itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1)
                        world.spawnEntity(itemEntity)
                        playerEntity.getWorld().spawnEntity(ExperienceOrbEntity(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, random.nextInt(6) + 1))
                        if (itemStack.isIn(ItemTags.FISHES)) {
                            playerEntity.increaseStat(Stats.FISH_CAUGHT, 1)
                        }
                    }
                    i = 1
                }
                else { // logic for spawning Pokemon using rarity

                    //System.out.println("Player reeled in a Pokemon from the bucket: " + chosenBucket.name)

                    val bobberOwner = playerOwner as ServerPlayerEntity

                    // decrememnt the bait count on the rod itself when reeling in a pokemon
                    CobblemonEvents.BOBBER_SPAWN_POKEMON_PRE.postThen(
                        BobberSpawnPokemonEvent.Pre(this, chosenBucket, rodItem!!),
                        { event ->
                            return 0
                        },
                        { event ->

                            PokerodItem.consumeBait(rodItem!!)

                            // spawn the pokemon from the chosen bucket at the bobber's location
                            spawnPokemonFromFishing(bobberOwner, chosenBucket, bobberBait)

                            val serverWorld = world as ServerWorld

                            val g = MathHelper.nextFloat(random, 0.0f, 360.0f) * (Math.PI.toFloat() / 180)
                            val h = MathHelper.nextFloat(random, 25.0f, 60.0f)
                            val partX = this.x + (MathHelper.sin(g) * h).toDouble() * 0.1
                            serverWorld.spawnParticles(
                                ParticleTypes.SPLASH,
                                partX,
                                this.y,
                                this.z,
                                6 + random.nextInt(4),
                                0.0,
                                0.2,
                                0.0,
                                0.0
                            )

                            playerEntity.getWorld().spawnEntity(
                                ExperienceOrbEntity(
                                    playerEntity.getWorld(),
                                    playerEntity.getX(),
                                    playerEntity.getY() + 0.5,
                                    playerEntity.getZ() + 0.5,
                                    random.nextInt(6) + 1
                                )
                            )
                        })
                }
            }
            if (this.isOnGround) {
                i = 2
            }
            discard()
            i
        } else {
            isCast = false
            0
        }
    }

    // calculate the trajectory for the reeled in pokemon
    fun lobPokemonTowardsTarget(player: PlayerEntity, entity: Entity) {
        val rad = Math.toRadians(player.yaw.toDouble())
        val targetDirection = Vec3d(-Math.sin(rad), 0.0, Math.cos(rad))
        val targetPos = player.pos.add(targetDirection.multiply(5.0))

        val delta = targetPos.subtract(entity.pos)
        val horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z)

        // Introduce a damping factor that reduces the velocity as the distance increases
        val dampingFactor = 1 - (horizontalDistance / 80).coerceIn(0.0, 0.8) // increase end of coerceIn to dampen more

        val verticalVelocity = 0.30 // Base vertical velocity for a gentle arc
        val horizontalVelocityFactor = 0.13 // Base horizontal velocity factor

        // Apply the damping factor to both horizontal and vertical velocities
        val adjustedHorizontalVelocity = horizontalDistance * horizontalVelocityFactor * dampingFactor
        val adjustedVerticalVelocity = (verticalVelocity + (horizontalDistance * 0.05)) * dampingFactor

        // Calculate the final velocities
        val velocityX = delta.x / horizontalDistance * adjustedHorizontalVelocity
        val velocityZ = delta.z / horizontalDistance * adjustedHorizontalVelocity
        val velocityY = adjustedVerticalVelocity

        val tossVelocity = Vec3d(velocityX, velocityY, velocityZ)
        entity.setVelocity(tossVelocity)
    }

    fun spawnPokemonFromFishing(player: PlayerEntity, chosenBucket: SpawnBucket, bobberBait: ItemStack) {
        var hookedEntityID: Int? = null

        val spawner = BestSpawner.fishingSpawner

        //val spawnCause = SpawnCause(spawner = spawner, bucket = spawner.chooseBucket(), entity = spawner.getCauseEntity())
        val spawnCause = FishingSpawnCause(
            spawner = spawner,
            bucket = chosenBucket,
            entity = player,
            rodStack = player.mainHandStack // Crab, you should probably parse in the rod item connected to the bobber so we can check enchants in spawn conditions
        )

        val result = spawner.run(spawnCause, world as ServerWorld, pos.toBlockPos())

        if (result == null) {
            // This has a chance to fail, if the position has no suitability for a fishing context
            //  it could also just be a miss which
            //   means two attempts to spawn in the same location can have differing results (which is expected for
            //   randomness).
            player.sendMessage("Not even a nibble".red())
        }

        var spawnedPokemon: PokemonEntity? = null
        val resultingSpawn = result?.get()

        if (resultingSpawn is EntitySpawnResult) {
            for (entity in resultingSpawn.entities) {
                //pokemon.uuid = it.uuid
                hookedEntityID = entity.id
                spawnedPokemon = (entity as PokemonEntity)

                // todo Check if there was a berry used on the bobber and if so then see how it may affect the pokemon
                if (bobberBait != ItemStack.EMPTY) {
                    // look up how mints affect the pokemon

                    modifyPokemonWithBait(spawnedPokemon, bobberBait) // check to see if the spawned pokemon gets modified due to the bait used

                    // remove the bait from the bobber
                    val playerPokerodItemStack = if (this.playerOwner?.getStackInHand(Hand.MAIN_HAND)?.item is PokerodItem) this.playerOwner!!.getStackInHand(Hand.MAIN_HAND) else this.playerOwner!!.getStackInHand(Hand.OFF_HAND)
                    val playerPokerod = playerPokerodItemStack.item
//                    val baitStack = PokerodItem.getBait(playerPokerodItemStack)
//                    baitStack.decrement(1)
//                    PokerodItem.setBait(playerPokerodItemStack, baitStack)
                }

                // create accessory splash particle when you fish something up
                particleEntityHandler(this, Identifier("cobblemon","accessory_fish_splash"))

                if (spawnedPokemon.pokemon.species.weight.toDouble() < 900.0) { // if weight value of Pokemon is less than 200 lbs (in hectograms) which we store weight as) then reel it in to the player
                    // play sound for small splash when this weight class is fished up
                    world.playSound(null, this.blockPos, CobblemonSounds.FISHING_SPLASH_SMALL, SoundCategory.BLOCKS, 1.0F, 1.0F)

                    // create small splash particle for small pokemon
                    particleEntityHandler(this, Identifier("cobblemon","small_fish_splash"))

                    // direction and position
                    val rad = Math.toRadians(player.yaw.toDouble() + 180)
                    val behindDirection = Vec3d(-Math.sin(rad), 0.0, Math.cos(rad))
                    val targetPos = player.pos.add(behindDirection.multiply(2.0))
                    val diff = targetPos.subtract(entity.pos)
                    val distance = diff.horizontalLength()

                    // Example of applying the new velocity
                    lobPokemonTowardsTarget(player, entity)
                }
                else { // it is a big lad and you cannot reel it in
                    // create big splash particle for large pokemon
                    particleEntityHandler(this, Identifier("cobblemon","big_fish_splash"))

                    world.playSound(null, this.blockPos, CobblemonSounds.FISHING_SPLASH_BIG, SoundCategory.BLOCKS, 1.0F, 1.0F)
                }
                CobblemonEvents.BOBBER_SPAWN_POKEMON_POST.post(BobberSpawnPokemonEvent.Post(this, chosenBucket, rodItem!!,
                    entity
                ))
            }
        }

        // What if it isn't an entity though
        hookedEntity = world.getEntityById(hookedEntityID!!)

        //val spawnedPokemon = spawnAction.entity
        if (spawnedPokemon != null) {
            BattleBuilder.pve((player as ServerPlayerEntity), spawnedPokemon).ifErrored { it.sendTo(player) { it.red() } }
        }
    }

    fun modifyPokemonWithBait(pokemonEntity: PokemonEntity, bait: ItemStack) {
        val pokemon = pokemonEntity.pokemon

        // check if it attracts a certain nature
        if (checkNatureAttact(bait)) {
            // figure out which nature is attracted by the bait and do a calculation to see if it was successful
            alterNatureAttempt(pokemon, bait)
        }

        // check if it raises specific IV by +3
        if (checkIVRaise(bait)) {
            // try to alter the IVs of the pokemon based on the bait bonus
            alterIVAttempt(pokemon, bait)
        }

        // todo check if it attracts certain EV yields? Maybe this needs to be done before the spown conditions are used? This one might be hard

        // Reduce Bite time.... This can be done way earlier (And is currently in place)

        // check if it alters the gender of the pokemon
        if (checkBetterGenderOdds(bait) && (pokemon.species.maleRatio > 0 && pokemon.species.maleRatio < 1)) {
            // try to alter the pokemon gender based on the bait bonus
            alterGenderAttempt(pokemon, bait)
        }

        // check if it raises the average level
        if (checkLevelBoost(bait)) {
            // try to alter the pokemon level based on the bait bonus
            alterLevelAttempt(pokemon, bait)
        }


        // check for shiny odds
        if (checkShinyOdds(bait)) {
            // try to alter the pokemon level based on the bait bonus
            alterShinyAttempt(pokemon, bait)
        }

        // check for HA
        if (checkHiddenAbilityOdds(bait)) {
            // try to alter the pokemon level based on the bait bonus
            alterHAAttempt(pokemon, bait)
        }

        // check for friendship increase
        if (checkFriendshipIncrease(bait)){
            alterFriendshipAttempt(pokemon, bait)
        }
    }

    fun checkBaitSuccessRate(successChance: Double): Boolean {
        return Math.random() <= successChance
    }

    // function to return true of false if the given bait affects the attraction of certain Natures
    fun checkNatureAttact(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.NATURE }
    }

    // function to return true of false if the given bait affects the raising of IVs of a pokemon via fishing
    fun checkIVRaise(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.IV }
    }

    // function to return true of false if the given bait affects the pokemon with certain EV yields
    fun checkEVAttract(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.EV }
    }

    // function to return true of false if the given bait affects the pokemon gender
    fun checkBetterGenderOdds(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.GENDER_CHANCE }
    }

    // function to return true of false if the given bait affects time to expect a bite
    fun checkReduceBiteTime(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.BITE_TIME }
    }

    // function to return true of false if the given bait affects pokemon's level boost
    fun checkLevelBoost(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.LEVEL_RAISE }
    }

    // function to return true of false if the given bait affects pokemon's shiny chance
    fun checkShinyOdds(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.SHINY_REROLL }
    }

    // function to return true of false if the given bait affects pokemon's Hidden Ability
    fun checkHiddenAbilityOdds(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.HIDDEN_ABILITY_CHANCE }
    }

    // function to return true of false if the given bait to make it so a Pokemon is always reeled in
    fun checkPokemonFishRate(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.POKEMON_CHANCE }
    }

    // function to return true or false if the given bait will raise friendship of a caught mon
    fun checkFriendshipIncrease(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.FRIENDSHIP }
    }


    // try to alter the nature of the spawned pokemon
    fun alterNatureAttempt(pokemon: Pokemon, stack: ItemStack) {
        // natures for each stat
        val attNaturesIds = listOf(Natures.LONELY, Natures.ADAMANT, Natures.NAUGHTY, Natures.BRAVE)
        val spaNaturesIds = listOf(Natures.MODEST, Natures.MILD, Natures.RASH, Natures.QUIET)
        val defNaturesIds = listOf(Natures.BOLD, Natures.IMPISH, Natures.LAX, Natures.RELAXED)
        val spdNaturesIds = listOf(Natures.CALM, Natures.GENTLE, Natures.CAREFUL, Natures.SASSY)
        val speNaturesIds = listOf(Natures.TIMID, Natures.HASTY, Natures.JOLLY, Natures.NAIVE)
        val neutralNaturesIds = listOf(Natures.HARDY, Natures.DOCILE, Natures.BASHFUL, Natures.QUIRKY, Natures.SERIOUS)

        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val natures = bait.effects.filter { it.type == FishingBait.Effects.NATURE }
        val randomNatureEffect = natures.random()

        if (!checkBaitSuccessRate(randomNatureEffect.chance)) return

        pokemon.nature = when(randomNatureEffect.subcategory) {
            cobblemonResource("atk") -> attNaturesIds.random()
            cobblemonResource("spa") -> spaNaturesIds.random()
            cobblemonResource("def") -> defNaturesIds.random()
            cobblemonResource("spd") -> spdNaturesIds.random()
            cobblemonResource("spe") -> speNaturesIds.random()
            else -> return
        }
    }

    // alter the IVs based on the bait effect
    fun alterIVAttempt(pokemon: Pokemon, stack: ItemStack) {

        val bait = FishingBaits.getFromItemStack(stack) ?: return

        // various IV effects
        val hpIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("hp")}
        val atkIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("atk")}
        val defIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("def")}
        val spaIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("spa")}
        val spdIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("spd")}
        val speIVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.IV && it.subcategory == cobblemonResource("spe")}

        if (hpIVEffect != null && checkBaitSuccessRate(hpIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.HP] ?: 0) + hpIVEffect.value > 31) // if HP IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.HP] ?: 0) + (hpIVEffect.value).toInt())
        }
        if (atkIVEffect != null && checkBaitSuccessRate(atkIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.ATTACK] ?: 0) + atkIVEffect.value > 31) // if ATTACK IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.ATTACK, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.ATTACK, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.ATTACK] ?: 0) + (atkIVEffect.value).toInt())
        }
        if (defIVEffect != null && checkBaitSuccessRate(defIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.DEFENCE] ?: 0) + defIVEffect.value > 31) // if DEFENCE IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.DEFENCE, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.DEFENCE, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.DEFENCE] ?: 0) + (defIVEffect.value).toInt())
        }
        if (spaIVEffect != null && checkBaitSuccessRate(spaIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_ATTACK] ?: 0) + spaIVEffect.value > 31) // if SPECIAL_ATTACK IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_ATTACK, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_ATTACK, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_ATTACK] ?: 0) + (spaIVEffect.value).toInt())
        }
        if (spdIVEffect != null && checkBaitSuccessRate(spdIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_DEFENCE] ?: 0) + spdIVEffect.value > 31) // if SPECIAL_DEFENCE IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_DEFENCE, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_DEFENCE, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_DEFENCE] ?: 0) + (spdIVEffect.value).toInt())
        }
        if (speIVEffect != null && checkBaitSuccessRate(speIVEffect.chance)) {
            if ((pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPEED] ?: 0) + speIVEffect.value > 31) // if SPEED IV is already less than 3 away from 31
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPEED, 31)
            else
                pokemon.ivs.set(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPEED, (pokemon.ivs[com.cobblemon.mod.common.api.pokemon.stats.Stats.SPEED] ?: 0) + (speIVEffect.value).toInt())
        }
    }

    // try to alter the gender based on the bait effect
    fun alterGenderAttempt(pokemon: Pokemon, stack: ItemStack) {

        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val effect = bait.effects.first { it.type == FishingBait.Effects.GENDER_CHANCE }
        val gender = effect.subcategory ?: return
        if (!checkBaitSuccessRate(effect.chance))

            if (pokemon.gender != Gender.MALE && gender == cobblemonResource("male")) {
                pokemon.gender = Gender.MALE
            }
            else if (pokemon.gender != Gender.FEMALE && gender == cobblemonResource("female")) {
                pokemon.gender = Gender.FEMALE
            }
    }

    // alter the level of the pokemon
    fun alterLevelAttempt(pokemon: Pokemon, stack: ItemStack) {
        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val effect = bait.effects.filter { it.type == FishingBait.Effects.LEVEL_RAISE }.random()
        if (!checkBaitSuccessRate(effect.chance)) return

        var level = pokemon.level + effect.value.toInt()
        if (level > Cobblemon.config.maxPokemonLevel) level = Cobblemon.config.maxPokemonLevel

        pokemon.level = level
    }


    // try to reroll for a shiny based on the bait effect
    fun alterShinyAttempt(pokemon: Pokemon, stack: ItemStack) {
        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val effect = bait.effects.filter { it.type == FishingBait.Effects.SHINY_REROLL }.random()
        if (!checkBaitSuccessRate(effect.chance)) return
        if (!pokemon.shiny) {
            //reroll for a shiny using shiny odds
            val shinyOdds = config.shinyRate.toInt()
            val randomNumber = kotlin.random.Random.nextInt(0, shinyOdds + 1)

            // Check if the random number indicates a shiny Pokemon
            if (randomNumber <= (effect.value).toInt()) {
                pokemon.shiny = true
            }
        }
    }

    // check if the bite time is reduced based on the bait bonus
    fun alterBiteTimeAttempt(waitCountdown: Int, stack: ItemStack): Int {
        val bait = FishingBaits.getFromItemStack(stack) ?: return waitCountdown
        val effect = bait.effects.filter { it.type == FishingBait.Effects.BITE_TIME }.random()
        if (!checkBaitSuccessRate(effect.chance)) return waitCountdown
        return if (waitCountdown - waitCountdown * (effect.value) <= 0)
            1 // return min value
        else
            (waitCountdown - waitCountdown * (effect.value)).toInt()
    }

    // chance to alter HA based on the berry effect
    fun alterHAAttempt(pokemon: Pokemon, stack: ItemStack) {
        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val effect = bait.effects.filter { it.type == FishingBait.Effects.HIDDEN_ABILITY_CHANCE }.random()
        if (checkBaitSuccessRate(effect.chance)) {
            giveHiddenAbility(pokemon)
        }
    }

    // chance to alter Friendship upon reeling in
    fun alterFriendshipAttempt(pokemon: Pokemon, stack: ItemStack) {
        val bait = FishingBaits.getFromItemStack(stack) ?: return
        val effect = bait.effects.filter { it.type == FishingBait.Effects.FRIENDSHIP }.random()

        if (checkBaitSuccessRate(effect.chance)) {
            if (pokemon.friendship + effect.value > 255)
                pokemon.setFriendship(255)
            else
                pokemon.setFriendship(pokemon.friendship + effect.value.toInt())
        }
    }

    // give a pokemon a random hidden ability
    private fun giveHiddenAbility(pokemon: Pokemon): Boolean {
        // This will iterate from highest to lowest priority
        pokemon.form.abilities.mapping.values.forEach { abilities ->
            abilities.filterIsInstance<HiddenAbility>()
                .randomOrNull ()
                ?.let { ability ->
                    // No need to force, this is legal
                    pokemon.ability = ability.template.create(false)
                    return true
                }
        }
        // There was never a hidden ability :( possible but not by default
        return false
    }

    // check the chance of a pokemon to spawn and if it is affected by bait
    fun getPokemonSpawnChance(stack: ItemStack): Int {
        val bait = FishingBaits.getFromItemStack(stack) ?: return this.pokemonSpawnChance
        val effectList = bait.effects.filter { it.type == FishingBait.Effects.POKEMON_CHANCE }
        if (effectList.isEmpty()) return this.pokemonSpawnChance
        val effect = effectList.random()
        return if (checkBaitSuccessRate(effect.chance)) {
            ((effect.chance) * 100).toInt()
        } else this.pokemonSpawnChance
    }

    // Particle Stuff
    private fun particleEntityHandler(entity: Entity, particle: Identifier) {
        val spawnSnowstormParticlePacket = SpawnSnowstormParticlePacket(particle, entity.pos)
        spawnSnowstormParticlePacket.sendToPlayersAround(entity.x, entity.y, entity.z, 64.0, entity.world.registryKey)
    }

    companion object {
        val POKEROD_ID = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.STRING)
        val POKEBOBBER_BAIT = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.ITEM_STACK)
        val HOOK_ENTITY_ID = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
        private val CAUGHT_FISH = DataTracker.registerData(PokeRodFishingBobberEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
    }
}
