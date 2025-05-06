package com.cobblemon.mod.common.config

object ConfigConstants {
    object Speed {
        const val DEFAULT: Double = 1.0
        const val UNDERWATER: Double = 1.0
        const val MIN: Double = 0.0
        const val MAX: Double = 100.0
    }

    object Height {
        const val SWIM: Double = 2.0
        const val FLY: Double = 0.5
        const val MIN: Double = 0.0
        const val MAX: Double = 100.0
    }

    object SpeedLimit {
        const val VALUE: Double = 0.0
        const val MIN: Double = 0.0
        const val MAX: Double = 420.0
    }

    object Feature {
        const val IS_WATER_BREATHING_SHARED: Boolean = true
        const val CAN_DISMOUNT_IN_MIDAIR: Boolean = false
        const val USE_CAMERA_NAVIGATION: Boolean = false
    }

    object SpeedStat {
        const val ACTIVE: Boolean = true

        object Stat {
            const val MIN_STAT: Int = 20
            const val MAX_STAT: Int = 400
            const val MIN: Int = 0
            const val MAX: Int = 500
        }

        object Speed {
            const val MIN_SPEED: Double = 0.5
            const val MAX_SPEED: Double = 4.0
            const val MIN: Double = 0.0
            const val MAX: Double = 100.0
        }
    }

    object Sprinting {
        const val ACTIVE: Boolean = true
        const val ON_LAND: Boolean = true
        const val IN_WATER: Boolean = true
        const val IN_AIR: Boolean = true

        object Speed {
            const val VALUE: Double = 1.5
            const val MIN: Double = 1.0
            const val MAX: Double = 100.0
        }

        object Exhaust {
            const val ACTIVE: Boolean = true
            const val VALUE: Double = 0.5
            const val DURATION: Double = 1.0
            const val MIN: Double = 0.0
            const val MAX: Double = 1.0
        }

        object Stamina {
            const val VALUE: Int = 200
            const val MIN: Int = 1
            const val MAX: Int = 6000
        }

        object Recovery {
            const val VALUE: Int = 300
            const val MIN: Int = 1
            const val MAX: Int = 6000
        }

        object Delay {
            const val VALUE: Int = 20
            const val MIN: Int = 0
            const val MAX: Int = 6000
        }
    }
}