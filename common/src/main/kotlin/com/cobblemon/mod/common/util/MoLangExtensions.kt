/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangEnvironment
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.MoScope
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.MoValue
import java.lang.IllegalArgumentException
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.molang.SingleExpression
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d

val genericRuntime = MoLangRuntime().setup()

fun MoLangRuntime.resolve(expression: Expression, context: Map<String, MoValue> = emptyMap()): MoValue = try {
    //expression.evaluate(MoScope(), environment)
    execute(expression, context)
} catch (e: Exception) {
    throw IllegalArgumentException("Unable to parse expression: ${expression.getString()}", e)
}
fun MoLangRuntime.resolveDouble(expression: Expression): Double = resolve(expression).asDouble()
fun MoLangRuntime.resolveFloat(expression: Expression): Float = resolve(expression).asDouble().toFloat()
fun MoLangRuntime.resolveInt(expression: Expression): Int = resolveDouble(expression).toInt()
fun MoLangRuntime.resolveString(expression: Expression): String = resolve(expression).asString()
fun MoLangRuntime.resolveObject(expression: Expression): ObjectValue<*> = resolve(expression) as ObjectValue<*>
fun MoLangRuntime.resolveBoolean(expression: Expression): Boolean = resolve(expression).asDouble() != 0.0

fun MoLangRuntime.resolve(expression: ExpressionLike): MoValue = expression.resolve(this)
fun MoLangRuntime.resolveDouble(expression: ExpressionLike): Double = resolve(expression).asDouble()
fun MoLangRuntime.resolveFloat(expression: ExpressionLike): Float = resolve(expression).asDouble().toFloat()
fun MoLangRuntime.resolveInt(expression: ExpressionLike): Int = resolveDouble(expression).toInt()
fun MoLangRuntime.resolveString(expression: ExpressionLike): String = resolve(expression).asString()
fun MoLangRuntime.resolveObject(expression: ExpressionLike): ObjectValue<*> = resolve(expression) as ObjectValue<*>
fun MoLangRuntime.resolveBoolean(expression: ExpressionLike): Boolean = resolve(expression).asDouble() != 0.0


fun MoLangRuntime.resolveVec3d(triple: Triple<Expression, Expression, Expression>) = Vec3d(
    resolveDouble(triple.first),
    resolveDouble(triple.second),
    resolveDouble(triple.third)
)

fun MoLangRuntime.resolveBoolean(expression: Expression, pokemon: Pokemon): Boolean {
    environment.writePokemon(pokemon)
    return resolveBoolean(expression)
}

fun MoLangRuntime.resolveDouble(expression: Expression, pokemon: Pokemon): Double {
    environment.writePokemon(pokemon)
    return resolveDouble(expression)
}

fun MoLangRuntime.resolveInt(expression: Expression, pokemon: Pokemon): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression)
}

fun MoLangRuntime.resolveFloat(expression: Expression, pokemon: Pokemon): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression)
}


fun MoLangRuntime.resolveBoolean(expression: Expression, pokemon: BattlePokemon): Boolean {
    environment.writePokemon(pokemon)
    return resolveBoolean(expression)
}

fun MoLangRuntime.resolveDouble(expression: Expression, pokemon: BattlePokemon): Double {
    environment.writePokemon(pokemon)
    return resolveDouble(expression)
}

fun MoLangRuntime.resolveInt(expression: Expression, pokemon: BattlePokemon): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression)
}

fun MoLangRuntime.resolveFloat(expression: Expression, pokemon: BattlePokemon): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression)
}


fun Expression.getString() = originalString ?: "0"
fun Double.asExpressionLike() = SingleExpression(NumberExpression(this))
fun Double.asExpression() = NumberExpression(this)
fun String.asExpression() = try {
    MoLang.createParser(if (this == "") "0.0" else this).parseExpression()
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expression: $this")
    throw exc
}
fun String.asExpressions() = try {
    MoLang.createParser(if (this == "") "0.0" else this).parse()
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expressions: $this")
    throw exc
}

fun String.asExpressionLike() = try {
    val ls = MoLang.createParser(if (this == "") "0.0" else this).parse()
    if (ls.size == 1) {
        SingleExpression(ls[0])
    } else {
        ListExpression(ls)
    }
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expressions: $this")
    throw exc
}

fun MoLangEnvironment.writePokemon(pokemon: Pokemon) {
    val pokemonStruct = VariableStruct()
    pokemon.writeVariables(pokemonStruct)
    setSimpleVariable("pokemon", pokemonStruct)
}

fun MoLangEnvironment.writePokemon(pokemon: BattlePokemon) {
    val pokemonStruct = VariableStruct()
    pokemon.writeVariables(pokemonStruct)
    setSimpleVariable("pokemon", pokemonStruct)
}

fun List<String>.asExpressionLike() = joinToString(separator = "\n").asExpressionLike()
fun List<Expression>.resolve(runtime: MoLangRuntime) = runtime.execute(this)
fun List<Expression>.resolveDouble(runtime: MoLangRuntime) = resolve(runtime).asDouble()
fun List<Expression>.resolveInt(runtime: MoLangRuntime) = resolveDouble(runtime).toInt()
fun List<Expression>.resolveBoolean(runtime: MoLangRuntime) = resolveDouble(runtime) == 1.0
fun List<Expression>.resolveObject(runtime: MoLangRuntime) = resolve(runtime) as ObjectValue<*>

fun MoParams.getStringOrNull(index: Int) = if (params.size > index) getString(index) else null
fun MoParams.getDoubleOrNull(index: Int) = if (params.size > index) getDouble(index) else null
fun MoParams.getBooleanOrNull(index: Int) = if (params.size > index) getDouble(index) == 1.0 else null

fun MoLangRuntime.withQueryValue(name: String, value: MoValue): MoLangRuntime {
    environment.query.functions.put(name) { value }
    return this
}

fun MoLangRuntime.withPlayerValue(name: String = "player", value: PlayerEntity) = withQueryValue(name, value.asMoLangValue())

//fun MoLangRuntime.withPokemonValue(name: String = "pokemon", value: Pokemon) = withQueryValue(name, value.asMoLangValue())
//fun MoLangRuntime.withNPCValue(name: String = "npc", value: NPCEntity) = withQueryValue(name, value.struct)
