package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box

//Handles the actual raycasting to figure out what pokemon we are looking at
object PokemonScanner {
    //This basically draws a box around the casting entity, finds all entities in the box, then finds the one that a ray emanating from the player hits first
    fun detectEntity(castingEntity: Entity): Entity? {
        val eyePos = castingEntity.eyePos
        val lookVec = castingEntity.getRotationVec(1.0F)
        val maxDistance = RAY_LENGTH
        val boundingBoxSize = 12.0
        var closestEntity: Entity? = null
        var closestDistance = maxDistance

        // Define a large bounding box around the player
        val boundingBox = Box(
            castingEntity.x - boundingBoxSize, castingEntity.y - boundingBoxSize, castingEntity.z - boundingBoxSize,
            castingEntity.x + boundingBoxSize, castingEntity.y + boundingBoxSize, castingEntity.z + boundingBoxSize
        )

        // Get all entities within the boundingBox
        val entities = castingEntity.world.getEntitiesByClass(Entity::class.java, boundingBox) { it != castingEntity }

        for (entity in entities) {
            val entityBox: Box = entity.boundingBox

            // Calculate the size of the bounding box
            val boxWidth = entityBox.xLength
            val boxHeight = entityBox.yLength
            val boxDepth = entityBox.zLength

            val boxVolume = boxWidth * boxHeight * boxDepth

            val minSize = 0.2 // Smallest bounding box volume (joltik at .2)
            val maxSize = 3.0 // Largest bounding box volume (wailord at 21.5)
            val minSizeScale = 2.0 // Maximum inflation for getting closer to smallest hitbox
            val maxSizeScale = 1.0 // No inflation for getting closer to largest hitbox
            val steepCoefficient = 20.0

            // Normalize the volume within the defined range
            val normalizedSize = (boxVolume - minSize) / (maxSize - minSize).coerceAtLeast(0.01)

            // Calculate the scaling factor using very steep exponential decay to make smaller hitboxes bigger
            val inflationFactor = maxSizeScale + (minSizeScale - maxSizeScale) * Math.exp(-steepCoefficient * normalizedSize)

            // Inflate the base bounding box
            val inflatedBox = entityBox.expand(
                (inflationFactor - 1) * boxWidth / 2,
                (inflationFactor - 1) * boxHeight / 2,
                (inflationFactor - 1) * boxDepth / 2
            )

            val intersection = inflatedBox.raycast(eyePos, eyePos.add(lookVec.multiply(maxDistance)))

            if (intersection.isPresent) {
                val distanceToEntity = eyePos.distanceTo(intersection.get())
                if (distanceToEntity < closestDistance) {
                    closestEntity = entity
                    closestDistance = distanceToEntity
                }
            }
        }
        return closestEntity
    }

    fun findPokemon(castingEntity: Entity): PokemonEntity? {
        val targetedEntity = detectEntity(castingEntity)
        return targetedEntity as? PokemonEntity
    }

    fun isEntityInRange(castingEntity: Entity, targetEntity: Entity): Boolean {
        return targetEntity.pos.distanceTo(castingEntity.pos) <= RAY_LENGTH
    }

    val RAY_LENGTH = 10.0
}