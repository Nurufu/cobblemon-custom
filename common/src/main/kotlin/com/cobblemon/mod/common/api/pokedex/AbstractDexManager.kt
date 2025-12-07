package com.cobblemon.mod.common.api.pokedex

import net.minecraft.util.Identifier

abstract class AbstractDexManager() {
    abstract val entries: Map<String, String>

    fun getValueForKey(key: String): String? {
        return entries[key]
    }

    fun getKnowledgeForSpecies(speciesId: Identifier): PokedexEntryProgress {
        return getValueForKey(getKnowledgeKeyForSpecies(speciesId))?.let {
            PokedexEntryProgress.valueOf(it)
        } ?: PokedexEntryProgress.NONE
    }

    companion object {
        const val NUM_CAUGHT_KEY = "cobblemon.pokedex.entries.caught"
        const val NUM_SEEN_KEY = "cobblemon.pokedex.entries.seen"

        fun getKeyForSpeciesBase(speciesId: Identifier): String {
            return "cobblemon.pokedex.${speciesId}"
        }

        fun getKnowledgeKeyForSpecies(speciesId: Identifier): String {
            return "${getKeyForSpeciesBase(speciesId)}.knowledge"
        }

        fun getCaptureMethodKeyForSpecies(speciesId: Identifier): String {
            return "${getKeyForSpeciesBase(speciesId)}.capture"
        }
    }
}