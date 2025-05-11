/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.generic

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnGenericBedrockPacket
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.world.World

class GenericBedrockEntity(world: World) : Entity(CobblemonEntities.GENERIC_BEDROCK_ENTITY, world), PosableEntity, Schedulable {
    companion object {
        val CATEGORY = DataTracker.registerData(GenericBedrockEntity::class.java, IdentifierDataSerializer)
        val ASPECTS = DataTracker.registerData(GenericBedrockEntity::class.java, StringSetDataSerializer)
        val POSE_TYPE = DataTracker.registerData(GenericBedrockEntity::class.java, PoseTypeDataSerializer)
        val SCALE = DataTracker.registerData(GenericBedrockEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }

    var savesToWorld = false
    override val schedulingTracker = SchedulingTracker()
    override val delegate = if (world.isClient) {
        // Don't import because scanning for imports is a CI job we'll do later to detect errant access to client from server
        com.cobblemon.mod.common.client.entity.GenericBedrockClientDelegate()
    } else {
        GenericBedrockServerDelegate()
    }

    override val struct: QueryStruct = QueryStruct(hashMapOf())

    var category: Identifier
        get() = this.dataTracker.get(CATEGORY)
        set(value) {
            this.dataTracker.set(CATEGORY, value)
        }

    var aspects: Set<String>
        get() = this.dataTracker.get(ASPECTS)
        set(value) {
            this.dataTracker.set(ASPECTS, value)
        }

    var scale: Float
        get() = this.dataTracker.get(SCALE)
        set(value) {
            this.dataTracker.set(SCALE, value)
        }

    var colliderWidth = 1F
        set(value) {
            super.getWidth()
            field = value
            calculateDimensions()
        }

    var colliderHeight = 1F
        set(value) {
            field = value
            calculateDimensions()
        }

    var syncAge = false

    init {
        addPosableFunctions(struct)
    }

    override fun initDataTracker() {
        this.dataTracker.startTracking(CATEGORY, cobblemonResource("generic"))
        this.dataTracker.startTracking(ASPECTS, emptySet())
        this.dataTracker.startTracking(SCALE, 1F)
        this.dataTracker.startTracking(POSE_TYPE, PoseType.NONE)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        this.category = Identifier(nbt.getString(DataKeys.GENERIC_BEDROCK_CATEGORY))
        this.aspects = nbt.getList(DataKeys.GENERIC_BEDROCK_ASPECTS, NbtString.STRING_TYPE.toInt()).map { it.asString() }.toSet()
        this.dataTracker.set(POSE_TYPE, PoseType.values()[nbt.getByte(DataKeys.GENERIC_BEDROCK_POSE_TYPE).toInt()])
        this.scale = nbt.getFloat(DataKeys.GENERIC_BEDROCK_SCALE)
        this.colliderWidth = nbt.getFloat(DataKeys.GENERIC_BEDROCK_COLLIDER_WIDTH)
        this.colliderHeight = nbt.getFloat(DataKeys.GENERIC_BEDROCK_COLLIDER_HEIGHT)
        this.syncAge = nbt.getBoolean(DataKeys.GENERIC_BEDROCK_SYNC_AGE)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putString(DataKeys.GENERIC_BEDROCK_CATEGORY, category.toString())
        nbt.put(DataKeys.GENERIC_BEDROCK_ASPECTS, NbtList().also { it.addAll(aspects.map(NbtString::of)) })
        nbt.putByte(DataKeys.GENERIC_BEDROCK_POSE_TYPE, getCurrentPoseType().ordinal.toByte())
        nbt.putFloat(DataKeys.GENERIC_BEDROCK_SCALE, scale)
        nbt.putFloat(DataKeys.GENERIC_BEDROCK_COLLIDER_WIDTH, colliderWidth)
        nbt.putFloat(DataKeys.GENERIC_BEDROCK_COLLIDER_HEIGHT, colliderHeight)
        nbt.putBoolean(DataKeys.GENERIC_BEDROCK_SYNC_AGE, syncAge)
    }

    override fun canHit() = true
    override fun isCollidable() = true

    override fun shouldSave() = super.shouldSave() && this.savesToWorld
    override fun getDimensions(pose: EntityPose) = EntityDimensions.changing(colliderWidth, colliderHeight).scaled(scale)
    override fun getCurrentPoseType(): PoseType = this.dataTracker.get(POSE_TYPE)

    override fun createSpawnPacket() = CobblemonNetwork.asVanillaClientBound(
        SpawnGenericBedrockPacket(
            category = category,
            aspects = aspects,
            poseType = getCurrentPoseType(),
            scale = scale,
            width = colliderWidth,
            height = colliderHeight,
            startAge = if (syncAge) age else 0,
            vanillaSpawnPacket = super.createSpawnPacket() as EntitySpawnS2CPacket
        )
    )

    override fun tick() {
        super.tick()
        delegate.tick(this)
        schedulingTracker.update(1/20F)
    }
}