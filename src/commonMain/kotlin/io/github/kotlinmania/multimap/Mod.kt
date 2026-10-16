// port-lint: source lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.multimap

import kotlin.native.HiddenFromObjC

// Tracking file for upstream multimap/src/lib.rs.
//
// Upstream module declarations:
//   Module `entry` provides Entry, OccupiedEntry, and VacantEntry in `Entry.kt`.
//   Module `serde` provides serialization support in `Serde.kt`.
//   Core MultiMap, Iter, IterMut, and multimapOf are parceled into `MultiMap.kt`, `Iter.kt`, and `IterMut.kt`.
//
// Upstream re-exports:
//   Re-exports Iter as IterAll and IterMut as IterAllMut from the standard hash map module
//   Re-exports Entry, OccupiedEntry, and VacantEntry from entry
//
// Callers migrated:
//   codex-kotlin/codex-config -> io.github.kotlinmania.multimap.MultiMap
//   codex-kotlin/codex-execpolicy -> io.github.kotlinmania.multimap.MultiMap
//   codex-kotlin/codex-execpolicy-legacy -> io.github.kotlinmania.multimap.MultiMap

/**
 * A map implementation which allows storing multiple values per key.
 *
 * The interface is roughly based on Kotlin's [MutableMap], but is changed
 * and extended to accomodate the multi-value use case. In fact, [MultiMap] is
 * implemented mostly as a thin wrapper around a backing map and stores its
 * values as a [MutableList] per key.
 *
 * Values are guaranteed to be in insertion order as long as not manually
 * changed. Keys are not ordered. Multiple idential key-value-pairs can exist
 * in the [MultiMap]. A key can exist in the [MultiMap] with no associated value.
 *
 * # Examples
 *
 * ```
 * // create a new MultiMap. An explicit type signature can be omitted because of the
 * // type inference.
 * val queries = MultiMap<String, String>()
 *
 * // insert some queries.
 * queries.insert("urls", "http://rust-lang.org")
 * queries.insert("urls", "http://mozilla.org")
 * queries.insert("urls", "http://wikipedia.org")
 * queries.insert("id", "42")
 * queries.insert("name", "roger")
 *
 * // check if there's any urls.
 * println("Are there any urls in the multimap? ${if (queries.containsKey("urls")) "Yes" else "No"}.")
 *
 * // get the first item in a key's vector.
 * check(queries.get("urls") == "http://rust-lang.org")
 *
 * // get all the urls.
 * check(queries.getVec("urls") ==
 *     mutableListOf("http://rust-lang.org", "http://mozilla.org", "http://wikipedia.org"))
 *
 * // iterate over all keys and the first value in the key's vector.
 * for ((key, value) in queries.iter()) {
 *     println("key: $key, val: $value")
 * }
 *
 * // iterate over all keys and the key's vector.
 * for ((key, values) in queries.iterAll()) {
 *     println("key: $key, values: $values")
 * }
 *
 * // the different methods for getting value(s) from the multimap.
 * val map = MultiMap<String, Int>()
 *
 * map.insert("key1", 42)
 * map.insert("key1", 1337)
 *
 * check(map["key1"] == 42)
 * check(map.get("key1") == 42)
 * check(map.getVec("key1") == mutableListOf(42, 1337))
 * ```
 */
@HiddenFromObjC
typealias IterAll<K, V> = Iterable<Pair<K, MutableList<V>>>

/** Mutable iterator view over all keys and their associated value lists. */
@HiddenFromObjC
typealias IterAllMut<K, V> = Iterable<Pair<K, MutableList<V>>>
