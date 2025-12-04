/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

enum class PokedexEntryCategory(name: String) {
    STARTER("starter"),
    STANDARD("standard"),
    PSEUDO_LEGEND("pseudo_legend"),
    SUB_LEGEND("sub_legend"),
    LEGEND("legend"),
    MYTHICAL("mythical");

    companion object {
        infix fun from(name: String): PokedexEntryCategory? = PokedexEntryCategory.values().firstOrNull { it.name == name }
    }
}