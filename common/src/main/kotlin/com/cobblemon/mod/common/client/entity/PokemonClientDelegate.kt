/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.entity.PokemonSideDelegate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.scheduling.afterOnClient
import com.cobblemon.mod.common.api.scheduling.lerpOnClient
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer.Companion.ease
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class PokemonClientDelegate : PoseableEntityState<PokemonEntity>(), PokemonSideDelegate {
    companion object {
        const val BEAM_SHRINK_TIME = 0.4F
        const val BEAM_EXTEND_TIME = 0.2F
        const val POKEBALL_AIR_TIME = 0.5F
        const val SHINY_PARTICLE_COOLDOWN = 3.5F
    }

    override val schedulingTracker: SchedulingTracker
        get() = currentEntity.schedulingTracker
    lateinit var currentEntity: PokemonEntity
    var phaseTarget: Entity? = null
    var entityScaleModifier = 1F

    override fun getEntity() = currentEntity

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
        schedulingTracker.update(0F)
    }

    var beamStartTime = System.currentTimeMillis()
    var ballStartTime = System.currentTimeMillis()
    var lastShinyParticle = System.currentTimeMillis()
    var shined = false
    var notified = false
    var ballDone = false
    var ballOffset = 0f
    var ballRotOffset = 0f
    var sendOutPosition: Vec3d? = null
    var sendOutOffset: Vec3d? = null
    var playedSendOutSound: Boolean = false
    var playedThrowingSound: Boolean = false

    var glowTime: Int = 0

    val secondsSinceBeamEffectStarted: Float
        get() = (System.currentTimeMillis() - beamStartTime) / 1000F

    val secondsSinceBallThrown: Float
        get() = (System.currentTimeMillis() - ballStartTime) / 1000F

    val secondsSinceLastShinyParticle: Float
        get() = (System.currentTimeMillis() - lastShinyParticle) / 1000F

    private var cryAnimation: StatefulAnimation<PokemonEntity, *>? = null

    private var scaleAnimTask: ScheduledTask? = null

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        if (this::currentEntity.isInitialized) {
            if (data == PokemonEntity.SPECIES) {
                val identifier = Identifier(currentEntity.dataTracker.get(PokemonEntity.SPECIES))
                currentPose = null
                currentEntity.pokemon.species = PokemonSpecies.getByIdentifier(identifier)!! // TODO exception handling
                // force a model update - handles edge case where the PoseableEntityState's tracked PoseableEntityModel isn't updated until the LivingEntityRenderer render is run
                currentModel = PokemonModelRepository.getPoser(identifier, currentEntity.aspects)
            }   else if (data == PokemonEntity.ASPECTS) {
                currentAspects = currentEntity.dataTracker.get(PokemonEntity.ASPECTS)
                currentEntity.pokemon.shiny = currentAspects.contains("shiny")
            }
            else if (data == PokemonEntity.DYING_EFFECTS_STARTED) {
                val isDying = currentEntity.dataTracker.get(PokemonEntity.DYING_EFFECTS_STARTED)
                if (isDying) {
                    val model = (currentModel ?: return) as PokemonPoseableModel
                    val animation = try {
                        model.getAnimation(this, "faint", runtime)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    } ?: return
                    val primaryAnimation = PrimaryAnimation(animation)
                    after(seconds = 3F) { entityScaleModifier = 0F }
                    this.addPrimaryAnimation(primaryAnimation)
                }
            } else if (data == PokemonEntity.BEAM_MODE) {
                // If you make adjustments to this code, make sure to Find Usages for both PokemonEntity.beamMode and PokemonEntity.BEAM_MODE
                // TODO: change beamMode to an enum, or a set of booleans (send-out vs recall, delay vs delay)
                val beamMode = currentEntity.beamMode
                when (beamMode) {
                    0 -> { /* Do nothing */ }
                    1 -> {
                        // Scaling up out of pokeball
                        playedSendOutSound = false
                        entityScaleModifier = 0F
                        beamStartTime = System.currentTimeMillis()
                        ballStartTime = System.currentTimeMillis()
                        currentEntity.isInvisible = true
                        ballDone = false
                        var soundPos = currentEntity.pos
                        currentEntity.pokemon.getOwnerUUID()?.let{
                            currentEntity.world.getPlayerByUuid(it)?.let {
                                val offset = it.pos.subtract(currentEntity.pos.add(0.0, 2.0 - (ballOffset.toDouble()/10f), 0.0)).normalize().multiply(-ease(ballOffset.toDouble()))
                                with(it.pos.subtract(currentEntity.pos)) {
                                    var newOffset = offset.multiply(2.0)
                                    val distance = it.pos.distanceTo(currentEntity.pos)
                                    newOffset = newOffset.multiply((distance / 10.0) * 5)
                                    soundPos = currentEntity.pos.add(newOffset)
                                }
                                it.swingHand(it.activeHand ?: Hand.MAIN_HAND)
                            }
                        }
                        val client = MinecraftClient.getInstance()
                        val sound = MovingSoundInstance(SoundEvent.of(CobblemonSounds.POKE_BALL_TRAIL.id), SoundCategory.PLAYERS, { sendOutPosition?.add(sendOutOffset) }, 0.1f, 1f, false, 20, 0)
                        if(!playedThrowingSound){
                            client.soundManager.play(sound)
                            playedThrowingSound = true
                        }
                        lerpOnClient(POKEBALL_AIR_TIME) { ballOffset = it }
                        ballRotOffset = ((Math.random()) * currentEntity.world.random.nextBetween(-15, 15)).toFloat()

                        currentEntity.after(seconds = POKEBALL_AIR_TIME){
                            beamStartTime = System.currentTimeMillis()
                            ballDone = true
                            if (client.soundManager.get(CobblemonSounds.POKE_BALL_SEND_OUT.id) != null || client.soundManager.get(CobblemonSounds.POKE_BALL_SEND_OUT.id) != null && !playedSendOutSound) {
                                if(currentEntity.pokemon.shiny){
                                    client.world?.playSound(client.player, soundPos.x, soundPos.y, soundPos.z, SoundEvent.of(CobblemonSounds.POKE_BALL_SHINY_SEND_OUT.id), SoundCategory.PLAYERS, 0.6f, 1f)
                                } else {
                                    client.world?.playSound(client.player, soundPos.x, soundPos.y, soundPos.z, SoundEvent.of(CobblemonSounds.POKE_BALL_SEND_OUT.id), SoundCategory.PLAYERS, 0.6f, 1f)
                                }
                                playedSendOutSound = true
                            }
                            currentEntity.ownerUuid?.let {
                                client.world?.playSound(
                                    client.player,
                                    soundPos.x,
                                    soundPos.y,
                                    soundPos.z,
                                    SoundEvent.of(CobblemonSounds.POKE_BALL_SEND_OUT.id),
                                    SoundCategory.PLAYERS,
                                    0.6f,
                                    1f
                                )
                            }
                            // Display ball-specific particles
                            sendOutPosition?.let{
                                val newPos = it.add(sendOutOffset)
                                val ballType = currentEntity.pokemon.caughtBall.name.path.lowercase().replace("_","")
                                val mode = if(currentEntity.isBattling) "battle" else "casual"
                                val sendflash = BedrockParticleEffectRepository.getEffect(cobblemonResource("${ballType}/${mode}/sendflash"))
                                sendflash?.let { effect ->
                                    val wrapper = MatrixWrapper()
                                    val matrix = MatrixStack()
                                    matrix.translate(newPos.x, newPos.y, newPos.z)
                                    wrapper.updateMatrix(matrix.peek().positionMatrix)
                                    val world = MinecraftClient.getInstance().world ?: return@let
                                    ParticleStorm(effect, wrapper, world).spawn()
                                    val ballsparks = BedrockParticleEffectRepository.getEffect(cobblemonResource("${ballType}/${mode}/ballsparks"))
                                    val ballsendsparkle = BedrockParticleEffectRepository.getEffect(cobblemonResource("${ballType}/${mode}/ballsendsparkle"))
                                    // using afterOnClient because it's such a small timeframe that it's unlikely the entity has been removed & we'd like the precision
                                    afterOnClient(seconds = 0.01667f) {
                                        ballsparks?.let { effect ->
                                            ParticleStorm(effect, wrapper, world).spawn()
                                        }
                                        ballsendsparkle?.let { effect ->
                                            ParticleStorm(effect, wrapper, world).spawn()
                                        }
                                        currentEntity.after(seconds = 0.4f) {
                                            val ballsparkle = BedrockParticleEffectRepository.getEffect(cobblemonResource("${ballType}/ballsparkle"))
                                            ballsparkle?.let { effect ->
                                                ParticleStorm(effect, wrapper, world).spawn()
                                            }
                                        }
                                    }
                                }
                            }
                            currentEntity.after(seconds = POKEBALL_AIR_TIME) {
                                if(scaleAnimTask == null || scaleAnimTask!!.expired){
                                    scaleAnimTask = lerpOnClient(BEAM_SHRINK_TIME) { entityScaleModifier = it }
                                    currentEntity.isInvisible = false
                                    currentEntity.after(seconds = POKEBALL_AIR_TIME*2){
                                        ballOffset = 0f
                                        ballRotOffset = 0f
                                        sendOutPosition = null
                                }

                                }
                            }
                        }
                    }
                    2 -> {
                        // Scaling up with no delay and no particles
                        if(ballDone){
                            playedSendOutSound = false
                            entityScaleModifier = 0F
                            currentEntity.isInvisible = false
                            ballDone = false
                            val soundPos = currentEntity.pos
                            val client = MinecraftClient.getInstance()

                            val soundEvent = if(currentEntity.pokemon.shiny){
                                CobblemonSounds.POKE_BALL_SHINY_SEND_OUT
                            } else {
                                CobblemonSounds.POKE_BALL_SEND_OUT
                            }
                            currentEntity.after(seconds=0.5F){
                                ballDone = true
                            }
                            if(!playedSendOutSound && client.soundManager.get(soundEvent.id) != null && currentEntity.ownerUuid != null){
                                client.world?.playSound(
                                    client.player,
                                    soundPos.x,
                                    soundPos.y,
                                    soundPos.z,
                                    soundEvent,
                                    SoundCategory.PLAYERS,
                                    0.6f,
                                    1f
                                )
                                playedSendOutSound = true
                            }
                            scaleAnimTask = lerpOnClient (BEAM_SHRINK_TIME) { entityScaleModifier = it }
                            currentEntity.after (seconds = BEAM_SHRINK_TIME*2) {
                                ballOffset=0f
                                ballRotOffset=0f
                                sendOutPosition=null
                            }
                        }
                    }
                    3 -> {
                        // Scaling down into pokeball
                        entityScaleModifier = 1F
                        beamStartTime = System.currentTimeMillis()
                        ballOffset = 0f
                        ballRotOffset = 0f
                        sendOutPosition = null
                        afterOnClient(seconds = BEAM_EXTEND_TIME) {
                            entityScaleModifier = 1F
                        }
                        scaleAnimTask?.expire()
                        scaleAnimTask = lerpOnClient(BEAM_SHRINK_TIME){
                            entityScaleModifier = (1-it)
                        }
                    }
                    else -> { /* Do nothing */ }
                }
            } else if (data == PokemonEntity.LABEL_LEVEL) {
                currentEntity.dataTracker.get(PokemonEntity.LABEL_LEVEL)
                    .takeIf { it > 0 }
                    ?.let { currentEntity.pokemon.level = it }
            } else if (data == PokemonEntity.PHASING_TARGET_ID) {
                val phasingTargetId = currentEntity.dataTracker.get(PokemonEntity.PHASING_TARGET_ID)
                if (phasingTargetId != -1) {
                    setPhaseTarget(phasingTargetId)
                } else {
                    phaseTarget = null
                }
            }
        }
    }

    override fun changePokemon(pokemon: Pokemon) {
        pokemon.isClient = true
    }

    override fun initialize(entity: PokemonEntity) {
        this.currentEntity = entity
        this.age = entity.age

        this.runtime.environment.getQueryStruct().addFunctions(mapOf(
            "in_battle" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.isBattling)
            },
            "shiny" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.shiny)
            },
            "form" to java.util.function.Function {
                return@Function StringValue(currentEntity.pokemon.form.name)
            },
            "width" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.boundingBox.xLength)
            },
            "height" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.boundingBox.yLength)
            },
            "weight" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.species.weight.toDouble())
            },
            "evo_glow_time" to java.util.function.Function {
              it.get<MoValue?>(0).asDouble().let { glowTime = (it * 20).toInt()}
              return@Function DoubleValue(this.glowTime.toDouble())
            },
            "friendship" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.friendship.toDouble())
            }
        ))
    }

    override fun tick(entity: PokemonEntity) {
        updateLocatorPosition(entity.pos)
        incrementAge(entity)
        if(currentEntity.ownerUuid == null && currentEntity.pokemon.shiny && secondsSinceLastShinyParticle > SHINY_PARTICLE_COOLDOWN && !currentEntity.isBattling) {
            playShinyEffect("cobblemon:shiny_sparkle_ambient_wild")
            lastShinyParticle = System.currentTimeMillis()
        }
        getClientShinyPokemon()
    }

    fun getClientShinyPokemon() {
        val player = MinecraftClient.getInstance().player ?: return
        val isWithinRange = player.pos.distanceTo(currentEntity.pos) <= Cobblemon.config.shinyNoticeParticlesDistance

        if(currentEntity.pokemon.shiny && currentEntity.ownerUuid == null && !player.isSpectator){
            if(isWithinRange && !shined && !notified){
                playShinyEffect("cobblemon:wild_shiny_ring")
                shined = true
                notified = true
                lastShinyParticle = System.currentTimeMillis()
            } else if(isWithinRange && !shined && notified){
                playShinyEffect("cobblemon:wild_shiny_ring")
                shined = true
                lastShinyParticle = System.currentTimeMillis()
            } else if (!isWithinRange && notified) {
                shined = false
            }
        }
    }

    fun playShinyEffect(particleId: String){
        val locator = listOf("shiny_particles","middle").firstOrNull{this.locatorStates[it] != null} ?: "root"
        check(locator != "root") {return}
        runtime.resolve("q.particle('$particleId', '$locator')".asExpressionLike())
    }

    fun setPhaseTarget(targetId: Int) {
        this.phaseTarget = currentEntity.world.getEntityById(targetId)
    }

    override fun spawnShinyParticle(player: PlayerEntity) {
        if(secondsSinceLastShinyParticle > SHINY_PARTICLE_COOLDOWN) {
            playShinyEffect("cobblemon:ambient_shiny_sparkle")
            lastShinyParticle = System.currentTimeMillis()
        }
    }

    override fun handleStatus(status: Byte) {
        if (status == 10.toByte()) {
            val model = (currentModel ?: return) as PokemonPoseableModel
            val animation = model.getEatAnimation(currentEntity, this) ?: return
            statefulAnimations.add(animation)
        }
    }

    override fun updatePostDeath() {
        ++currentEntity.deathTime
    }

    fun cry() {
        val model = currentModel ?: return
        if (model is PokemonPoseableModel) {
           if (cryAnimation != null && (cryAnimation in statefulAnimations || cryAnimation == primaryAnimation)) {
               return
           }

            val animation = model.cryAnimation(currentEntity, this) ?: return
            if (animation is PrimaryAnimation) {
                addPrimaryAnimation(animation)
            } else {
                statefulAnimations.add(animation)
            }
            cryAnimation = animation
        }
    }
}