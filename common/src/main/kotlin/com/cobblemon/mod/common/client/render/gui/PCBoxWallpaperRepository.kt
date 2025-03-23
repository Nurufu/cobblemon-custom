/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.gui

import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object PCBoxWallpaperRepository {
    lateinit var wallpapers: List<Identifier>
    var defaultWallpaper = cobblemonResource("textures/gui/pc/pc_screen_overlay.png")

    fun findWallpapers(resourceManager: ResourceManager): List<Identifier> {
        val wallpapers = mutableListOf<Identifier>(defaultWallpaper)
        resourceManager.findResources("textures/gui/pc/wallpaper") { path -> path?.endsWith(".png") == true }.keys.forEach { key ->
            wallpapers.add(key)
        }
        this.wallpapers = wallpapers
        return wallpapers
    }
}
