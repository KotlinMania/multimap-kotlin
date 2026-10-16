// port-lint: source serde.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

/**
 * Serde trait implementations for [MultiMap].
 */
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.multimap

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.de.mutableListDeserialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdecore.ser.serialize
import kotlin.native.HiddenFromObjC

internal fun <Ok, K, V> serialize(
    multimap: MultiMap<K, V>,
    serializer: Serializer<Ok>,
): SerdeResult<Ok> = multimap.inner.serialize(serializer)

@HiddenFromObjC
class MultiMapVisitor<K, V>(
    private val keyDeserialize: Deserialize<K>,
    private val valueDeserialize: Deserialize<V>,
) : Visitor<MultiMap<K, V>> {
    override fun expecting(): String = "expected a map"

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<MultiMap<K, V>> =
        serdeCatching {
            val hint = access.sizeHint() ?: 0
            val values = MultiMap.withCapacityAndHasher<K, V, Unit>(hint, Unit)
            val listDeserialize = mutableListDeserialize(valueDeserialize)
            while (true) {
                val entry = access.nextEntry(keyDeserialize, listDeserialize).getOrThrow() ?: break
                values.inner[entry.first] = entry.second
            }
            values
        }

    companion object {
        fun <K, V> new(
            keyDeserialize: Deserialize<K>,
            valueDeserialize: Deserialize<V>,
        ): MultiMapVisitor<K, V> = MultiMapVisitor(keyDeserialize, valueDeserialize)
    }
}

fun <K, V> multiMapDeserialize(
    keyDeserialize: Deserialize<K>,
    valueDeserialize: Deserialize<V>,
): Deserialize<MultiMap<K, V>> =
    object : Deserialize<MultiMap<K, V>> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<MultiMap<K, V>> =
            deserializer.deserializeMap(MultiMapVisitor.new(keyDeserialize, valueDeserialize))
    }
