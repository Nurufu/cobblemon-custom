package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object DexEntries : JsonDataRegistry<DexEntry> {
    override val id = cobblemonResource("dex_entries")
    override val type = ResourceType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .create()

    override val typeToken: TypeToken<DexEntry> = TypeToken.get(DexEntry::class.java)
    override val resourcePath = "dex_entries"

    lateinit var entries: Map<Identifier, DexEntry>

    override fun reload(data: Map<Identifier, DexEntry>) {
        entries = data
    }

    override val observable = SimpleObservable<DexEntries>()
    override fun sync(player: ServerPlayerEntity) {

    }
}