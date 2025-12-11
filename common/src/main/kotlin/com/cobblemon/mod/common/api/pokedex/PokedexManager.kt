package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier
import java.util.*

class PokedexManager(
    override val uuid: UUID,
    override val speciesRecords: MutableMap<Identifier, SpeciesDexRecord>
) : AbstractPokedexManager(), InstancedPlayerData {

    fun encounter(pokemon: Pokemon) {
        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.formOnlyShowdownId()
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).encountered(pokemon)
    }

    fun catch(pokemon: Pokemon){
        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.formOnlyShowdownId()
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).encountered(pokemon)
    }

    override fun markDirty() {
    }

    override fun initialize() {
        speciesRecords.entries.forEach { (key, value) -> value.initialize(this, key) }
    }


    override fun onSpeciesRecordUpdated(speciesDexRecord: SpeciesDexRecord) {
        uuid.getPlayer()?.sendPacket(
            SetClientPlayerDataPacket(
                type = PlayerInstancedDataStoreType.POKEDEX,
                playerData = ClientPokedexManager(mutableMapOf(speciesDexRecord.id to speciesDexRecord)),
                isIncremental = true
            )
        )
    }



    companion object {
        val CODEC = RecordCodecBuilder.create<PokedexManager> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(Identifier.CODEC, SpeciesDexRecord.CODEC).fieldOf("speciesRecords").forGetter { it.speciesRecords }
            ).apply(instance) { uuid, map ->
                PokedexManager(UUID.fromString(uuid), map)
            }
        }
    }

    override fun toClientData() = ClientPokedexManager(speciesRecords)
}