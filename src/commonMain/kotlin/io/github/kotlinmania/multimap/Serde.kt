// port-lint: source src/serde.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

/**
 * kotlinx.serialization implementations for [MultiMap].
 *
 * Mirrors the upstream `serde_impl` Cargo feature, which is on by default.
 * Upstream wires [MultiMap] into the serde derive ecosystem by hand-rolling a
 * `Serialize` impl that delegates to the inner map and a `Deserialize` impl
 * driven by a [MultiMapVisitor]. kotlinx.serialization expresses both
 * directions through a single [KSerializer], so the two upstream impls and the
 * helper visitor collapse into [MultiMapSerializer], which delegates to a
 * `Map<K, List<V>>` serializer underneath.
 */

package io.github.kotlinmania.multimap

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// impl<K, V, BS> Serialize for MultiMap<K, V, BS>
//     where K: Serialize + Eq + Hash, V: Serialize, BS: BuildHasher
//
// impl<'a, K, V, S> Deserialize<'a> for MultiMap<K, V, S>
//     where K: Deserialize<'a> + Eq + Hash, V: Deserialize<'a>,
//           S: BuildHasher + Default
//
// Both upstream impls go through the inner `HashMap<K, Vec<V>>`. The Kotlin
// translation drops the `BuildHasher` generic — Kotlin's [MutableMap] hides
// hasher selection — and pairs the [MultiMap] with a serializer that round-
// trips it as a `Map<K, List<V>>` using kotlinx.serialization's built-in
// [MapSerializer] and [ListSerializer].
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

    // Mirrors the upstream `MultiMapVisitor::visit_map`, which reads each
    // `(K, Vec<V>)` entry and inserts it directly into the new multimap's
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
