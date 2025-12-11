package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.api.net.Encodable
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

abstract class PokedexVariation : Encodable {
    abstract val type: Identifier

    companion object {
        fun decodeAll(buf: PacketByteBuf): PokedexVariation {
            val typeId = buf.readIdentifier()
            val result = PokedexVariationTypes.getById(typeId)?.decoder?.invoke(buf)
                ?: throw RuntimeException("Unknown dex data type: $typeId")
            return result
        }
    }

}