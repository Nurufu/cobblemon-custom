/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.settings

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket

/**
 * A holder for config options the server wants to sync with the client.
 * See [ServerSettingsPacket] & [ServerSettingsPacketHandler] for more information.
 *
 * @author Licious
 * @since September 27th, 2022
 */
object ServerSettings {

    var preventCompletePartyDeposit = Cobblemon.config.preventCompletePartyDeposit
    var displayEntityLevelLabel = Cobblemon.config.displayEntityLevelLabel

    var general = General
    var restrictions = Restrictions
    var speedStat = SpeedStat
    var sprinting = Sprinting

    object General {
        var globalBaseSpeedModifier: Double = Cobblemon.config.general.globalBaseSpeedModifier
        var globalLandSpeedModifier: Double = Cobblemon.config.general.globalLandSpeedModifier
        var globalWaterSpeedModifier: Double = Cobblemon.config.general.globalWaterSpeedModifier
        var globalAirSpeedModifier: Double = Cobblemon.config.general.globalAirSpeedModifier
        var underwaterSpeedModifier: Double = Cobblemon.config.general.underwaterSpeedModifier
        var waterVerticalClimbSpeed: Double = Cobblemon.config.general.waterVerticalClimbSpeed
        var airVerticalClimbSpeed: Double = Cobblemon.config.general.airVerticalClimbSpeed
        var rideSpeedLimit: Double = Cobblemon.config.general.rideSpeedLimit
        var isWaterBreathingShared: Boolean = Cobblemon.config.general.isWaterBreathingShared
    }

    object Restrictions {
        var blacklistedDimensions: List<String> = Cobblemon.config.restrictions.blacklistedDimensions
    }

    object SpeedStat {
        var affectsSpeed: Boolean = Cobblemon.config.speedStat.affectsSpeed
        var minStatThreshold: Int = Cobblemon.config.speedStat.minStatThreshold
        var maxStatThreshold: Int = Cobblemon.config.speedStat.maxStatThreshold
        var minSpeedModifier: Double = Cobblemon.config.speedStat.minSpeedModifier
        var maxSpeedModifier: Double = Cobblemon.config.speedStat.maxSpeedModifier
    }

    object Sprinting {
        var canSprint: Boolean = Cobblemon.config.sprinting.canSprint
        var rideSprintSpeed: Double = Cobblemon.config.sprinting.rideSprintSpeed
        var canSprintOnLand: Boolean = Cobblemon.config.sprinting.canSprintOnLand
        var canSprintInWater: Boolean = Cobblemon.config.sprinting.canSprintInWater
        var canSprintInAir: Boolean = Cobblemon.config.sprinting.canSprintInAir
        var canExhaust: Boolean = Cobblemon.config.sprinting.canExhaust
        var maxStamina: Int = Cobblemon.config.sprinting.maxStamina
        var recoveryTime: Int = Cobblemon.config.sprinting.recoveryTime
        var recoveryDelay: Int = Cobblemon.config.sprinting.recoveryDelay
        var exhaustionSpeed: Double = Cobblemon.config.sprinting.exhaustionSpeed
        var exhaustionDuration: Double = Cobblemon.config.sprinting.exhaustionDuration
    }

}