package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.util.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.starliteheart.cobbleride.common.pokemon.RiderOffsetType
import java.util.*

class RideableSpecies : ClientDataSynchronizer<RideableSpecies>, ShowdownIdentifiable {
    var name: String = "Bulbasaur"
    var enabled: Boolean = true
    var offsets: EnumMap<RiderOffsetType, Vec3d> = EnumMap(RiderOffsetType::class.java)
    var shouldRiderSit: Boolean = true
    var baseSpeedModifier: Float = 1.0F
    var landSpeedModifier: Float = 1.0F
    var waterSpeedModifier: Float = 1.0F
    var airSpeedModifier: Float = 1.0F

    val forms: MutableList<RideableFormData> = mutableListOf()

    private val standardForm by lazy {
        RideableFormData(
            "Normal",
            enabled,
            offsets,
            shouldRiderSit,
            baseSpeedModifier,
            landSpeedModifier,
            waterSpeedModifier,
            airSpeedModifier
        ).initialize(this)
    }

    @Transient
    lateinit var identifier: Identifier

    fun initialize() {
        this.forms.forEach { it.initialize(this) }
        if (this.forms.isNotEmpty() && this.forms.none { it == this.standardForm }) {
            this.forms.add(0, this.standardForm)
        }
    }

    fun getForm(name: String): RideableFormData = forms.lastOrNull { it.name == name } ?: standardForm

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeIdentifier(this.identifier)
        buffer.writeString(this.name)
        buffer.writeBoolean(this.enabled)
        buffer.writeMap(this.offsets, { _, k -> buffer.writeEnumConstant(k) }, { _, v -> buffer.writeVec3d(v) })
        buffer.writeBoolean(this.shouldRiderSit)
        buffer.writeFloat(this.baseSpeedModifier)
        buffer.writeFloat(this.landSpeedModifier)
        buffer.writeFloat(this.waterSpeedModifier)
        buffer.writeFloat(this.airSpeedModifier)
        buffer.writeCollection(this.forms) { _, form -> form.encode(buffer) }
    }

    override fun decode(buffer: PacketByteBuf) {
        this.identifier = buffer.readIdentifier()
        this.name = buffer.readString()
        this.enabled = buffer.readBoolean()
        this.offsets.clear()
        this.offsets += buffer.readMap(
            { _ -> buffer.readEnumConstant(RiderOffsetType::class.java) },
            { _ -> buffer.readVec3d() })
        this.shouldRiderSit = buffer.readBoolean()
        this.baseSpeedModifier = buffer.readFloat()
        this.landSpeedModifier = buffer.readFloat()
        this.waterSpeedModifier = buffer.readFloat()
        this.airSpeedModifier = buffer.readFloat()
        this.forms.clear()
        this.forms += buffer.readList { RideableFormData().apply { decode(buffer) } }.filterNotNull().toMutableList()
        this.initialize()
    }

    override fun shouldSynchronize(other: RideableSpecies): Boolean {
        if (other.identifier.toString() != other.identifier.toString())
            return false
        return other.showdownId() != this.showdownId()
                || other.name != this.name
                || other.enabled != this.enabled
                || other.offsets != this.offsets
                || other.shouldRiderSit != this.shouldRiderSit
                || other.baseSpeedModifier != this.baseSpeedModifier
                || other.landSpeedModifier != this.landSpeedModifier
                || other.waterSpeedModifier != this.waterSpeedModifier
                || other.airSpeedModifier != this.airSpeedModifier
                || other.forms != this.forms
    }

    override fun showdownId(): String {
        val id = this.unformattedShowdownId()
        if (this.identifier.namespace == Cobblemon.MODID) {
            return id
        }
        return this.identifier.namespace + id
    }

    private fun unformattedShowdownId(): String = Regex("[^a-z0-9]+").replace(this.name.lowercase(), "")

    override fun toString() = this.showdownId()

}