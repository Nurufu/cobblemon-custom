/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.SentOutState
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.raycastSafeSendout
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.world.RaycastContext

object SendOutPokemonHandler : ServerNetworkPacketHandler<SendOutPokemonPacket> {

    const val THROW_DURATION = 0.5F
    const val SEND_OUT_DURATION = 1.5F

    override fun handle(packet: SendOutPokemonPacket, server: MinecraftServer, player: ServerPlayerEntity) {
        val slot = packet.slot.takeIf { it >= 0 } ?: return
        val party = Cobblemon.storage.getParty(player)
        val pokemon = party.get(slot) ?: return
        if (pokemon.isFainted()) {
            return
        }
        val state = pokemon.state
        if(player.vehicle != null && player.vehicle is RideablePokemonEntity) {
            val mount = player.vehicle as RideablePokemonEntity
            if (mount.isOwner(player) && mount.pokemon.state is SentOutState && mount.pokemon == party.get(slot)){
                player.playSound(CobblemonSounds.PC_DROP, SoundCategory.PLAYERS, 0.4F, 1F)
                player.dismountVehicle()
                return
            }
        }
        if (state is ShoulderedState || state !is ActivePokemonState) {
            val position = player.raycastSafeSendout(pokemon, 12.0, 5.0, RaycastContext.FluidHandling.ANY)

            if (position != null) {
                pokemon.sendOutWithAnimation(player, player.serverWorld, position)
            }
        } else {
            val entity = state.entity
            if (entity != null) {
                val buffer = THROW_DURATION + 0.5F - entity.ticksLived.toFloat() / 20F
                if(buffer > 0.75F){
                    //nothing
                }else if(buffer >0){
                    entity.after(buffer){entity.recallWithAnimation()}
                }else{
                    entity.recallWithAnimation()
                }
            } else {
                pokemon.recall()
            }
        }
    }
}