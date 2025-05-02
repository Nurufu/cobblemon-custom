/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

class SpawnPokemonPacket(
    private val ownerId: UUID?,
    private val scaleModifier: Float,
    private val species: Species,
    private val form: FormData,
    private val aspects: Set<String>,
    private val battleId: UUID?,
    private val phasingTargetId: Int,
    private val beamMode: Byte,
    private val nickname: MutableText?,
    private val labelLevel: Int,
    private val poseType: PoseType,
    private val unbattlable: Boolean,
    private val hideLabel: Boolean,
    private val caughtBall: Identifier,
    private val spawnYaw: Float,
    private val friendship: Int,
    vanillaSpawnPacket: EntitySpawnS2CPacket
) : SpawnExtraDataEntityPacket<SpawnPokemonPacket, PokemonEntity>(vanillaSpawnPacket) {

    override val id: Identifier = ID

    constructor(entity: PokemonEntity, vanillaSpawnPacket: EntitySpawnS2CPacket) : this(
        entity.ownerUuid,
        entity.pokemon.scaleModifier,
        entity.exposedSpecies,
        entity.pokemon.form,
        entity.pokemon.aspects,
        entity.battleId,
        entity.phasingTargetId,
        entity.beamMode.toByte(),
        entity.pokemon.nickname,
        if (Cobblemon.config.displayEntityLevelLabel) entity.dataTracker.get(PokemonEntity.LABEL_LEVEL) else -1,
        entity.dataTracker.get(PokemonEntity.POSE_TYPE),
        entity.dataTracker.get(PokemonEntity.UNBATTLEABLE),
        entity.dataTracker.get(PokemonEntity.HIDE_LABEL),
        entity.pokemon.caughtBall.name,
        entity.dataTracker.get(PokemonEntity.SPAWN_DIRECTION),
        entity.dataTracker.get(PokemonEntity.FRIENDSHIP),
        vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: PacketByteBuf) {
        buffer.writeNullable(ownerId) { _, v -> buffer.writeUuid(v) }
        buffer.writeFloat(this.scaleModifier)
        buffer.writeIdentifier(this.species.resourceIdentifier)
        buffer.writeString(this.form.formOnlyShowdownId())
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeNullable(this.battleId) { pb, value -> pb.writeUuid(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamMode.toInt())
        buffer.writeNullable(this.nickname) { _, v -> buffer.writeText(v) }
        buffer.writeInt(this.labelLevel)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeBoolean(this.unbattlable)
        buffer.writeBoolean(this.hideLabel)
        buffer.writeIdentifier(this.caughtBall)
        buffer.writeFloat(this.spawnYaw)
        buffer.writeInt(this.friendship)
    }

    override fun applyData(entity: PokemonEntity) {
        entity.ownerUuid = ownerId
        entity.pokemon.apply {
            scaleModifier = this@SpawnPokemonPacket.scaleModifier
            species = this@SpawnPokemonPacket.species
            form = this@SpawnPokemonPacket.form
            forcedAspects = this@SpawnPokemonPacket.aspects
            nickname = this@SpawnPokemonPacket.nickname
            PokeBalls.getPokeBall(this@SpawnPokemonPacket.caughtBall)?.let { caughtBall = it }
        }
        entity.phasingTargetId = this.phasingTargetId
        entity.beamMode = this.beamMode.toInt()
        entity.battleId = this.battleId
        entity.dataTracker.set(PokemonEntity.LABEL_LEVEL, labelLevel)
        entity.dataTracker.set(PokemonEntity.SPECIES, entity.pokemon.species.resourceIdentifier.toString())
        entity.dataTracker.set(PokemonEntity.ASPECTS, aspects)
        entity.dataTracker.set(PokemonEntity.POSE_TYPE, poseType)
        entity.dataTracker.set(PokemonEntity.UNBATTLEABLE, unbattlable)
        entity.dataTracker.set(PokemonEntity.HIDE_LABEL, hideLabel)
        entity.dataTracker.set(PokemonEntity.SPAWN_DIRECTION, spawnYaw)
        entity.dataTracker.set(PokemonEntity.FRIENDSHIP, friendship)
    }

    override fun checkType(entity: Entity): Boolean = entity is PokemonEntity

    companion object {
        val ID = cobblemonResource("spawn_pokemon_entity")
        fun decode(buffer: PacketByteBuf): SpawnPokemonPacket {
            val ownerId = buffer.readNullable { buffer.readUuid() }
            val scaleModifier = buffer.readFloat()
            val species = PokemonSpecies.getByIdentifier(buffer.readIdentifier())!!
            val showdownId = buffer.readString()
            val form = species.forms.firstOrNull { it.formOnlyShowdownId() == showdownId } ?: species.standardForm
            val aspects = buffer.readList(PacketByteBuf::readString).toSet()
            val battleId = buffer.readNullable { buffer.readUuid() }
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val nickname = buffer.readNullable { buffer.readText().copy() }
            val labelLevel = buffer.readInt()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val unbattlable = buffer.readBoolean()
            val hideLabel = buffer.readBoolean()
            val caughtBall = buffer.readIdentifier()
            val spawnAngle = buffer.readFloat()
            val friendship = buffer.readInt()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnPokemonPacket(ownerId, scaleModifier, species, form, aspects, battleId, phasingTargetId, beamModeEmitter, nickname, labelLevel, poseType, unbattlable, hideLabel, caughtBall, spawnAngle, friendship, vanillaPacket)
        }
    }

}