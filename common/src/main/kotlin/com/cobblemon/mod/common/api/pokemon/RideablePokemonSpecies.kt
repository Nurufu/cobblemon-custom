package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.pokemon.RideableSpecies
import com.cobblemon.mod.common.util.adapters.VerboseVec3dAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import com.cobblemon.mod.common.net.messages.client.data.RideableSpeciesRegistrySyncPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.rideableResource

object RideablePokemonSpecies : JsonDataRegistry<RideableSpecies> {
    override val id: Identifier = cobblemonResource("rideable_species")
    override val type: ResourceType = ResourceType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapter(Vec3d::class.java, VerboseVec3dAdapter)
        .create()

    override val typeToken: TypeToken<RideableSpecies> = TypeToken.get(RideableSpecies::class.java)

    override val resourcePath: String = "rideable_species"

    override val observable = SimpleObservable<RideablePokemonSpecies>()

    private val speciesByIdentifier = hashMapOf<Identifier, RideableSpecies>()

    private val species: Collection<RideableSpecies>
        get() = speciesByIdentifier.values

    init {
        PokemonSpecies.observable.subscribe {
            species.forEach(RideableSpecies::initialize)
        }
    }

    fun getByIdentifier(identifier: Identifier) = speciesByIdentifier[identifier]

    fun getByName(name: String) = getByIdentifier(rideableResource(name))

    override fun reload(data: Map<Identifier, RideableSpecies>) {
        speciesByIdentifier.clear()
        data.forEach { (identifier, species) ->
            species.identifier = identifier
            speciesByIdentifier[identifier] = species
        }

        Cobblemon.LOGGER.info("Loaded {} Pokémon species with ride details", species.size)
        observable.emit(this)
    }

    override fun sync(player: ServerPlayerEntity) {
        RideableSpeciesRegistrySyncPacket(species.toList()).sendToPlayer(player)
    }
}