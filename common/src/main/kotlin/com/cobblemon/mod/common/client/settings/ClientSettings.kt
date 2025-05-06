package com.cobblemon.mod.common.client.settings

import com.cobblemon.mod.common.Cobblemon


object ClientSettings {
    var canDismountInMidair: Boolean = Cobblemon.config.client.canDismountInMidair
    var useCameraNavigation: Boolean = Cobblemon.config.client.useCameraNavigation
}