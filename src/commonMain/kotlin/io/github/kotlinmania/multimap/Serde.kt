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
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.native.HiddenFromObjC

fun wrapSerialize(value: Any?): Serialize =
    when (value) {
        null ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeNone()
            }
        is Serialize -> value
        is Boolean ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeBool(value)
            }
        is Byte ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeI8(value)
            }
        is Short ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeI16(value)
            }
        is Int ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeI32(value)
            }
        is Long ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeI64(value)
            }
        is UByte ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeU8(value)
            }
        is UShort ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeU16(value)
            }
        is UInt ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeU32(value)
            }
        is ULong ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeU64(value)
            }
        is Float ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeF32(value)
            }
        is Double ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeF64(value)
            }
        is Char ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeChar(value)
            }
        is String ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeStr(value)
            }
        is ByteArray ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serializer.serializeBytes(value)
            }
        is Iterable<*> ->
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    serdeCatching {
                        val list = value.toList()
                        val seq = serializer.serializeSeq(list.size).getOrThrow()
                        for (item in list) {
                            seq.serializeElement(wrapSerialize(item)).getOrThrow()
                        }
                        seq.end().getOrThrow()
                    }
            }
        else -> error("Cannot serialize value of type ${value::class}")
    }

internal fun <Ok, K, V> serializeMultiMap(
    multimap: MultiMap<K, V>,
    serializer: Serializer<Ok>,
): SerdeResult<Ok> =
    serdeCatching {
        val mapSerializer = serializer.serializeMap(multimap.inner.size).getOrThrow()
        for ((key, values) in multimap.inner) {
            mapSerializer
                .serializeEntry(
                    wrapSerialize(key),
                    wrapSerialize(values),
                ).getOrThrow()
        }
        mapSerializer.end().getOrThrow()
    }

fun <K, V> multiMapDeserialize(
    keyDeserialize: Deserialize<K>,
    valueDeserialize: Deserialize<V>,
): Deserialize<MultiMap<K, V>> =
    object : Deserialize<MultiMap<K, V>> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<MultiMap<K, V>> =
            deserializer.deserializeMap(
                object : Visitor<MultiMap<K, V>> {
                    override fun expecting(): String = "expected a map"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<MultiMap<K, V>> =
                        serdeCatching {
                            val hint = access.sizeHint() ?: 0
                            val multimap = MultiMap.withCapacity<K, V>(hint)
                            val listDeserialize = mutableListDeserialize(valueDeserialize)
                            while (true) {
                                val entry = access.nextEntry(keyDeserialize, listDeserialize).getOrThrow() ?: break
                                multimap.inner[entry.first] = entry.second
                            }
                            multimap
                        }
                },
            )
    }

@HiddenFromObjC
class MultiMapSerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
) : KSerializer<MultiMap<K, V>> {
    private val delegate: KSerializer<Map<K, List<V>>> =
        MapSerializer(keySerializer, ListSerializer(valueSerializer))

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: MultiMap<K, V>) {
        delegate.serialize(encoder, value.inner)
    }

    // Reads each `(K, List<V>)` entry and inserts it directly into the new multimap's
    // backing map. The capacity hint comes from the underlying serializer's
    // size hint, propagated through [MultiMap.withCapacity].
    override fun deserialize(decoder: Decoder): MultiMap<K, V> {
        val map = delegate.deserialize(decoder)
        val values = MultiMap.withCapacity<K, V>(map.size)
        for ((key, value) in map) {
            values.inner[key] = value.toMutableList()
        }
        return values
    }
}
