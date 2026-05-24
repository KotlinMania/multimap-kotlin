// port-lint: source src/entry.rs
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

// Upstream `entry.rs` re-exports `OccupiedEntry` and `VacantEntry` from
// `std::collections::hash_map`, wrapping each so the MultiMap can vend
// vector-valued views into individual map slots. Kotlin's standard
// `MutableMap` does not vend separate occupied / vacant entry types, so the
// port models the wrappers as simple holders that carry the backing
// `MutableMap<K, MutableList<V>>` together with the entry's key. This keeps
// the per-method semantics (panicking when an Occupied entry has zero values,
// extending with collections, removing the value vector) identical to
// upstream while compiling against the Kotlin standard library only.

/** A view into a single occupied location in a MultiMap. */
@HiddenFromObjC
class OccupiedEntry<K, V> internal constructor(
    internal val inner: MutableMap<K, MutableList<V>>,
    internal val key: K,
) {
    /**
     * Gets a reference to the first item in value in the vector corresponding to entry.
     *
     * # Panics
     *
     * This method will panic if the key has zero values.
     */
    fun get(): V {
        return inner[key]!!.firstOrNull() ?: error("no values in entry")
    }

    /** Gets a reference to the values (vector) corresponding to entry. */
    fun getVec(): MutableList<V> {
        return inner[key]!!
    }

    /**
     * Gets a mut reference to the first item in value in the vector corresponding to entry.
     *
     * # Panics
     *
     * This method will panic if the key has zero values.
     */
    fun getMut(): V {
        return inner[key]!!.firstOrNull() ?: error("no values in entry")
    }

    /** Gets a mut reference to the values (vector) corresponding to entry. */
    fun getVecMut(): MutableList<V> {
        return inner[key]!!
    }

    /**
     * Converts the OccupiedEntry into a mutable reference to the first item in value in the entry
     * with a lifetime bound to the map itself
     */
    fun intoMut(): V {
        return inner[key]!![0]
    }

    /**
     * Converts the OccupiedEntry into a mutable reference to the values (vector) in the entry
     * with a lifetime bound to the map itself
     */
    fun intoVecMut(): MutableList<V> {
        return inner[key]!!
    }

    /** Inserts a new value onto the vector of the entry. */
    fun insert(value: V) {
        getVecMut().add(value)
    }

    /** Extends the existing vector with the specified values. */
    fun insertVec(values: List<V>) {
        getVecMut().addAll(values)
    }

    /** Takes the values (vector) out of the entry, and returns it */
    fun remove(): MutableList<V> {
        return inner.remove(key)!!
    }
}

/** A view into a single empty location in a MultiMap. */
@HiddenFromObjC
class VacantEntry<K, V> internal constructor(
    internal val inner: MutableMap<K, MutableList<V>>,
    internal val key: K,
) {
    /**
     * Sets the first value in the vector of the entry with the VacantEntry's key,
     * and returns a mutable reference to it.
     */
    fun insert(value: V): V {
        val list = mutableListOf(value)
        inner[key] = list
        return list[0]
    }

    /**
     * Sets values in the entry with the VacantEntry's key,
     * and returns a mutable reference to it.
     */
    fun insertVec(values: MutableList<V>): MutableList<V> {
        inner[key] = values
        return values
    }
}

/** A view into a single location in a map, which may be vacant or occupied. */
@HiddenFromObjC
sealed class Entry<K, V> {
    /** An occupied Entry. */
    @HiddenFromObjC
    class Occupied<K, V>(val entry: OccupiedEntry<K, V>) : Entry<K, V>()

    /** A vacant Entry. */
    @HiddenFromObjC
    class Vacant<K, V>(val entry: VacantEntry<K, V>) : Entry<K, V>()

    /**
     * Ensures a value is in the entry by inserting the default if empty, and returns
     * a mutable reference to the value in the entry. This will return a mutable reference to the
     * first value in the vector corresponding to the specified key.
     */
    fun orInsert(default: V): V {
        return when (this) {
            is Occupied -> entry.intoMut()
            is Vacant -> entry.insert(default)
        }
    }

    /**
     * Ensures a value is in the entry by inserting the default values if empty, and returns
     * a mutable reference to the values (the corresponding vector to the specified key) in
     * the entry.
     */
    fun orInsertVec(defaults: MutableList<V>): MutableList<V> {
        return when (this) {
            is Occupied -> entry.intoVecMut()
            is Vacant -> entry.insertVec(defaults)
        }
    }
}
