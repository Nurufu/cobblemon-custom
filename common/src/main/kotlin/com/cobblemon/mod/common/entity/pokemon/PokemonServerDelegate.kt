/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.entity.PokemonSideDelegate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.SentOutState
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.update
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.TrackedData
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

/** Handles purely server logic for a Pokémon */
class PokemonServerDelegate : PokemonSideDelegate {
    lateinit var entity: PokemonEntity
    var acknowledgedHPStat = -1

    lateinit var closest: ServerPlayerEntity

    /** Mocked properties exposed to the client [PokemonEntity]. */
    private val mock: PokemonProperties?
        get() = entity.effects.mockEffect?.mock

    override fun changePokemon(pokemon: Pokemon) {
        updatePathfindingPenalties(pokemon)
        entity.initGoals()
        updateMaxHealth()
    }

    fun updatePathfindingPenalties(pokemon: Pokemon) {
        val moving = pokemon.form.behaviour.moving
        entity.setPathfindingPenalty(PathNodeType.LAVA, if (moving.swim.canSwimInLava) 12F else -1F)
        entity.setPathfindingPenalty(PathNodeType.WATER, if (moving.swim.canSwimInWater) 12F else -1F)
        entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, if (moving.swim.canSwimInWater) 6F else -1F)
        if (moving.swim.canBreatheUnderwater) {
            entity.setPathfindingPenalty(PathNodeType.WATER, if (moving.walk.avoidsLand) 0F else 4F)
        }
        if (moving.swim.canBreatheUnderlava) {
            entity.setPathfindingPenalty(PathNodeType.LAVA, if (moving.swim.canSwimInLava) 4F else -1F)
        }
        if (moving.walk.avoidsLand) {
            entity.setPathfindingPenalty(PathNodeType.WALKABLE, 12F)
        }

        if (moving.walk.canWalk && moving.fly.canFly) {
            entity.setPathfindingPenalty(PathNodeType.WALKABLE, 0F)
        }

        entity.navigation.setCanPathThroughFire(entity.isFireImmune)
    }

    fun updateMaxHealth() {
        val currentHealthRatio = entity.health.toDouble() / entity.maxHealth
        // Why you would remove HP is beyond me but protects us from obscure crash due to crappy addon
        acknowledgedHPStat = entity.form.baseStats[Stats.HP] ?: return

        val minStat = 50 // Metapod's base HP
        val maxStat = 150 // Slaking's base HP
        val baseStat = acknowledgedHPStat.coerceIn(minStat..maxStat)
        val r = (baseStat - minStat) / (maxStat - minStat).toDouble()
        val minPossibleHP = 10.0 // half of a player's HP
        val maxPossibleHP = 100.0 // Iron Golem HP
        val maxHealth = minPossibleHP + r * (maxPossibleHP - minPossibleHP)

        entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        entity.health = currentHealthRatio.toFloat() * maxHealth.toFloat()
    }

    override fun initialize(entity: PokemonEntity) {
        this.entity = entity
        with(entity) {
            speed = 0.1F
            entity.despawner.beginTracking(this)
        }
        updateTrackedValues()
    }

    fun getBattle() = entity.battleId?.let(BattleRegistry::getBattle)

    fun updateTrackedValues() {
        val trackedSpecies = mock?.species ?: entity.pokemon.species.resourceIdentifier.toString()
        val trackedNickname =  mock?.nickname ?: entity.pokemon.nickname ?: Text.empty()
        val trackedAspects = mock?.aspects ?: entity.pokemon.aspects

        entity.ownerUuid = entity.pokemon.getOwnerPlayer()?.uuid
        entity.dataTracker.set(PokemonEntity.SPECIES, trackedSpecies)
        if (entity.dataTracker.get(PokemonEntity.NICKNAME) != trackedNickname) {
            entity.dataTracker.set(PokemonEntity.NICKNAME, trackedNickname)
        }
        entity.dataTracker.set(PokemonEntity.ASPECTS, trackedAspects)
        entity.dataTracker.set(PokemonEntity.LABEL_LEVEL, entity.pokemon.level)
        entity.dataTracker.set(PokemonEntity.MOVING, entity.velocity.multiply(1.0, if (entity.isOnGround) 0.0 else 1.0, 1.0).length() > 0.005F)
        entity.dataTracker.set(PokemonEntity.FRIENDSHIP, entity.pokemon.friendship)

        updatePoseType()
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        if (this::entity.isInitialized) {
            when (data) {
                PokemonEntity.BEHAVIOUR_FLAGS -> updatePoseType()
            }
        }
    }

    override fun tick(entity: PokemonEntity) {
        val state = entity.pokemon.state
        if (state !is ActivePokemonState || state.entity != entity) {
            if (!entity.isDead && entity.health > 0) {
                entity.pokemon.state = SentOutState(entity)
            }
        }

        if (entity.ownerUuid != null && entity.pokemon.storeCoordinates.get() == null) {
            entity.discard()
        }

        val tethering = entity.tethering
        if (tethering != null && entity.pokemon.tetheringId != tethering.tetheringId) {
            entity.discard()
        }

//        if (!entity.behaviour.moving.walk.canWalk && entity.behaviour.moving.fly.canFly && !entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
//            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
//        }

        if(entity.pokemon.isWild() && entity.pokemon.aspects.contains("shiny") && !entity.shined){
            if (shinyNotif(entity)) {
                entity.shined = true
            }
        }

        if(entity.pokemon.isWild() && !entity.pinged && (entity.pokemon.isLegendary() || entity.pokemon.isMythical() || entity.pokemon.isUltraBeast()))
        {
            if (legeNotif(entity)) {
                entity.pinged = true
            }
        }

        entity.dataTracker.update(PokemonEntity.BATTLE_ID) { opt ->
            val battleId = opt.orElse(null)
            if (battleId != null && BattleRegistry.getBattle(battleId).let { it == null || it.ended }) {
                Optional.empty()
            } else {
                opt
            }
        }

        val battle = getBattle()
        if (entity.ticksLived % 20 == 0 && battle != null) {
            val activeBattlePokemon = battle
                .activePokemon
                .find { it.battlePokemon?.uuid == entity.pokemon.uuid }

            if (activeBattlePokemon != null) {
                activeBattlePokemon.position = entity.world as ServerWorld to entity.pos
            }
        }

        if (entity.form.baseStats[Stats.HP] != acknowledgedHPStat) {
            updateMaxHealth()
        }

        if (entity.ownerUuid != entity.pokemon.getOwnerUUID()) {
            entity.ownerUuid = entity.pokemon.getOwnerUUID()
        }

        if (entity.ownerUuid == null && tethering != null) {
            entity.ownerUuid = tethering.playerId
        }

        if (entity.ownerUuid != null && entity.owner == null && entity.tethering == null) {
            entity.remove(Entity.RemovalReason.DISCARDED)
        }

        updateTrackedValues()
    }

    fun shinyNotif(pokemon: PokemonEntity): Boolean {
        val world = entity.world as ServerWorld
        val players = world.players
        if (players.size < 1) {
            return false
        }
        val close = ArrayList<ServerPlayerEntity>()

        players.forEach{
            if(it.pos.distanceTo(pokemon.pos) <= Cobblemon.config.shinyNoticeParticlesDistance)
            {
                close.add(it)
            }
        }
        if (close.size < 1) {
            return false
        }
        closest = close[0]
        close.forEach{
            if(it.pos.distanceTo(pokemon.pos) < closest.pos.distanceTo(pokemon.pos))
            {
                closest = it
            }
        }
        players.forEach{it.sendMessage(closest.playerListName?.let { it1 ->
            lang("shiny.notif", entity.pokemon.species.translatedName,
                it1
            )
        })}
        return true
    }

    fun legeNotif(pokemon: PokemonEntity): Boolean {
        val world = entity.world as ServerWorld
        val players = world.players
        if (players.size < 1) {
            return false
        }
        val close = ArrayList<ServerPlayerEntity>()
        //players.forEach{it.pos.distanceTo(pokemon.pos) <= Cobblemon.config.shinyNoticeParticlesDistance}
        players.forEach{
            if(it.pos.distanceTo(pokemon.pos) <= Cobblemon.config.shinyNoticeParticlesDistance)
            {
                close.add(it)
            }
        }
        if (close.size < 1) {
            return false
        }
        closest = close[0]
        close.forEach{
            if(it.pos.distanceTo(pokemon.pos) < closest.pos.distanceTo(pokemon.pos))
            {
                closest = it
            }
        }
        players.forEach{it.sendMessage(closest.playerListName?.let { it1 -> lang("lege.notif", it1) })}
        close.forEach { val cry = "pokemon."+entity.pokemon.species.toString()+".cry"
                        it.playSound(SoundEvent.of(Identifier("item.trident.thunder")),SoundCategory.MASTER, 0.3f, 0.5f)
                        it.playSound(SoundEvent.of(Identifier("cobblemon", cry)),SoundCategory.MASTER, 0.6f, 1f)}
        return true
    }

    fun updatePoseType() {
        val isSleeping = entity.pokemon.status?.status == Statuses.SLEEP && entity.behaviour.resting.canSleep
        val isMoving = entity.dataTracker.get(PokemonEntity.MOVING)
        val isPassenger = entity.hasVehicle()
        val isUnderwater = entity.getIsSubmerged()
        val isFlying = entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)

        val poseType = when {
            isPassenger -> PoseType.STAND
            isSleeping -> PoseType.SLEEP
            isMoving && isUnderwater  -> PoseType.SWIM
            isUnderwater -> PoseType.FLOAT
            isMoving && isFlying -> PoseType.FLY
            isFlying -> PoseType.HOVER
            isMoving -> PoseType.WALK
            else -> PoseType.STAND
        }

        entity.dataTracker.set(PokemonEntity.POSE_TYPE, poseType)
    }

    override fun drop(source: DamageSource?) {
        val player = source?.source as? ServerPlayerEntity
        if (entity.pokemon.isWild()) {
            entity.killer = player
        }
    }

    override fun updatePostDeath() {
        // clear active effects before proceeding
        if (entity.deathTime == 0) {
            entity.effects.wipe()
            entity.deathTime = 1
            return
        }
        else if (entity.effects.progress?.isDone == false) {
            return
        }

        entity.dataTracker.set(PokemonEntity.DYING_EFFECTS_STARTED, true)
        ++entity.deathTime

        if (entity.deathTime == 30) {
            val owner = entity.owner
            if (owner != null) {
                entity.world.playSoundServer(owner.pos, CobblemonSounds.POKE_BALL_RECALL, volume = 0.6F)
                entity.dataTracker.set(PokemonEntity.PHASING_TARGET_ID, owner.id)
                entity.dataTracker.set(PokemonEntity.BEAM_MODE, 3)
            }
        }

        if (entity.deathTime == 60) {
            if (entity.owner == null) {
                entity.world.sendEntityStatus(entity, 60.toByte()) // Sends smoke effect
                if(entity.world.gameRules.getBoolean(CobblemonGameRules.DO_POKEMON_LOOT)) {
                    (entity.drops ?: entity.pokemon.form.drops).drop(entity, entity.world as ServerWorld, entity.pos, entity.killer)
                }
            }

            entity.remove(Entity.RemovalReason.KILLED)
        }
    }
}