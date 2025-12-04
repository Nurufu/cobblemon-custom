/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

/*
class MongoPlayerDataBackend<T : InstancedPlayerData>(
    mongoClient: MongoClient,
    databaseName: String,
    val typeToken: TypeToken<T>
) : PlayerDataStoreBackend<T> {

    companion object {
        val gson = GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(PlayerDataExtension::class.java, PlayerDataExtensionAdapter)
            .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
            .create()
    }

    private val collection = mongoClient.getDatabase(databaseName).getCollection("PlayerDataCollection")

    override fun load(uuid: UUID): T {
        val filter = Document("uuid", uuid.toString())
        val document = collection.find(filter).first()

        return if (document != null) {
            val jsonStr = document.toJson()
            gson.fromJson(jsonStr, typeToken).also {
                val newProps = it::class.memberProperties.filterIsInstance<KMutableProperty<*>>().filter { member -> member.getter.call(it) == null }
                if (newProps.isNotEmpty()) {
                    val defaultData = GeneralPlayerData.defaultData(uuid)
                    newProps.forEach { member -> member.setter.call(it, member.getter.call(defaultData)) }
                }
            }
        } else {
            InstancedPlayerData.getDefaultObjectForType(typeToken)
        }
    }

    override fun save(playerData: T) {
        val jsonStr = gson.toJson(generalPlayerData)
        val document = Document.parse(jsonStr)
        document.put("uuid", generalPlayerData.uuid.toString())
        val filter = Document("uuid", generalPlayerData.uuid.toString())

        collection.replaceOne(filter, document, ReplaceOptions().upsert(true))
    }
}

 */
