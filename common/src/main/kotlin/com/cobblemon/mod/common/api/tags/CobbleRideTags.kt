package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object CobbleRideTags {
    @JvmField
    val NO_MOUNT_ITEMS = create("no_mount_items")

    @JvmField
    val NO_MOUNT_BATTLE_ITEMS = create("no_mount_battle_items")

    private fun create(path: String) = TagKey.of(RegistryKeys.ITEM, cobblemonResource(path))
}