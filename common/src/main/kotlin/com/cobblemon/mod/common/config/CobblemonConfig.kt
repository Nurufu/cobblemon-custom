/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.config

import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.pokeball.catching.calculators.CobblemonCaptureCalculator
import com.cobblemon.mod.common.util.adapters.CaptureCalculatorAdapter
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.google.gson.GsonBuilder
import juuxel.adorn.relocated.jankson.annotation.SerializedName

class CobblemonConfig {
    //Ride stuff
    @SerializedName("general")
    var general: General = General()

    @SerializedName("client")
    var client: Client = Client()

    @SerializedName("restrictions")
    var restrictions: Restrictions = Restrictions()

    @SerializedName("speedStat")
    var speedStat: SpeedStat = SpeedStat()

    @SerializedName("sprinting")
    var sprinting: Sprinting = Sprinting()

    inner class General {
        @SerializedName("globalBaseSpeedModifier")
        var globalBaseSpeedModifier: Double = ConfigConstants.Speed.DEFAULT

        @SerializedName("globalLandSpeedModifier")
        var globalLandSpeedModifier: Double = ConfigConstants.Speed.DEFAULT

        @SerializedName("globalWaterSpeedModifier")
        var globalWaterSpeedModifier: Double = ConfigConstants.Speed.DEFAULT

        @SerializedName("globalAirSpeedModifier")
        var globalAirSpeedModifier: Double = ConfigConstants.Speed.DEFAULT

        @SerializedName("underwaterSpeedModifier")
        var underwaterSpeedModifier: Double = ConfigConstants.Speed.UNDERWATER

        @SerializedName("waterVerticalClimbSpeed")
        var waterVerticalClimbSpeed: Double = ConfigConstants.Height.SWIM

        @SerializedName("airVerticalClimbSpeed")
        var airVerticalClimbSpeed: Double = ConfigConstants.Height.FLY

        @SerializedName("rideSpeedLimit")
        var rideSpeedLimit: Double = ConfigConstants.SpeedLimit.VALUE

        @SerializedName("isWaterBreathingShared")
        var isWaterBreathingShared: Boolean = ConfigConstants.Feature.IS_WATER_BREATHING_SHARED
    }

    inner class Client {
        @SerializedName("canDismountInMidair")
        var canDismountInMidair: Boolean = ConfigConstants.Feature.CAN_DISMOUNT_IN_MIDAIR

        @SerializedName("useCameraNavigation")
        var useCameraNavigation: Boolean = ConfigConstants.Feature.USE_CAMERA_NAVIGATION
    }

    inner class Restrictions {
        @SerializedName("blacklistedDimensions")
        var blacklistedDimensions: List<String> = listOf()
    }

    inner class SpeedStat {
        @SerializedName("affectsSpeed")
        var affectsSpeed: Boolean = ConfigConstants.SpeedStat.ACTIVE

        @SerializedName("minStatThreshold")
        var minStatThreshold: Int = ConfigConstants.SpeedStat.Stat.MIN_STAT

        @SerializedName("maxStatThreshold")
        var maxStatThreshold: Int = ConfigConstants.SpeedStat.Stat.MAX_STAT

        @SerializedName("minSpeedModifier")
        var minSpeedModifier: Double = ConfigConstants.SpeedStat.Speed.MIN_SPEED

        @SerializedName("maxSpeedModifier")
        var maxSpeedModifier: Double = ConfigConstants.SpeedStat.Speed.MAX_SPEED
    }

    inner class Sprinting {
        @SerializedName("canSprint")
        var canSprint: Boolean = ConfigConstants.Sprinting.ACTIVE

        @SerializedName("rideSprintSpeed")
        var rideSprintSpeed: Double = ConfigConstants.Sprinting.Speed.VALUE

        @SerializedName("canSprintOnLand")
        var canSprintOnLand: Boolean = ConfigConstants.Sprinting.ON_LAND

        @SerializedName("canSprintInWater")
        var canSprintInWater: Boolean = ConfigConstants.Sprinting.IN_WATER

        @SerializedName("canSprintInAir")
        var canSprintInAir: Boolean = ConfigConstants.Sprinting.IN_AIR

        @SerializedName("canExhaust")
        var canExhaust: Boolean = ConfigConstants.Sprinting.Exhaust.ACTIVE

        @SerializedName("maxStamina")
        var maxStamina: Int = ConfigConstants.Sprinting.Stamina.VALUE

        @SerializedName("recoveryTime")
        var recoveryTime: Int = ConfigConstants.Sprinting.Recovery.VALUE

        @SerializedName("recoveryDelay")
        var recoveryDelay: Int = ConfigConstants.Sprinting.Delay.VALUE

        @SerializedName("exhaustionSpeed")
        var exhaustionSpeed: Double = ConfigConstants.Sprinting.Exhaust.VALUE

        @SerializedName("exhaustionDuration")
        var exhaustionDuration: Double = ConfigConstants.Sprinting.Exhaust.DURATION
    }
    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
            .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
            .registerTypeAdapter(CaptureCalculator::class.java, CaptureCalculatorAdapter)
            .create()
    }

    var searchShadow: Boolean = true
    var inBattleModifier: Boolean = false
    var lastSavedVersion: String = "0.0.1"

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 1, max = 1000)
    var maxPokemonLevel = 100

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 0, max = 1000)
    var maxPokemonFriendship = 255

    @NodeCategory(Category.Pokemon)
    var announceDropItems = true
    @NodeCategory(Category.Pokemon)
    var defaultDropItemMethod = ItemDropMethod.ON_ENTITY
    @NodeCategory(Category.Pokemon)
    @LastChangedVersion("1.4.0")
    var ambientPokemonCryTicks = 1080

    @NodeCategory(Category.Storage)
    @IntConstraint(min = 1, max = 1000)
    var defaultBoxCount = 30
    @NodeCategory(Category.Storage)
    @IntConstraint(min = 1, max = 120)
    var pokemonSaveIntervalSeconds = 30

    @NodeCategory(Category.Storage)
    var storageFormat = "nbt"

    @NodeCategory(Category.Storage)
    var preventCompletePartyDeposit = false

    @NodeCategory(Category.Storage)
    var mongoDBConnectionString = "mongodb://localhost:27017"
    @NodeCategory(Category.Storage)
    var mongoDBDatabaseName = "cobblemon"

    // TODO new types of constraint

    @NodeCategory(Category.Spawning)
    @IntConstraint(min = 1, max = 200)
    var maxVerticalCorrectionBlocks = 64

    @NodeCategory(Category.Spawning)
    @IntConstraint(min = 1, max = 1000)
    var minimumLevelRangeMax = 10

    @NodeCategory(Category.Spawning)
    var enableSpawning = true

    @NodeCategory(Category.Spawning)
    var minimumDistanceBetweenEntities = 8.0

    @NodeCategory(Category.Spawning)
    var maxNearbyBlocksHorizontalRange = 4

    @NodeCategory(Category.Spawning)
    var maxNearbyBlocksVerticalRange = 2

    @NodeCategory(Category.Spawning)
    var maxVerticalSpace = 8

    @NodeCategory(Category.Spawning)
    var worldSliceDiameter = 8

    @NodeCategory(Category.Spawning)
    var worldSliceHeight = 16

    @NodeCategory(Category.Spawning)
    var ticksBetweenSpawnAttempts = 20F

    @NodeCategory(Category.Spawning)
    var minimumSliceDistanceFromPlayer = 16F

    @NodeCategory(Category.Spawning)
    var maximumSliceDistanceFromPlayer = 16 * 4F

    @NodeCategory(Category.Spawning)
    var exportSpawnConfig = false

    @NodeCategory(Category.Spawning)
    var savePokemonToWorld = true

    @NodeCategory(Category.Starter)
    var exportStarterConfig = false

    @NodeCategory(Category.Battles)
    var autoUpdateShowdown = true

    @NodeCategory(Category.Battles)
    var defaultFleeDistance = 16F * 2

    @NodeCategory(category = Category.Battles)
    var allowExperienceFromPvP = true

    @NodeCategory(category = Category.Battles)
    var experienceShareMultiplier = .5

    @NodeCategory(category = Category.Battles)
    var luckyEggMultiplier = 1.5

    @NodeCategory(category = Category.Battles)
    var allowSpectating = true

    @NodeCategory(category = Category.Pokemon)
    var experienceMultiplier = 2F

    @NodeCategory(category = Category.Spawning)
    var pokemonPerChunk = 1F

    @NodeCategory(Category.PassiveStatus)
    var passiveStatuses = mutableMapOf(
        Statuses.POISON.configEntry(),
        Statuses.POISON_BADLY.configEntry(),
        Statuses.PARALYSIS.configEntry(),
        Statuses.FROZEN.configEntry(),
        Statuses.SLEEP.configEntry(),
        Statuses.BURN.configEntry()
    )

    @NodeCategory(Category.Healing)
    var infiniteHealerCharge = false

    @NodeCategory(Category.Healing)
    var maxHealerCharge = 6.0f

    @NodeCategory(Category.Healing)
    var chargeGainedPerTick = 0.000333333f

    @NodeCategory(Category.Healing)
    var defaultFaintTimer = 300

    @NodeCategory(Category.Healing)
    var faintAwakenHealthPercent = 0.2f

    @NodeCategory(Category.Healing)
    var healPercent = 0.05

    @NodeCategory(Category.Healing)
    var healTimer = 60

    @NodeCategory(Category.Spawning)
    var baseApricornTreeGenerationChance = 0.1F

    @NodeCategory(Category.Pokemon)
    var ninjaskCreatesShedinja = true

    @NodeCategory(Category.Pokemon)
    var displayEntityLevelLabel = true

    @NodeCategory(Category.Spawning)
    var shinyRate = 8192F

    @NodeCategory(Category.Pokemon)
    var captureCalculator: CaptureCalculator = CobblemonCaptureCalculator

    @NodeCategory(Category.Pokemon)
    var playerDamagePokemon = true

    @NodeCategory(Category.Pokemon)
    var shinyNoticeParticlesDistance = 24F

    @NodeCategory(Category.World)
    var appleLeftoversChance = 0.025

    @NodeCategory(Category.World)
    var maxRootsInArea = 5

    @NodeCategory(Category.World)
    var bigRootPropagationChance = 0.1

    @NodeCategory(Category.World)
    var energyRootChance = 0.25

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 0, max = 10)
    var maxDynamaxLevel = 10

    @NodeCategory(Category.Spawning)
    var teraTypeRate = 20F

    @NodeCategory(Category.World)
    var defaultPasturedPokemonLimit = 16

    @NodeCategory(Category.World)
    var pastureBlockUpdateTicks = 40

    @NodeCategory(Category.World)
    var pastureMaxWanderDistance = 64

    @NodeCategory(Category.World)
    var pastureMaxPerChunk = 4F

    @NodeCategory(Category.World)
    var maxInsertedFossilItems = 2

    @NodeCategory(Category.Battles)
    var walkingInBattleAnimations = false

    @NodeCategory(Category.Debug)
    var enableDebugKeys = false

        fun validate(config: CobblemonConfig) {
            with(config.general) {
                globalBaseSpeedModifier =
                    globalBaseSpeedModifier.coerceIn(ConfigConstants.Speed.MIN, ConfigConstants.Speed.MAX)
                globalLandSpeedModifier =
                    globalLandSpeedModifier.coerceIn(ConfigConstants.Speed.MIN, ConfigConstants.Speed.MAX)
                globalWaterSpeedModifier =
                    globalWaterSpeedModifier.coerceIn(ConfigConstants.Speed.MIN, ConfigConstants.Speed.MAX)
                globalAirSpeedModifier =
                    globalAirSpeedModifier.coerceIn(ConfigConstants.Speed.MIN, ConfigConstants.Speed.MAX)
                underwaterSpeedModifier =
                    underwaterSpeedModifier.coerceIn(ConfigConstants.Speed.MIN, ConfigConstants.Speed.MAX)
                waterVerticalClimbSpeed =
                    waterVerticalClimbSpeed.coerceIn(ConfigConstants.Height.MIN, ConfigConstants.Height.MAX)
                airVerticalClimbSpeed =
                    airVerticalClimbSpeed.coerceIn(ConfigConstants.Height.MIN, ConfigConstants.Height.MAX)
                rideSpeedLimit =
                    rideSpeedLimit.coerceIn(ConfigConstants.SpeedLimit.MIN, ConfigConstants.SpeedLimit.MAX)
            }

            with(config.speedStat) {
                minStatThreshold =
                    minStatThreshold.coerceIn(ConfigConstants.SpeedStat.Stat.MIN, ConfigConstants.SpeedStat.Stat.MAX)
                maxStatThreshold =
                    maxStatThreshold.coerceIn(minStatThreshold, ConfigConstants.SpeedStat.Stat.MAX)
                minSpeedModifier =
                    minSpeedModifier.coerceIn(ConfigConstants.SpeedStat.Speed.MIN, ConfigConstants.SpeedStat.Speed.MAX)
                maxSpeedModifier =
                    maxSpeedModifier.coerceIn(minSpeedModifier, ConfigConstants.SpeedStat.Speed.MAX)
            }

            with(config.sprinting) {
                rideSprintSpeed =
                    rideSprintSpeed.coerceIn(ConfigConstants.Sprinting.Speed.MIN, ConfigConstants.Sprinting.Speed.MAX)
                maxStamina =
                    maxStamina.coerceIn(ConfigConstants.Sprinting.Stamina.MIN, ConfigConstants.Sprinting.Stamina.MAX)
                recoveryTime = recoveryTime.coerceIn(
                    ConfigConstants.Sprinting.Recovery.MIN,
                    ConfigConstants.Sprinting.Recovery.MAX
                )
                recoveryDelay =
                    recoveryDelay.coerceIn(ConfigConstants.Sprinting.Delay.MIN, ConfigConstants.Sprinting.Delay.MAX)
                exhaustionSpeed = exhaustionSpeed.coerceIn(
                    ConfigConstants.Sprinting.Exhaust.MIN,
                    ConfigConstants.Sprinting.Exhaust.MAX
                )
                exhaustionDuration = exhaustionDuration.coerceIn(
                    ConfigConstants.Sprinting.Exhaust.MIN,
                    ConfigConstants.Sprinting.Exhaust.MAX
                )
            }
        }
    }

