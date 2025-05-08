package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.RideablePokemonSpecies
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import com.cobblemon.mod.common.pokemon.RideableSpecies
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.rideableResource

class RideableSpeciesRegistrySyncPacket(species: Collection<RideableSpecies>) :
    DataRegistrySyncPacket<RideableSpecies, RideableSpeciesRegistrySyncPacket>(species) {
    override val id = ID

//    // Manually redeclared here, to bypass internal check from external module
//    override fun decodeBuffer(buffer: PacketByteBuf) {
//        val size = buffer.readInt()
//        val newBuffer = PacketByteBuf(buffer.readBytes(size))
//        this.buffer = newBuffer
//    }

    override fun encodeEntry(buffer: PacketByteBuf, entry: RideableSpecies) {
        try {
            buffer.writeIdentifier(entry.identifier)
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the rideable species {}", entry.identifier, e)
        }
    }

    override fun decodeEntry(buffer: PacketByteBuf): RideableSpecies? {
        val identifier = buffer.readIdentifier()
        val species = RideableSpecies()
        species.identifier = identifier
        return try {
            species.decode(buffer)
            species
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding the species {}", identifier, e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<RideableSpecies>) {
        RideablePokemonSpecies.reload(entries.associateBy { it.identifier })
    }

    companion object {
        val ID: Identifier = cobblemonResource("species_sync")

        @JvmStatic
        fun decode(buffer: PacketByteBuf): RideableSpeciesRegistrySyncPacket =
            RideableSpeciesRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}