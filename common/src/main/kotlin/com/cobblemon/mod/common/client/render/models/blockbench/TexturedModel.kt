/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.util.adapters.LocatorBoneAdapter
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.minecraft.client.model.Dilation
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelPartData
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.util.math.MatrixStack
/**
 * Bedrock's GEO format.
 *
 * @author Chris Fusco
 * @since May 10th, 2022
 */
class TexturedModel {
    @SerializedName("format_version")
    val formatVersion: String = "0"
    @SerializedName("minecraft:geometry")
    val geometry: List<ModelGeometry>? = null

    fun create(isForLivingEntityRenderer: Boolean) : TexturedModelData {
        return createWithUvOverride(isForLivingEntityRenderer, 0, 0, null, null)
    }

    fun resolveParentsFromRoot(boneMap: MutableMap<String, ModelBone>, bone: ModelBone): Set<ModelBone> {
        return if (bone.parent == null) {
            emptySet()
        } else {
            val parent = boneMap[bone.parent] ?: return emptySet()
            resolveParentsFromRoot(boneMap, parent) + bone
        }
    }

    /**
     * The core idea here is that Bedrock GEO models have a particular set of rules, and Flywheel has its own
     * set of rules. In Bedrock, there are bones, and those are used to apply cumulative rotations, and rotations
     * alone. This can make cubes move if you rotate around a distant pivot point, but modifying pivots alone is
     * not going to change cubes.
     *
     * If a bone has a crazy far away pivot but no rotation, it has no effect on the cubes. This is different to Java
     * Edition's [net.minecraft.client.model.ModelPart] models, which have 'parts', and the position of those parts
     * has a cumulative position impact on the children rather than just a rotational one.
     *
     * Flywheel, on the other hand, has none of these. There is no hierarchy, and rotations are always around the
     * origin. This is very like the Java Edition baked model format that's used for blocks, but we're not converting
     * a block model, are we?
     *
     * The major issue is that our GEO cumulative rotations must be converted to be done in a single rotation about
     * (0,0,0). We need both the orientation and the position to be the same. The way this has been accomplished is to:
     * - use [MatrixStack]s to calculate the correct final-destination for each cube start position,
     * - convert the matrix we used to a rotation about zero by breaking it into euler angles
     * - note that this euler rotation is correct on orientation but wrong on final position, and is how flywheel does it.
     * - invert the euler rotation
     * - apply the inverted rotation to the correct final-destination to find the correct pre-destination
     *
     * - Hiro & Apion
     */
//    fun createFlywheelModel(atlas: SpriteAtlasHolder, textureName: Identifier, name: String): Model {
//        val texture = atlas.getSprite(textureName)
//        val width = ((texture.maxU * atlas.atlas.width.toFloat()) - (texture.minU * atlas.atlas.width)).toInt()
//        val height =( (texture.maxV * atlas.atlas.height.toFloat()) - (texture.minV * atlas.atlas.height)).toInt()
//
//        val modelBuilder = PartBuilder(name, width, height)
//        modelBuilder.sprite(texture)
//        val boneMap = mutableMapOf<String, ModelBone>()
//        geometry?.forEach { it.bones?.forEach { boneMap[it.name] = it } }
//
//        geometry?.forEach {
//            it.bones?.forEach { bone ->
//                // This is meant to prepare the rotation stack so that the rotation around a cube's pivot point is respecting
//                // all of the parent bone rotations. It's not meant to influence the POSITION of the cubes further down
//                // the chain, only the location of the joints. It doesn't sum the pivot points either; the child bones
//                // are only affected by the parent bones because of the rotations.
//                //
//                // This probably doesn't work but haven't tested it on models with joints so.
//                val stack = MatrixStack()
//                for (bone in resolveParentsFromRoot(mutableMapOf(), bone)) {
//                    val rotation = bone.rotation?.takeIf { it[0] != 0F || it[1] != 0F || it[2] != 0F } ?: continue
//                    stack.translate(-bone.pivot[0], bone.pivot[1], -bone.pivot[2])
//                    stack.multiply(Quaternionf().rotationXYZ(rotation[0].toRadians(), rotation[1].toRadians(), rotation[2].toRadians()))
//                    stack.translate(bone.pivot[0], -bone.pivot[1], bone.pivot[2])
//                }
//
//                bone.cubes?.forEach { cube ->
//                    val size = cube.size?.let { Vector3f(it[0], it[1], it[2]) } ?: Vector3f()
//                    val rotation = cube.rotation?.let { Vector3f(it[0], it[1], it[2]) } ?: Vector3f()
//                    val inflation = (cube.inflate ?: 0F) / 2
//                    val uvs = cube.uv?.let { Vector2i(it[0], it[1]) } ?: Vector2i()
//
//                    /*
//                     * The origin has the X and Z flipped, and that also means counting from the opposite side of the
//                     * cube (second line is accomplishing that part). The reason for this is because Minecraft Java Edition
//                     * is a "right hand" coordinate system (finger guns time!!) whereas Bedrock presumably is left-hand.
//                     *
//                     * Pivots are also inverted fyi.
//                     *
//                     * - Hiro
//                     */
//                    val origin = cube.origin?.let { Vector3f(-it[0], it[1], -it[2]) } ?: Vector3f()
//                    origin.sub(size.get(Vector3f()).mul(1F, 0F, 1F))
//                    val pivot = cube.pivot?.let { Vector3f(-it[0], it[1], -it[2]) } ?: Vector3f()
//
//                    // Apply translate, rotate, then translate back because the pivots don't actually directly translate
//                    // to shifted positions in Bedrock.
//                    stack.push()
//                    stack.translate(pivot.x, pivot.y, pivot.z)
//                    stack.multiply(Quaternionf().rotationXYZ(rotation.x.toRadians(), rotation.y.toRadians(), rotation.z.toRadians()))
//                    stack.translate(-pivot.x, -pivot.y, -pivot.z)
//                    val rotationMatrix = stack.peek().positionMatrix
//                    stack.pop()
//
//                    // Finds where Bedrock put the start of the cube. The matrix does the rotation correctly because we
//                    // are chasing rotations around very specific points.
//                    val desiredPoint = rotationMatrix.transformPosition(origin.get(Vector3f()))
//
//                    // The rotation in Euler angles (which are about 0,0,0), that's what Flywheel likes to eat.
//                    // It will orient it correctly, but a limitation of Euler angles is that it can't get the thing
//                    // to rotate around a specific point, only ever the origin. We'll fix that.
//                    val eulerRotation = rotationMatrix.getEulerAnglesXYZ(Vector3f())
//                    // The reversed form of the rotation Flywheel is about to apply.
//                    val reversedRotation = Quaternionf().fromEulerXYZ(-eulerRotation.x, -eulerRotation.y, -eulerRotation.z)
//                    // Figure out the start point such that Flywheel applying the eulerRotation will put it at the desired point.
//                    val correctStart = reversedRotation.transform(desiredPoint.get(Vector3f()))
//
//                    // Now just put it all together. Inflation is a slight tweak to size and start position.
//                    // Invert YZ is something I don't understand but is necessary.
//                    modelBuilder.cuboid()
//                        .sprite(texture)
//                        .start(correctStart.x - inflation, correctStart.y - inflation, correctStart.z - inflation)
//                        .invertYZ()
//                        .size(size.x + 2 * inflation, size.y + 2 * inflation, size.z + 2 * inflation)
//                        .rotate(eulerRotation.x, eulerRotation.y, eulerRotation.z)
//                        .textureOffset(uvs.x, uvs.y)
//                        .endCuboid()
//                }
//            }
//        }
//        return modelBuilder.build()
//    }

    fun createWithUvOverride(isForLivingEntityRenderer: Boolean, u: Int, v: Int, textureWidth: Int?, textureHeight: Int?) : TexturedModelData {
        val modelData = ModelData()
        val parts = HashMap<String, ModelPartData>()
        val bones = HashMap<String, ModelBone>()

        try {
            val geometry = this.geometry!![0]
            val geometryBones = geometry.bones!!.toMutableList()
            var parentPart: ModelPartData

            // We want all the regular bones, but then we want locators to be mapped into being empty bones.
            // Reasoning there is that in many ways they really do function the same. Creating a bespoke locator
            // thing in ModelPart and holding onto it through runtime would require 500 mixins and 1000 virgin
            // sacrifices, so no thanks. This actually works startlingly well and allows us to follow DRY.
            // - Hiro
            geometryBones += geometryBones.mapNotNull { bone ->
                val locators = bone.locators ?: return@mapNotNull null
                locators.map { (name, locator) ->
                    val locatorBone = ModelBone()
                    locatorBone.name = LocatorAccess.PREFIX + name
                    locatorBone.parent = bone.name
                    locatorBone.pivot = locator.offset
                    locatorBone.rotation = locator.rotation
                    return@map locatorBone
                }
            }.flatten() + ModelBone().apply {
                this.name = LocatorAccess.PREFIX + "root"
            }

            for (bone in geometryBones) {
                bones[bone.name] = bone
                parentPart = if (bone.parent != null) parts[bone.parent]!! else modelData.root

                val boneRotation = bone.rotation
                val modelTransform : ModelTransform
                when {
                    bone.parent == null -> {
                        // The root part always has a 24 Y offset. One of life's great mysteries.
                        modelTransform = ModelTransform.pivot(0F, if (isForLivingEntityRenderer) 24F else 0F, 0F)
                    }
                    boneRotation != null -> {
                        modelTransform = ModelTransform.of(
                            -(bones[bone.parent]!!.pivot[0] - bone.pivot[0]),
                            bones[bone.parent]!!.pivot[1] - bone.pivot[1],
                            -(bones[bone.parent]!!.pivot[2] - bone.pivot[2]),
                            Math.toRadians(boneRotation[0].toDouble()).toFloat(),
                            Math.toRadians(boneRotation[1].toDouble()).toFloat(),
                            Math.toRadians(boneRotation[2].toDouble()).toFloat()
                        )
                    }
                    else -> {
                        modelTransform = ModelTransform.pivot(
                            -(bones[bone.parent]!!.pivot[0] - bone.pivot[0]),
                            bones[bone.parent]!!.pivot[1] - bone.pivot[1],
                            -(bones[bone.parent]!!.pivot[2] - bone.pivot[2])
                        )
                    }
                }

                val modelPart = ModelPartBuilder.create()
                val subParts = mutableListOf<ModelPartBuilder>()
                val modelTransforms = mutableListOf<ModelTransform>()

                val boneCubes = bone.cubes
                if (boneCubes != null) {
                    var pivot: List<Float>
                    var subPart: ModelPartBuilder

                    for (cube in boneCubes) {
                        subPart = if (cube.rotation != null) ModelPartBuilder.create() else modelPart
                        pivot = cube.pivot ?: bone.pivot

                        if (cube.uv != null) {
                            subPart.uv(
                                cube.uv[0] + u,
                                cube.uv[1] + v
                            )
                        }
                        if (cube.mirror != null && cube.mirror == true) {
                            subPart.mirrored()
                        }
                        if (cube.size != null && cube.origin != null) {
                            subPart.cuboid(
                                cube.origin[0] - pivot[0],
                                // Y is inverted in Java Edition, but that also means counting from the other side of the cube.
                                -(cube.origin[1] - pivot[1] + cube.size[1]),
                                cube.origin[2] - pivot[2],
                                cube.size[0],
                                cube.size[1],
                                cube.size[2],
                                Dilation(cube.inflate ?: 0f)
                            )
                        }
                        if (cube.mirror != null && cube.mirror == true) {
                            subPart.mirrored(false)
                        }

                        if (subPart != modelPart) {
                            modelTransforms.add(ModelTransform.of(
                                -(bone.pivot[0] - cube.pivot!![0]),
                                bone.pivot[1] - cube.pivot[1],
                                -(bone.pivot[2] - cube.pivot[2]),
                                Math.toRadians(cube.rotation!![0].toDouble()).toFloat(),
                                Math.toRadians(cube.rotation[1].toDouble()).toFloat(),
                                Math.toRadians(cube.rotation[2].toDouble()).toFloat()
                            ))
                            subParts.add(subPart)
                        }
                    }
                }

                parts[bone.name] = parentPart.addChild(
                    bone.name,
                    modelPart,
                    modelTransform
                )

                var counter = 0
                subParts.forEachIndexed { index, part ->
                    parts[bone.name]!!.addChild(
                        bone.name + counter++.toString(),
                        part,
                        modelTransforms[index]
                    )
                }
            }

            return TexturedModelData.of(
                modelData,
                textureWidth ?: geometry.description.textureWidth,
                textureHeight ?: geometry.description.textureHeight
            )
        } catch (e: Exception) {
            if (geometry != null) {
                throw IllegalArgumentException("Error creating TexturedModelData with identifier ${geometry[0].description.identifier}", e)
            } else {
                throw IllegalArgumentException("Error creating TexturedModelData", e)
            }
        }
    }

    companion object {
        val GSON = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(LocatorBone::class.java, LocatorBoneAdapter)
            .create()

        fun from(json: String) : TexturedModel {
            try {
                return GSON.fromJson(json, TexturedModel::class.java)
            } catch (exception: Exception) {
                throw IllegalStateException("Issue loading pokemon geo: $json", exception)
            }
        }
    }
}
class ModelGeometry {
    lateinit var description: ModelDataDescription
    val bones: List<ModelBone>? = null
}
class ModelDataDescription(
    val identifier: String,
    @SerializedName("texture_width")
    val textureWidth: Int,
    @SerializedName("texture_height")
    val textureHeight: Int,
    @SerializedName("visible_bounds_width")
    val visibleBoundsWidth: Float,
    @SerializedName("visible_bounds_height")
    val visibleBoundsHeight: Float,
    @SerializedName("visible_bounds_offset")
    val visibleBoundsOffset: List<Float>
)

class ModelBone {
    var name: String = ""
    var parent: String? = null
    var pivot: List<Float> = emptyList()
    var rotation: List<Float>? = null
    var cubes: List<Cube>? = null
    var locators: Map<String, LocatorBone>? = null
}

class Cube {
    val origin: List<Float>? = null
    val size: List<Float>? = null
    val pivot: List<Float>? = null
    val rotation: List<Float>? = null
    val uv: List<Int>? = null
    val inflate: Float? = null
    val mirror: Boolean = false
}

class LocatorBone(
    var offset: List<Float> = listOf(0F, 0F, 0F),
    var rotation: List<Float> = listOf(0F, 0F, 0F)
)
