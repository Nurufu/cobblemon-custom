package com.cobblemon.mod.common.client.pokedex

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.GameRenderer
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper.clamp
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the UI for scanning a pokemon
 *
 * @author Village
 */
class PokedexScannerRenderer {
    fun renderScanProgress(graphics: DrawContext, progress: Int) {
        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val centerX = screenWidth / 2.0
        val centerY = screenHeight / 2.0
        val radius = 50.0  // Radius of the circle
        val segments = 100  // Number of segments in the circle

        val progressAngle = 360.0 * (progress / 100.0)  // Calculate the angle for the current progress

        // Prepare rendering
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)

        // Start drawing
        val tessellator = Tessellator.getInstance().buffer
        tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)
        // Center vertex
        tessellator
            .vertex(centerX, centerY, 0.0)
            .color(0.0f, 1.0f, 1.0f, 1.0f)

        // Circle vertices
        for (i in 0..segments) {
            val angle = progressAngle * (i / segments.toDouble()) * (Math.PI / 180.0)
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            tessellator
                .vertex(x, y, 0.0)
                .color(0.0f, 1.0f, 1.0f, 1.0f)
        }

        // Finish drawing
        BufferRenderer.drawWithGlobalProgram(tessellator.end())

        // Restore rendering state
        RenderSystem.disableBlend()
    }

    fun renderPhotodexOverlay(graphics: DrawContext, tickDelta: Float, scale: Float) {
        val client = MinecraftClient.getInstance()
        val matrices = graphics.matrices
        val usageContext = CobblemonClient.pokedexUsageContext

        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        // Texture dimensions
        val textureWidth = 345
        val textureHeight = 207

        // Get player yaw and convert to degrees in a circle
        val yaw = client.player?.lastRenderYaw ?: 0f
        val degrees = (yaw % 360 + 360) % 360  // Normalize the angle

        // Compass configuration
        val compassPoints = arrayOf("n", "i", "e", "i", "s", "i", "w", "i")
        val degreesPerSegment = 360 / compassPoints.size
        val centerIndex = Math.round(degrees / degreesPerSegment) % compassPoints.size
        val visibleSegments = arrayOfNulls<String>(5)  // Showing 5 segments at a time
        for (i in visibleSegments.indices) {
            val index = (centerIndex + i - 2 + compassPoints.size) % compassPoints.size
            visibleSegments[i] = compassPoints[index]
        }

        // Render Compass at the Top Center
        val compassSpacing = 20  // Width of each compass segment texture
        val compassStartX = (screenWidth - compassSpacing * visibleSegments.size) / 2
        val compassY = 10  // Top of the screen
        for (i in visibleSegments.indices) {
            val segmentTexture = getCompassTexture(visibleSegments[i] ?: "i")  // Assuming a method to get the right texture
            blitk(matrixStack = matrices, texture = segmentTexture, x = compassStartX + i * compassSpacing, y = compassY, width = 16, height = 16, alpha = 1.0F)
        }

        RenderSystem.enableBlend()
        // Pokédex zoom in/out animation
        val effectiveTicks = clamp(usageContext.transitionTicks + (if (usageContext.scanningGuiOpen) 1 else -1) * tickDelta, 0F, 12F)
        if (effectiveTicks <= 12) {
            val scale = 1 + (if (effectiveTicks <= 2) 0F else ((effectiveTicks - 2) * 0.075F))

            // Calculate centered position
            val x = (screenWidth - (textureWidth * scale)) / 2
            val y = (screenHeight - (textureHeight * scale)) / 2

            val opacity = if (effectiveTicks <= 2) 1F else (10F - (effectiveTicks.toFloat() - 2F)) / 10F
            blitk(matrixStack = matrices, texture = CobblemonClient.pokedexUsageContext.type.getTexturePath(), x = x / scale, y = y / scale, width = textureWidth, height = textureHeight, scale = scale, alpha = opacity)
            blitk(matrixStack = matrices, texture = SCAN_SCREEN, x = x / scale, y = y / scale, width = textureWidth, height = textureHeight, scale = scale, alpha = opacity)
        }

        // Scanning overlay
        val opacity = if (effectiveTicks >= 10) 1F else effectiveTicks/10F
        // Draw scan lines
        for (i in 0 until screenHeight) {
            if (i % 4 == 0) blitk(matrixStack = matrices, texture = SCAN_OVERLAY_LINES, x = 0, y = i, width = screenWidth, height = 4, alpha = opacity)
        }

        // Draw border
        // Top left corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = 0, y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, alpha = opacity)
        // Top right corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = (screenWidth - 4), y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, uOffset = 4, alpha = opacity)
        // Bottom left corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = 0, y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, alpha = opacity)
        // Bottom right corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = (screenWidth - 4), y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, uOffset = 4, alpha = opacity)

        // Border sides
        val notchStartX = (screenWidth - 200) / 2
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_TOP, x = 4, y = 0, width = notchStartX - 4, height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_TOP, x = notchStartX + 200, y = 0, width = (screenWidth - (notchStartX + 200 + 4)), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_BOTTOM, x = 4, y = (screenHeight - 3), width = (screenWidth - 8), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_LEFT, x = 0, y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_RIGHT, x = (screenWidth - 3), y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/overlay_notch.png"), x = notchStartX, y = 0, width = 200, height = 12, alpha = opacity)

        // Scan info frame
        if (usageContext.focusTicks > 0) {
            blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_info_frame.png"),
                x = (screenWidth / 2) - 120,
                y = (screenHeight / 2) - 80,
                width = 92,
                height = 55,
                textureHeight = 550,
                textureWidth = 92,
                vOffset = usageContext.focusTicks * 55,
                alpha = opacity
            )

            if (usageContext.focusTicks == 9 && usageContext.pokemonInFocus != null) {
                drawScaledText(
                    context = graphics,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = usageContext.pokemonInFocus!!.pokemon.species.name.text().bold(),
                    x = (screenWidth / 2) - 74,
                    y = (screenHeight / 2) - 74,
                    shadow = true,
                    centered = true
                )
            }
        }


        val rotation = usageContext.usageTicks % 360

        // Scan rings
        matrices.push()
        matrices.translate((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), 0.0f)

        matrices.push()
        matrices.multiplyPositionMatrix(Quaternionf().rotateZ(Math.toRadians((-rotation) * 0.5).toFloat()) as Matrix4f?)
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_outer.png"), x = -58, y = -58, width = 116, height = 116, alpha = opacity)
        matrices.pop()

        for (i in 0 until 80) {
            val rotationQuaternion = Quaternionf().rotateZ(Math.toRadians((i * 4.5) + (rotation * 0.5)).toFloat())
            matrices.push()
            matrices.multiplyPositionMatrix(rotationQuaternion as Matrix4f?)
            blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_middle.png"), x = -50, y = -0.5F, width = 100, height = 1, alpha = opacity)
            matrices.pop()
        }

        matrices.push()
        matrices.multiplyPositionMatrix(Quaternionf().rotateZ(Math.toRadians(-usageContext.innerRingRotation.toDouble()).toFloat()) as Matrix4f?)
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_inner.png"), x = -42, y = -42, width = 84, height = 84, alpha = opacity)
        matrices.pop()

        matrices.pop()

        RenderSystem.disableBlend()
    }

    fun onRenderOverlay(graphics: DrawContext, tickCounter: RenderTickCounter) {
        if (CobblemonClient.pokedexUsageContext.scanningGuiOpen) {
            val tickDelta = tickCounter.tickDelta
            renderPhotodexOverlay(graphics, tickDelta, 1.0F)
            renderScanProgress(graphics, CobblemonClient.pokedexUsageContext.scanningProgress)
        }
    }

    companion object {
        val SCAN_SCREEN = cobblemonResource("textures/gui/pokedex/pokedex_screen_scan.png")
        val SCAN_OVERLAY_CORNERS = cobblemonResource("textures/gui/pokedex/scan/overlay_corners.png")
        val SCAN_OVERLAY_TOP = cobblemonResource("textures/gui/pokedex/scan/overlay_border_top.png")
        val SCAN_OVERLAY_BOTTOM = cobblemonResource("textures/gui/pokedex/scan/overlay_border_bottom.png")
        val SCAN_OVERLAY_LEFT = cobblemonResource("textures/gui/pokedex/scan/overlay_border_left.png")
        val SCAN_OVERLAY_RIGHT = cobblemonResource("textures/gui/pokedex/scan/overlay_border_right.png")
        val SCAN_OVERLAY_LINES = cobblemonResource("textures/gui/pokedex/scan/overlay_scanlines.png")

        fun getCompassTexture(direction: String): Identifier {
            return cobblemonResource("textures/gui/pokedex/compass/$direction.png")
        }
    }
}