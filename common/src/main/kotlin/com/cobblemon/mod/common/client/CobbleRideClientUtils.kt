/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.api.gui.blitk
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

fun blitRideIcon(
    matrices: MatrixStack,
    texture: Identifier,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    scale: Float
) {
    blitk(
        matrixStack = matrices,
        texture = texture,
        x = x,
        y = y,
        width = width,
        height = height,
        scale = scale
    )
}