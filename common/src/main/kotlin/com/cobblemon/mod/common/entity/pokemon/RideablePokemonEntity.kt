package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.RideablePokemonSpecies
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.client.settings.ClientSettings
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.mixin.accessor.LivingEntityAccessor
import com.cobblemon.mod.common.net.messages.client.pokemon.ai.ClientMoveBehaviour
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetRidePokemonExhaustPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RideableFormData
import com.cobblemon.mod.common.util.averageOfTwoRots
import com.cobblemon.mod.common.util.emitParticle
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.google.common.collect.UnmodifiableIterator
import net.minecraft.entity.*
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.FluidTags
import net.minecraft.registry.tag.FluidTags.WATER
import net.minecraft.util.ActionResult
import net.minecraft.util.Arm
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

class RideablePokemonEntity : PokemonEntity, Mount {
    constructor(world: World) : super(world)
    constructor(
        world: World,
        pokemon: Pokemon
    ) : super(world, pokemon)

    // Ride data from RideablePokemonSpecies, is null if there is no data file for the species or form
    private val rideData: RideableFormData?
        get() = RideablePokemonSpecies.getByName(exposedSpecies.showdownId())?.getForm(exposedForm.name)

    // Used clientside to ensure that we can still access move behaviour for resolving ride logic
    var moveBehaviour: ClientMoveBehaviour = ClientMoveBehaviour(exposedForm.behaviour.moving)

    private val config: ServerSettings
        get() = ServerSettings

    private var lastRiderPosition: Vec3d? = null
    private var shouldSinkInWater = false
    private var sprintCooldownScale = 0F
    var sprintStaminaScale = 0F

    val canSprint: Boolean
        get() = config.sprinting.canSprint
    val canExhaust: Boolean
        get() = config.sprinting.canExhaust
    var isExhausted: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (world.isClient()) SetRidePokemonExhaustPacket(this.id, value).sendToServer()
            }
        }

    val isRideAscending: Boolean
        get() = controllingPassenger != null && (controllingPassenger as LivingEntityAccessor).jumping
    var isRideDescending: Boolean = false
    var isRideSprinting: Boolean = false

    private fun canBeRiddenBy(player: PlayerEntity): Boolean =
        rideData != null && rideData!!.enabled && (canBeControlledBy(player) || !this.isBattling) && isAllowedDimension()

    private fun isAllowedDimension() = !config.restrictions.blacklistedDimensions.contains(
        world.dimension.toString()
    )

    override fun isImmobile(): Boolean =
        pokemon.status?.status == Statuses.SLEEP || super.isImmobile()

    override fun isOwner(livingEntity: LivingEntity): Boolean {
        return this.ownerUuid == livingEntity.uuid
    }

    override fun isReadyToSitOnPlayer(): Boolean =
        !hasPassengers() && super.isReadyToSitOnPlayer()

    override fun canWalkOnFluid(state: FluidState): Boolean = !shouldSinkInWater &&
            if (state.isIn(WATER) && !isSubmergedIn(WATER)) {
                behaviour.moving.swim.canWalkOnWater
            } else if (state.isIn(FluidTags.LAVA) && !isSubmergedIn(FluidTags.LAVA)) {
                behaviour.moving.swim.canWalkOnLava
            } else {
                super.canWalkOnFluid(state)
            }

    private fun isOnWaterSurface(): Boolean =
        this.isTouchingWater && !this.isSubmergedInWater

    private fun isAbleToDive(): Boolean =
        moveBehaviour.swim.canSwimInWater && moveBehaviour.swim.canBreatheUnderwater

    private fun isAbleToFly(): Boolean =
        moveBehaviour.fly.canFly

    private fun isInPoseOfType(poseType: PoseType): Boolean =
        this.getCurrentPoseType() == poseType

    /* Doesn't exist in 1.20 :(
    override fun getPassengerAttachmentPoint(entity: Entity, dimensions: EntityDimensions, f: Float): Vec3d {
        val offsets = rideData?.offsets ?: hashMapOf()
        var offset = offsets[DEFAULT] ?: Vec3d.ZERO
        fun hasOffset(name: RiderOffsetType): Boolean = offsets[name] != null

        if (isFlying() && isInPoseOfType(PoseType.HOVER) && hasOffset(HOVERING)) {
            offsets[HOVERING]?.let { offset = offset.add(it) }
        } else if (isFlying() && hasOffset(FLYING)) {
            offsets[FLYING]?.let { offset = offset.add(it) }
        } else if (isSubmergedInWater && isInPoseOfType(PoseType.FLOAT) && hasOffset(SUSPENDED)) {
            offsets[SUSPENDED]?.let { offset = offset.add(it) }
        } else if (isSubmergedInWater && hasOffset(DIVING)) {
            offsets[DIVING]?.let { offset = offset.add(it) }
        } else if (isOnWaterSurface() && isInPoseOfType(PoseType.STAND) && hasOffset(FLOATING)) {
            offsets[FLOATING]?.let { offset = offset.add(it) }
        } else if (isOnWaterSurface() && hasOffset(SWIMMING)) {
            offsets[SWIMMING]?.let { offset = offset.add(it) }
        } else if (isInPoseOfType(PoseType.WALK) && hasOffset(WALKING)) {
            offsets[WALKING]?.let { offset = offset.add(it) }
        }

        if (aspects.any { it.contains(DataKeys.HAS_BEEN_SHEARED) } && hasOffset(SHEARED)) {
            offsets[SHEARED]?.let { offset = offset.add(it) }
        }

        var attachmentPoint = super.getPassengerAttachmentPoint(entity, dimensions, f)
            .add(offset)

        val lastPos = lastRiderPosition
        if (lastPos != null && lastPos.distanceTo(attachmentPoint) > 0.05) {
            attachmentPoint = attachmentPoint.multiply(0.2, 0.2, 0.2)
                .add(lastPos.multiply(0.8, 0.8, 0.8))
        }
        lastRiderPosition = attachmentPoint

        val rotatedOffset = rotateVec3d(attachmentPoint, yRotO)

        return rotatedOffset
    }*/

    override fun updatePassengerForDismount(entity: LivingEntity): Vec3d {
        val Vec3d = getPassengerDismountOffset(
            this.width.toDouble(), entity.width.toDouble(),
            (this.y + (if (entity.mainArm == Arm.RIGHT) 90.0f else -90.0f)).toFloat()
        )
        val Vec3d1: Vec3d? = this.getDismountLocationInDirection(Vec3d, entity)
        if (Vec3d1 != null) {
            return Vec3d1
        } else {
            val Vec3d2 = getPassengerDismountOffset(
                this.width.toDouble(), entity.width.toDouble(),
                (this.y + (if (entity.mainArm == Arm.LEFT) 90.0f else -90.0f)).toFloat()
            )
            val Vec3d3: Vec3d? = this.getDismountLocationInDirection(Vec3d2, entity)
            return Vec3d3 ?: this.pos
        }
    }

    private fun getDismountLocationInDirection(arg: Vec3d, arg2: LivingEntity): Vec3d? {
        val d0 = this.x + arg.x
        val d1 = this.boundingBox.minY
        val d2 = this.z + arg.z
        val mutableBlockPos = BlockPos.Mutable()
        val var10: UnmodifiableIterator<*> = arg2.poses.iterator()

        while (var10.hasNext()) {
            val pose = var10.next() as EntityPose
            mutableBlockPos[d0, d1] = d2
            val d3 = this.boundingBox.maxY + 0.75

            while (true) {
                val d4 = world.getDismountHeight(mutableBlockPos)
                if (mutableBlockPos.y.toDouble() + d4 > d3) {
                    break
                }

                if (Dismounting.canDismountInBlock(d4)) {
                    val box = arg2.getBoundingBox(pose)
                    val Vec3d = Vec3d(d0, mutableBlockPos.y.toDouble() + d4, d2)
                    if (Dismounting.canPlaceEntityAt(this.world, arg2, box.offset(Vec3d))) {
                        arg2.pose = pose
                        return Vec3d
                    }
                }

                mutableBlockPos.move(Direction.UP)
                if (mutableBlockPos.y.toDouble() < d3) {
                    break
                }
            }
        }

        return null
    }

    override fun getControllingPassenger(): LivingEntity? {
        val entity = this.firstPassenger
        if (entity is LivingEntity) {
            return if (canBeControlledBy(entity)) entity else null
        }
        return super.getControllingPassenger()
    }

    private fun canBeControlledBy(entity: LivingEntity): Boolean =
        entity is PlayerEntity && this.isOwner(entity)

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float, damageSource: DamageSource?): Boolean {
        val flag = super.handleFallDamage(fallDistance, damageMultiplier, damageSource)
        if (flag && this.hasPassengers()) {
            val i = this.computeFallDamage(fallDistance, damageMultiplier)
            val var5: Iterator<*> = this.passengersDeep.iterator()
            while (var5.hasNext()) {
                val entity = var5.next() as Entity
                entity.damage(damageSource, i.toFloat())
            }
        }
        return flag
    }

    private fun doPlayerRide(player: PlayerEntity) {
        player.yaw = yaw
        player.pitch = pitch
        if (!world.isClient()) {
            player.startRiding(this)
        }
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        val result = super.interactMob(player, hand)
        if (result == ActionResult.PASS && Cobblemon.implementation.canInteractToMount(player, hand, this)) {
            if (!this.hasPassengers() && this.canBeRiddenBy(player)) {
                this.doPlayerRide(player)
                return ActionResult.success(world.isClient())
            } else if (this.hasPassengers() && this.isOwner(player)
                && this.passengerList.none { it.uuid == player.uuid }
            ) {
                if (!world.isClient())
                    this.removeAllPassengers()
                return ActionResult.success(world.isClient)
            }
        }
        return result
    }

    fun shouldRiderSit(): Boolean = rideData?.shouldRiderSit ?: true

    override fun tickControlled(player: PlayerEntity, vec3d: Vec3d) {
        super.tickControlled(player, vec3d)
        val vec2 = this.getRiddenRotation(player)

        // Some lerping applied, to provide more realistic "weight" to rotation
        this.setRotation(averageOfTwoRots(vec2.y, this.yaw), averageOfTwoRots(vec2.x, this.pitch))
        this.headYaw = vec2.y
        this.bodyYaw = averageOfTwoRots(this.headYaw, this.yaw)
        this.yaw = averageOfTwoRots(this.bodyYaw, this.yaw)

        if (!shouldRiderSit()) {
            player.bodyYaw = this.bodyYaw
            player.yaw = this.yaw
            player.updateLimbs(false)
        }

        // If shared water breathing is allowed, check mount and apply if it has the effect
        if (moveBehaviour.swim.canBreatheUnderwater && config.general.isWaterBreathingShared) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.WATER_BREATHING, 60, 0, false, false, false))
        }

        if (this.isLogicalSideForUpdatingMovement) {
            // Carry over logic from PokemonMoveControl, where Pokemon that can swim will tread water
            if ((this.isInLava && !moveBehaviour.swim.canBreatheUnderlava) ||
                (((moveBehaviour.swim.canSwimInWater && !moveBehaviour.swim.canBreatheUnderwater) ||
                        (isAbleToDive() && isOnWaterSurface() && !isRideDescending))
                        && this.isTouchingWater && this.getFluidHeight(
                    WATER
                ) > this.swimHeight)
            ) {
                if (this.random.nextFloat() < 0.8f) {
                    this.jumpControl.setActive()
                }
            }

            // When descending, disable water walking for diving Pokemon
            shouldSinkInWater = isAbleToDive() && (isRideDescending || (isOnWaterSurface() && this.getFluidHeight(
                WATER
            ) > this.swimHeight))

            // Sprint control logic
            val shouldBeSprinting = canSprint && (isTouchingWater || isFlying() || config.sprinting.canSprintOnLand)
                    && (!isTouchingWater || config.sprinting.canSprintInWater) && (!isFlying() || config.sprinting.canSprintInAir)
                    && isRideSprinting && vec3d.horizontalLength() > 0 && (!canExhaust || (!isExhausted && sprintStaminaScale > 0F))
            if (shouldBeSprinting) {
                sprintCooldownScale = 0F
                if (canExhaust) {
                    sprintStaminaScale = max(sprintStaminaScale - (1F / config.sprinting.maxStamina), 0F)
                    if (sprintStaminaScale == 0F)
                        isExhausted = true
                }
            }
            this.isSprinting = shouldBeSprinting
            // Sets player POV, a bit dirty but it will work for now until the inevitable bugs (possibly unintended hunger drain?)
            player.isSprinting = shouldBeSprinting
        }

        // If the Pokemon is flying and can land, clear the flying state
        if (this.isFlying() && (this.isOnGround || isTouchingWater) && this.couldStopFlying()) {
            this.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
        }

        // Set flying state if we should be flying, or jump if on land
        if (isRideAscending) {
            if (isAbleToFly() && !this.isFlying() && !getIsSubmerged()) {
                this.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
            } else if (!isAbleToFly() && (this.isOnGround || (isTouchingWater && !isSubmergedInWater))) {
                this.jumpControl.setActive()
            }
        }

        // Activate any jumps that have been queued up this tick
        this.jumpControl.tick()
    }

    override fun tick() {
        super.tick()
//        Don't have rafts, uneeded I think?
//        if (isBattling) {
//            // Copying this from Cobblemon to fix a few bugs related to rafts where they don't get the behaviour on the client
//            platform = if (ticksLived > 5
//                && ownerUUID != null
//                && isInWater && !isUnderWater
//                && !exposedForm.behaviour.moving.swim.canBreatheUnderwater && !exposedForm.behaviour.moving.swim.canWalkOnWater
//                && !isFlying()
//            ) {
//                PlatformType.getPlatformTypeForPokemon((exposedForm))
//            } else {
//                PlatformType.NONE
//            }
//        } else {
//            // Battle clone destruction
//            if (this.beamMode == 0 && this.isBattleClone()) {
//                discard()
//                return
//            }
//            platform = PlatformType.NONE
//
//        }

        // Resolve stamina recovery and exhaustion effects
        if (canSprint && canExhaust) {
            if (world.isClient()) {
                if (!this.isSprinting && sprintStaminaScale < 1F) {
                    if (config.sprinting.recoveryDelay > 0 && sprintCooldownScale < 1F) {
                        sprintCooldownScale = min(sprintCooldownScale + (1F / config.sprinting.recoveryDelay), 1F)
                    } else {
                        sprintStaminaScale = min(sprintStaminaScale + (1F / config.sprinting.recoveryTime), 1F)
                    }
                }

                if (!(isExhausted && sprintStaminaScale < config.sprinting.exhaustionDuration)) {
                    isExhausted = false
                }
            }

            // Emit particles if exhausted, every X ticks
            if (isExhausted && age % 4 == 0) {
                emitParticle(this, ParticleTypes.FALLING_WATER)
            }
        }
    }

    private fun getRiddenRotation(entity: LivingEntity): Vec2f {
        return Vec2f(entity.pitch * 0.5f, entity.yaw)
    }

    override fun getControlledMovementInput(player: PlayerEntity, v: Vec3d): Vec3d {
        if (isImmobile) {
            return Vec3d.ZERO
        }

        val xxa = player.sidewaysSpeed * 0.5f
        var zza = player.forwardSpeed
        if (zza <= 0.0f) {
            zza *= 0.25f
        }
        var vec3d = Vec3d(xxa.toDouble(), 0.0, zza.toDouble())

        // We rotate the vector here to align with the player's line of view if camera navigation is enabled.
        if (ClientSettings.useCameraNavigation && vec3d.length() > 0) {
            if ((isTouchingWater && isAbleToDive() && (getIsSubmerged() || isRideDescending)) || isInLava || isFlying()) {
                vec3d = vec3d.rotateX((-player.pitch.toRadians()))
            }
        }

        // Adjust vertical movement if we're holding a key, regardless of navigation mode
        if ((isTouchingWater && isAbleToDive()) || isInLava || isFlying()) {
            val verticalSpeed = (
                    if (isFlying()) {
                        config.general.airVerticalClimbSpeed
                    } else {
                        config.general.waterVerticalClimbSpeed
                    }
                    ).toFloat()
            if (isRideAscending && !isRideDescending) {
                vec3d = vec3d.add(0.0, verticalSpeed.toDouble(), 0.0)
            } else if (isRideDescending && !isRideAscending) {
                vec3d = vec3d.add(0.0, -verticalSpeed.toDouble(), 0.0)
            }
        }

        return vec3d
    }

    override fun getSaddledSpeed(player: PlayerEntity): Float {
        val speedModifier = if (config.speedStat.affectsSpeed) {
            val minSpeedStat = config.speedStat.minStatThreshold
            val maxSpeedStat = config.speedStat.maxStatThreshold
            val minSpeedModifier = config.speedStat.minSpeedModifier
            val maxSpeedModifier = config.speedStat.maxSpeedModifier
            val clampedSpeedStat = pokemon.speed.coerceIn(minSpeedStat, maxSpeedStat).toDouble()
            val scaledSpeedStat = (clampedSpeedStat - minSpeedStat) / (maxSpeedStat - minSpeedStat)
            minSpeedModifier + scaledSpeedStat * (maxSpeedModifier - minSpeedModifier)
        } else {
            1.0
        }

        // Get land, water, air speed modifiers based on behaviour settings and data
        // Except not from behaviour since that varies at times from Pokemon to Pokemon and has been causing some silly
        //   inconsistencies, so we'll use a constant here to keep things more standardized to their actual ride data
        //   and speed stats
        val mediumSpeed = 0.35F
        val mediumModifier = if (isFlying()) {
            (rideData?.airSpeedModifier ?: 1.0F) * config.general.globalAirSpeedModifier
        } else if (isTouchingWater || isInLava) {
            // Adds a little speed to submerged Pokemon for better ride feel
            (rideData?.waterSpeedModifier
                ?: 1.0F) * config.general.globalWaterSpeedModifier * if (isAbleToDive() && getIsSubmerged()) {
                config.general.underwaterSpeedModifier
            } else {
                1.0
            }
        } else {
            (rideData?.landSpeedModifier ?: 1.0F) * config.general.globalLandSpeedModifier
        }

        val sprintModifier = if (this.isSprinting) {
            config.sprinting.rideSprintSpeed / 1.3
        } else if (this.isExhausted) {
            config.sprinting.exhaustionSpeed
        } else {
            1.0
        }

        // Calculate final adjusted speed
        val baseSpeed =
            getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * config.general.globalBaseSpeedModifier * (rideData?.baseSpeedModifier
                ?: 1.0F)
        val adjustedSpeed = baseSpeed * mediumSpeed * mediumModifier * speedModifier * sprintModifier

        return (
                if (config.general.rideSpeedLimit > 0) {
                    min(adjustedSpeed, config.general.rideSpeedLimit / 43.17)
                } else {
                    adjustedSpeed
                }
                ).toFloat()
    }

    override fun applyFluidMovingSpeed(d: Double, bl: Boolean, Vec3d: Vec3d): Vec3d {
        return super.applyFluidMovingSpeed(
            if (isAbleToDive() && isSubmergedInWater) {
                0.0
            } else {
                d
            }, bl, Vec3d
        )
    }
}