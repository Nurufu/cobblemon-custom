package com.cobblemon.mod.common.api.pokedex.entry

import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

enum class PokedexVariationTypes(
    val typeId: Identifier,
    val decoder: (PacketByteBuf) -> PokedexVariation,
    val type: KClass<out PokedexVariation>
) {
    FORM(BasicPokedexVariation.ID, BasicPokedexVariation.Companion::decode, BasicPokedexVariation::class);

    companion object {
        fun getById(id: Identifier): PokedexVariationTypes? {
            return entries.filter {
                it.typeId == id
            }.firstOrNull()
        }
    }
}