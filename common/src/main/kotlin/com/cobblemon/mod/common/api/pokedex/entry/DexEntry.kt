package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.api.dex.entry.ExtraDexData
import net.minecraft.util.Identifier

class DexEntry(
    val registryId: Identifier,
    val entryId: Identifier,
    val extraData: List<ExtraDexData>
) {

}