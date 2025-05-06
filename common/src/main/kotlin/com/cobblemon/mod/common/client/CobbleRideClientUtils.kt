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