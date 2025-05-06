package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.mixin.accessor.SpawnExtraDataEntityPacketAccessor
import com.cobblemon.mod.common.mixin.invoker.ClientPlayNetworkHandlerInvoker
import com.cobblemon.mod.common.net.messages.client.pokemon.ai.ClientMoveBehaviour
import com.cobblemon.mod.common.pokemon.Gender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.network.NetworkThreadUtils
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import com.cobblemon.mod.common.entity.pokemon.RideablePokemonEntity
import net.starliteheart.cobbleride.common.util.rideableResource
import java.util.*

class SpawnRidePokemonPacket(
    private val ownerId: UUID?,
    private val scaleModifier: Float,
    private val speciesId: Identifier,
    private val gender: Gender,
    private val shiny: Boolean,
    private val formName: String,
    private val aspects: Set<String>,
    private val moveBehaviour: ClientMoveBehaviour,
    private val battleId: UUID?,
    private val phasingTargetId: Int,
    private val beamMode: Byte,
    private val nickname: MutableText?,
    private val labelLevel: Int,
    private val poseType: PoseType,
    private val unbattleable: Boolean,
    private val hideLabel: Boolean,
    private val caughtBall: Identifier,
    private val spawnYaw: Float,
    private val friendship: Int,
    vanillaSpawnPacket: EntitySpawnS2CPacket
) : SpawnExtraDataEntityPacket<SpawnRidePokemonPacket, RideablePokemonEntity>(vanillaSpawnPacket) {

    override val id: Identifier = ID

    constructor(entity: RideablePokemonEntity, vanillaSpawnPacket: EntitySpawnS2CPacket) : this(
        entity.ownerUuid,
        entity.pokemon.scaleModifier,
        entity.exposedSpecies.resourceIdentifier,
        entity.pokemon.gender,
        entity.pokemon.shiny,
        entity.exposedForm.formOnlyShowdownId(),
        entity.aspects,
        ClientMoveBehaviour(entity.exposedForm.behaviour.moving),
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
        buffer.writeIdentifier(this.speciesId)
        buffer.writeEnumConstant(this.gender)
        buffer.writeBoolean(this.shiny)
        buffer.writeString(this.formName)
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        moveBehaviour.encode(buffer)
        buffer.writeNullable(this.battleId) { pb, value -> pb.writeUuid(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamMode.toInt())
        buffer.writeNullable(this.nickname) { _, v -> buffer.writeText(v) }
        buffer.writeInt(this.labelLevel)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeBoolean(this.unbattleable)
        buffer.writeBoolean(this.hideLabel)
        buffer.writeIdentifier(this.caughtBall)
        buffer.writeFloat(this.spawnYaw)
        buffer.writeInt(this.friendship)
    }

    override fun applyData(entity: RideablePokemonEntity) {
        entity.ownerUuid = ownerId
        entity.pokemon.apply {
            scaleModifier = this@SpawnRidePokemonPacket.scaleModifier
            species = this@SpawnRidePokemonPacket.speciesId.let {
                PokemonSpecies.getByIdentifier(it) ?: PokemonSpecies.random()
            }
            gender = this@SpawnRidePokemonPacket.gender
            shiny = this@SpawnRidePokemonPacket.shiny
            form =
                this@SpawnRidePokemonPacket.formName.let { formName -> species.forms.find { it.formOnlyShowdownId() == formName } }
                    ?: species.standardForm
            aspects = this@SpawnRidePokemonPacket.aspects
            nickname = this@SpawnRidePokemonPacket.nickname
            PokeBalls.getPokeBall(this@SpawnRidePokemonPacket.caughtBall)?.let { caughtBall = it }
        }
        entity.moveBehaviour = this.moveBehaviour
        entity.phasingTargetId = this.phasingTargetId
        entity.beamMode = this.beamMode.toInt()
        entity.battleId = this.battleId
        entity.dataTracker.set(PokemonEntity.LABEL_LEVEL, labelLevel)
        entity.dataTracker.set(PokemonEntity.SPECIES, entity.pokemon.species.resourceIdentifier.toString())
        entity.dataTracker.set(PokemonEntity.ASPECTS, aspects)
        entity.dataTracker.set(PokemonEntity.POSE_TYPE, poseType)
        entity.dataTracker.set(PokemonEntity.UNBATTLEABLE, unbattleable)
        entity.dataTracker.set(PokemonEntity.HIDE_LABEL, hideLabel)
        entity.dataTracker.set(PokemonEntity.SPAWN_DIRECTION, spawnYaw)
        entity.dataTracker.set(PokemonEntity.FRIENDSHIP, friendship)
    }

    override fun checkType(entity: Entity): Boolean = entity is RideablePokemonEntity

//    @Suppress("CAST_NEVER_SUCCEEDS")
    fun spawnRidePokemonAndApply(client: MinecraftClient) {
        client.execute {
            val player = client.player ?: return@execute
            val world = player.world as? ClientWorld ?: return@execute
            val vanillaSpawnPacket = (this as SpawnExtraDataEntityPacketAccessor).vanillaSpawnPacket
            NetworkThreadUtils.forceMainThread(vanillaSpawnPacket, player.networkHandler, client)
            val entity = RideablePokemonEntity(world)
            entity.onSpawnPacket(vanillaSpawnPacket)
            entity.velocity = Vec3d(
                vanillaSpawnPacket.x,
                vanillaSpawnPacket.y,
                vanillaSpawnPacket.z
            )
            // Cobblemon start
            if (this.checkType(entity)) {
                this.applyData(entity)
            }
            // Cobblemon end
            world.addEntity(0, entity)
            (player.networkHandler as ClientPlayNetworkHandlerInvoker).callPlaySpawnSound(entity)
        }

    }

    companion object {
        val ID = rideableResource("spawn_ride_pokemon_entity")
        fun decode(buffer: PacketByteBuf): SpawnRidePokemonPacket {
            val ownerId = buffer.readNullable { buffer.readUuid() }
            val scaleModifier = buffer.readFloat()
            val speciesId = buffer.readIdentifier()
            val gender = buffer.readEnumConstant(Gender::class.java)
            val shiny = buffer.readBoolean()
            val formName = buffer.readString()
            val aspects = buffer.readList { it.readString() }.toSet()
            val moveBehaviour = ClientMoveBehaviour.decode(buffer)
            val battleId = buffer.readNullable { buffer.readUuid() }
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val nickname = buffer.readNullable { buffer.readText().copy() }
            val labelLevel = buffer.readInt()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val unbattleable = buffer.readBoolean()
            val hideLabel = buffer.readBoolean()
            val caughtBall = buffer.readIdentifier()
            val spawnAngle = buffer.readFloat()
            val friendship = buffer.readInt()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnRidePokemonPacket(
                ownerId,
                scaleModifier,
                speciesId,
                gender,
                shiny,
                formName,
                aspects,
                moveBehaviour,
                battleId,
                phasingTargetId,
                beamModeEmitter,
                nickname,
                labelLevel,
                poseType,
                unbattleable,
                hideLabel,
                caughtBall,
                spawnAngle,
                friendship,
                vanillaPacket
            )
        }
    }
}