package com.cobblemon.mod.common.api.events.storage

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.storage.pc.PCBox
import net.minecraft.server.network.ServerPlayerEntity

interface SwapPCBoxEvent {

    val player: ServerPlayerEntity

    val box: Int

    val box2: Int
    class Pre(
        override val player: ServerPlayerEntity,
        override val box: Int,
        override val box2: Int
    ) : SwapPCBoxEvent, Cancelable()

    class Post(
        override val player: ServerPlayerEntity,
        override val box: Int,
        override val box2: Int
    ) : SwapPCBoxEvent

}
