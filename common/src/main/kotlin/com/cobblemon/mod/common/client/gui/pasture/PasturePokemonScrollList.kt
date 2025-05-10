/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pasture

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.pc.StorageSlot
import com.cobblemon.mod.common.client.gui.summary.widgets.PartySlotWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.server.pasture.UnpasturePokemonPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import org.joml.Quaternionf
import org.joml.Vector3f

class PasturePokemonScrollList(
    val x: Int,
    val y: Int,
    val parent: PastureWidget
) : AlwaysSelectedEntryListWidget<PasturePokemonScrollList.PastureSlot>(
    MinecraftClient.getInstance(),
    WIDTH, // width
    HEIGHT, // height
    0, // top
    HEIGHT, // bottom
    SLOT_HEIGHT + SLOT_SPACING
) {
    companion object {
        const val WIDTH = 70
        const val HEIGHT = 120
        const val SLOT_WIDTH = 62
        const val SLOT_HEIGHT = 25
        const val SLOT_SPACING = 3
        const val SCALE = 0.5F

        private val scrollOverlayResource = cobblemonResource("textures/gui/pasture/pasture_scroll_overlay.png")
        private val slotResource = cobblemonResource("textures/gui/pasture/pasture_slot.png")
    }

    private var scrolling = false

    override fun getRowWidth() = SLOT_WIDTH

    init {
        correctSize()
        setRenderHorizontalShadows(false)
        setRenderBackground(false)
        setRenderSelection(false)

        parent.pasturePCGUIConfiguration.pasturedPokemon.subscribeIncludingCurrent {
            val children = children()
            val newEntries = it.filter { pk -> children.none { it.pokemon.pokemonId == pk.pokemonId } }
            val removedEntries = children().filter { pk -> it.none { it.pokemonId == pk.pokemon.pokemonId } }

            removedEntries.forEach(this::removeEntry)
            newEntries.forEach { addEntry(PastureSlot(it, parent)) }
        }
    }

    override fun getScrollbarPositionX() = left + width - 3

    public override fun addEntry(entry: PastureSlot) = super.addEntry(entry)
    public override fun removeEntry(entry: PastureSlot) = super.removeEntry(entry)

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        correctSize()

        context.enableScissor(
            left,
            top + 1,
            left + width,
            top + 1 + height
        )

        super.render(context, mouseX, mouseY, partialTicks)
        context.disableScissor()

        // Scroll Overlay
        blitk(
            matrixStack = context.matrices,
            texture = scrollOverlayResource,
            x = left,
            y = top - 12,
            height = 131,
            width = WIDTH
        )

        val config = parent.pasturePCGUIConfiguration

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = "${children().count { it.isOwned() }}/${config.permissions.maxPokemon.takeIf { it >= 0 } ?: config.limit }".text().bold(),
            x = x + (WIDTH / 2),
            y = y - 9,
            centered = true
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        updateScrollingState(mouseX, mouseY)
        if (scrolling) {
            focused = getEntryAtPosition(mouseX, mouseY)
            isDragging = true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (scrolling) {
            if (mouseY < top) {
                scrollAmount = 0.0
            } else if (mouseY > bottom) {
                scrollAmount = maxScroll.toDouble()
            } else {
                scrollAmount += deltaY
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    private fun updateScrollingState(mouseX: Double, mouseY: Double) {
        scrolling = mouseX >= this.scrollbarPositionX.toDouble()
                && mouseX < (this.scrollbarPositionX + 3).toDouble()
                && mouseY >= top
                && mouseY < bottom
    }

    private fun correctSize() {
        updateSize(WIDTH, HEIGHT, y + 1, (y + 1) + (HEIGHT - 2))
        setLeftPos(x)
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + WIDTH)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + HEIGHT))

    class PastureSlot(val pokemon: OpenPasturePacket.PasturePokemonDataDTO, private val parent: PastureWidget) : Entry<PastureSlot>() {
        val client: MinecraftClient = MinecraftClient.getInstance()
        val state = FloatingState()

        fun isOwned() = client.player?.uuid == pokemon.playerId
        fun canUnpasture() = isOwned() || parent.pasturePCGUIConfiguration.permissions.canUnpastureOthers

        private val moveButton: PastureSlotIconButton = PastureSlotIconButton(
            xPos = 0,
            yPos = 0,
            onPress = {
                parent.storageWidget.pcGui.playSound(CobblemonSounds.PC_CLICK)
                UnpasturePokemonPacket(
                    pastureId = parent.pasturePCGUIConfiguration.pastureId,
                    pokemonId = pokemon.pokemonId
                ).sendToServer()
            }
        )

        override fun getNarration() = pokemon.displayName

        override fun render(
            context: DrawContext,
            index: Int,
            rowTop: Int,
            rowLeft: Int,
            rowWidth: Int,
            rowHeight: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTicks: Float
        ) {
            val x = rowLeft - 4
            val y = rowTop
            val matrixStack = context.matrices
            blitk(
                matrixStack = matrixStack,
                texture = slotResource,
                x = x,
                y = y,
                height = SLOT_HEIGHT,
                width = rowWidth,
                vOffset = if (isHovered) SLOT_HEIGHT else 0,
                textureHeight = SLOT_HEIGHT * 2
            )

            // Render Pokémon
            matrixStack.push()
            matrixStack.translate(x + 11 + (StorageSlot.SIZE / 2.0), y - 5.0, 0.0)
            matrixStack.scale(2.5F, 2.5F, 1F)
            drawProfilePokemon(
                species = pokemon.species,
                aspects = pokemon.aspects,
                matrixStack = matrixStack,
                rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
                state = state,
                partialTicks = partialTicks,
                scale = 4.5F
            )
            matrixStack.pop()

            val heldItem = pokemon.heldItem
            if (!heldItem.isEmpty) {
                renderScaledGuiItemIcon(
                    itemStack = heldItem,
                    x = x + 23.5,
                    y = y + 9.0,
                    scale = 0.5,
                    matrixStack = matrixStack
                )
            }

            drawScaledText(
                context = context,
                text = lang("ui.lv.number", pokemon.level),
                x = x + 46,
                y = y + 13,
                shadow = true,
                scale = SCALE
            )

            drawScaledText(
                context = context,
                text = pokemon.displayName.copy(),
                x = x + 11,
                y = y + 20,
                maxCharacterWidth = 90,
                scale = SCALE
            )

            if ("male" in pokemon.aspects || "female" in pokemon.aspects) {
                blitk(
                    matrixStack = matrixStack,
                    texture = if ("male" in pokemon.aspects) PartySlotWidget.genderIconMale else PartySlotWidget.genderIconFemale,
                    x = (x + 56.5) / SCALE,
                    y = (y + 20) / SCALE,
                    height = 7,
                    width = 5,
                    scale = SCALE
                )
            }

            if (canUnpasture()) {
                moveButton.setPos(x + 2, y + 9)
                moveButton.render(context, mouseX, mouseY, partialTicks)
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, delta: Int): Boolean {
            if (moveButton.isHovered(mouseX, mouseY) && canUnpasture()) {
                moveButton.onPress()
                return true
            }
            return false
        }
    }
}