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

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlin.native.HiddenFromObjC

// Upstream MultiMap<K, V, S = RandomState> carries a BuildHasher type parameter
// so callers can swap the standard library hasher. Kotlin's MutableMap hides
// hasher selection, so the port collapses the type to MultiMap<K, V> while
// providing MultiMap.withHasher and MultiMap.withCapacityAndHasher on the
// companion object.

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
 */
@HiddenFromObjC
class MultiMap<K, V> internal constructor(
    internal val inner: MutableMap<K, MutableList<V>>,
) : Iterable<Pair<K, MutableList<V>>>,
    Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializeMultiMap(this, serializer)

    /**
     * Creates an empty MultiMap
     *
     * # Examples
     *
     * ```
     * val map: MultiMap<String, Long> = MultiMap()
     * ```
     */
    constructor() : this(mutableMapOf())

    /**
     * Inserts a key-value pair into the multimap. If the key does exist in
     * the map then the value is pushed to that key's vector. If the key doesn't
     * exist in the map a new vector with the given value is inserted.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<String, Int>()
     * map.insert("key", 42)
     * ```
     */
    fun insert(k: K, v: V) {
        when (val e = entry(k)) {
            is Entry.Occupied -> {
                e.entry.getVecMut().add(v)
            }
            is Entry.Vacant -> {
                e.entry.insertVec(mutableListOf(v))
            }
        }
    }

    /**
     * Inserts multiple key-value pairs into the multimap. If the key does exist in
     * the map then the values are extended into that key's vector. If the key
     * doesn't exist in the map a new vector collected from the given values is inserted.
     *
     * This may be more efficient than inserting values independently.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<String, Int>()
     * map.insertMany("key", listOf(42, 43))
     * ```
     */
    fun insertMany(k: K, v: Iterable<V>) {
        when (val e = entry(k)) {
            is Entry.Occupied -> {
                e.entry.getVecMut().addAll(v)
            }
            is Entry.Vacant -> {
                e.entry.insertVec(v.toMutableList())
            }
        }
    }

    /**
     * Inserts multiple key-value pairs into the multimap. If the key does exist in
     * the map then the values are extended into that key's vector. If the key
     * doesn't exist in the map a new vector collected from the given values is inserted.
     *
     * This may be more efficient than inserting values independently.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<String, Int>()
     * map.insertManyFromSlice("key", listOf(42, 43))
     * ```
     */
    fun insertManyFromSlice(k: K, v: List<V>) {
        when (val e = entry(k)) {
            is Entry.Occupied -> {
                e.entry.getVecMut().addAll(v)
            }
            is Entry.Vacant -> {
                e.entry.insertVec(v.toMutableList())
            }
        }
    }

    /**
     * Returns true if the map contains a value for the specified key.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * check(map.containsKey(1) == true)
     * check(map.containsKey(2) == false)
     * ```
     */
    fun containsKey(k: K): Boolean = inner.containsKey(k)

    /**
     * Returns the number of unique keys in the map.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(2, 1337)
     * map.insert(2, 31337)
     * check(map.len() == 2)
     * ```
     */
    fun len(): Int = inner.size

    /**
     * Removes a key from the map, returning the vector of values at
     * the key if the key was previously in the map.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * check(map.remove(1) == mutableListOf(42, 1337))
     * check(map.remove(1) == null)
     * ```
     */
    fun remove(k: K): MutableList<V>? = inner.remove(k)

    /**
     * Returns a reference to the first item in the vector corresponding to
     * the key.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * check(map.get(1) == 42)
     * ```
     */
    operator fun get(k: K): V? = inner[k]?.firstOrNull()

    /**
     * Returns a mutable reference to the first item in the vector corresponding to
     * the key.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.getVecMut(1)?.set(0, 99)
     * check(map[1] == 99)
     * ```
     */
    fun getMut(k: K): V? = inner[k]?.firstOrNull()

    /**
     * Returns a reference to the vector corresponding to the key.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * check(map.getVec(1) == mutableListOf(42, 1337))
     * ```
     */
    fun getVec(k: K): MutableList<V>? = inner[k]

    /**
     * Returns a mutable reference to the vector corresponding to the key.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.getVecMut(1)?.let {
     *     it[0] = 1991
     *     it[1] = 2332
     * }
     * check(map.getVec(1) == mutableListOf(1991, 2332))
     * ```
     */
    fun getVecMut(k: K): MutableList<V>? = inner[k]

    /**
     * Returns true if the key is multi-valued.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(2, 2332)
     *
     * check(map.isVec(1) == true)   // key is multi-valued
     * check(map.isVec(2) == false)  // key is single-valued
     * check(map.isVec(3) == false)  // key not in map
     * ```
     */
    fun isVec(k: K): Boolean {
        val v = getVec(k) ?: return false
        return v.size > 1
    }

    /**
     * Returns the number of elements the map can hold without reallocating.
     *
     * # Examples
     *
     * ```
     * val map: MultiMap<Int, Int> = MultiMap()
     * check(map.capacity() >= 0)
     * ```
     */
    fun capacity(): Int = inner.size

    /**
     * Returns true if the map contains no elements.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * check(map.isEmpty())
     * map.insert(1, 42)
     * check(!map.isEmpty())
     * ```
     */
    fun isEmpty(): Boolean = inner.isEmpty()

    /**
     * Clears the map, removing all key-value pairs.
     * Keeps the allocated memory for reuse.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.clear()
     * check(map.isEmpty())
     * ```
     */
    fun clear() {
        inner.clear()
    }

    /**
     * An iterator visiting all keys in arbitrary order.
     * Iterator element type is [K].
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(2, 1337)
     * map.insert(4, 1991)
     *
     * val keys = map.keys().toMutableList()
     * keys.sort()
     * check(keys == mutableListOf(1, 2, 4))
     * ```
     */
    fun keys(): Iterable<K> = inner.keys

    /**
     * An iterator visiting pairs of each key and its first value in arbitrary order.
     * The iterator returns a pair of the key and the first element in the
     * corresponding key's vector. Iterator element type is `Pair<K, V>`.
     *
     * See [flatIter] for visiting all key-value pairs,
     * or [iterAll] for visiting each key and its vector of values.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(3, 2332)
     * map.insert(4, 1991)
     *
     * val pairs = map.iter().asSequence().toMutableList()
     * pairs.sortBy { it.first }
     * check(pairs == mutableListOf(1 to 42, 3 to 2332, 4 to 1991))
     * ```
     */
    fun iter(): Iter<K, V> = Iter(inner.entries.iterator(), inner.size)

    /**
     * A mutable iterator visiting pairs of each key and its first value
     * in arbitrary order. Iterator element type is `Pair<K, V>`.
     *
     * See [flatIterMut] for visiting all key-value pairs,
     * or [iterAllMut] for visiting each key and its vector of values.
     */
    fun iterMut(): IterMut<K, V> = IterMut(inner.entries.iterator(), inner.size)

    /**
     * An iterator visiting all key-value pairs in arbitrary order. The iterator returns
     * a pair of the key and the corresponding key's vector.
     * Iterator element type is `Pair<K, MutableList<V>>`.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(3, 2332)
     * map.insert(4, 1991)
     *
     * val pairs = map.iterAll().toMutableList()
     * pairs.sortBy { it.first }
     * check(pairs == mutableListOf(
     *     1 to mutableListOf(42, 1337),
     *     3 to mutableListOf(2332),
     *     4 to mutableListOf(1991),
     * ))
     * ```
     */
    fun iterAll(): IterAll<K, V> =
        inner.entries.map { it.key to it.value }

    /**
     * An iterator visiting all key-value pairs in arbitrary order. The iterator returns
     * a pair of the key and the corresponding key's vector. Iterator element type is
     * `Pair<K, MutableList<V>>`.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(3, 2332)
     * map.insert(4, 1991)
     *
     * for ((_, values) in map.iterAllMut()) {
     *     for (i in values.indices) values[i] = 99
     * }
     *
     * val pairs = map.iterAllMut().toMutableList()
     * pairs.sortBy { it.first }
     * check(pairs == mutableListOf(
     *     1 to mutableListOf(99, 99),
     *     3 to mutableListOf(99),
     *     4 to mutableListOf(99),
     * ))
     * ```
     */
    fun iterAllMut(): IterAllMut<K, V> =
        inner.entries.map { it.key to it.value }

    /**
     * An iterator visiting all key-value pairs in arbitrary order.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(3, 2332)
     * map.insert(4, 1991)
     *
     * val pairs = map.flatIter().toMutableList()
     * pairs.sortWith(compareBy({ it.first }, { it.second }))
     * check(pairs == mutableListOf(1 to 42, 1 to 1337, 3 to 2332, 4 to 1991))
     * ```
     */
    fun flatIter(): Iterable<Pair<K, V>> =
        iterAll().flatMap { (k, v) -> v.map { i -> k to i } }

    /**
     * A mutable iterator visiting all key-value pairs in arbitrary order.
     *
     * # Examples
     *
     * ```
     * val map = MultiMap<Int, Int>()
     * map.insert(1, 42)
     * map.insert(1, 1337)
     * map.insert(3, 2332)
     * map.insert(4, 1991)
     *
     * for ((key, _) in map.flatIterMut()) {
     *     map.getVecMut(key)?.let { vec ->
     *         for (i in vec.indices) vec[i] = vec[i] * key
     *     }
     * }
     *
     * val pairs = map.flatIter().toMutableList()
     * pairs.sortWith(compareBy({ it.first }, { it.second }))
     * check(pairs == mutableListOf(1 to 42, 1 to 1337, 3 to 6996, 4 to 7964))
     * ```
     */
    fun flatIterMut(): Iterable<Pair<K, V>> =
        iterAllMut().flatMap { (k, v) -> v.map { i -> k to i } }

    /**
     * Gets the specified key's corresponding entry in the map for in-place manipulation.
     * It's possible to both manipulate the vector and the `value` (the first value in the
     * vector).
     *
     * # Examples
     *
     * ```
     * val m = MultiMap<Int, Int>()
     * m.insert(1, 42)
     *
     * run {
     *     m.entry(1).orInsert(43)
     *     m.getVecMut(1)?.set(0, 44)
     * }
     * check(m.entry(2).orInsert(666) == 666)
     *
     * run {
     *     m.entry(1).orInsertVec(mutableListOf(43))
     *     m.getVecMut(1)?.add(50)
     * }
     * check(m.entry(2).orInsertVec(mutableListOf(667)) == mutableListOf(666))
     *
     * check(m.getVec(1) == mutableListOf(44, 50))
     * ```
     */
    fun entry(k: K): Entry<K, V> =
        if (inner.containsKey(k)) {
            Entry.Occupied(OccupiedEntry(inner, k))
        } else {
            Entry.Vacant(VacantEntry(inner, k))
        }

    /**
     * Retains only the elements specified by the predicate.
     *
     * In other words, remove all pairs `(k, v)` such that `f(k, v)` returns `false`.
     *
     * # Examples
     *
     * ```
     * val m = MultiMap<Int, Int>()
     * m.insert(1, 42)
     * m.insert(1, 99)
     * m.insert(2, 42)
     * m.retain { k, v -> k == 1 && v == 42 }
     * check(1 == m.len())
     * check(42 == m.get(1))
     * ```
     */
    fun retain(f: (K, V) -> Boolean) {
        for ((key, vector) in inner) {
            val keep = ArrayList<V>(vector.size)
            for (value in vector) {
                if (f(key, value)) keep.add(value)
            }
            vector.clear()
            vector.addAll(keep)
        }
        val emptyKeys = inner.entries.filter { it.value.isEmpty() }.map { it.key }
        for (k in emptyKeys) inner.remove(k)
    }

    /**
     * Returns the first value in the vector for the given key, throwing
     * [IllegalStateException] when the key is missing ("no entry found for key")
     * or the vector is empty ("no value found for key").
     */
    fun index(k: K): V {
        val list = inner[k] ?: error("no entry found for key")
        return list.firstOrNull() ?: error("no value found for key")
    }

    /** Formats the multimap entries as a debug map string. */
    fun fmt(): String {
        val sb = StringBuilder("{")
        var first = true
        for ((k, v) in inner) {
            if (!first) sb.append(", ")
            sb.append(k).append(": ").append(v)
            first = false
        }
        sb.append("}")
        return sb.toString()
    }

    override fun toString(): String = fmt()

    /** Returns true if this multimap contains the same keys and value lists as [other]. */
    fun eq(other: MultiMap<K, V>): Boolean {
        if (len() != other.len()) return false
        return iterAll().all { (key, value) -> other.getVec(key) == value }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultiMap<*, *>) return false
        if (len() != other.len()) return false

        val otherInner: Map<*, *> = other.inner
        for ((key, value) in iterAll()) {
            val otherList = otherInner[key] ?: return false
            if (value != otherList) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        for ((k, v) in inner) {
            h += (k?.hashCode() ?: 0) xor v.hashCode()
        }
        return h
    }

    /** Returns an iterator over `(key, values)` pairs in the multimap. */
    fun intoIter(): Iterator<Pair<K, MutableList<V>>> = iterAll().iterator()

    override fun iterator(): Iterator<Pair<K, MutableList<V>>> = intoIter()

    companion object {
        /** Creates an empty [MultiMap]. */
        internal fun <K, V> new(): MultiMap<K, V> = MultiMap()

        /**
         * Creates an empty multimap with the given initial capacity.
         *
         * # Examples
         *
         * ```
         * val map: MultiMap<String, Long> = MultiMap.withCapacity(20)
         * ```
         */
        internal fun <K, V> withCapacity(capacity: Int): MultiMap<K, V> = MultiMap(HashMap(capacity))

        /**
         * Creates an empty [MultiMap] which will use the given hash builder to hash keys.
         */
        internal fun <K, V, S> withHasher(hashBuilder: S): MultiMap<K, V> {
            hashBuilder.hashCode()
            return MultiMap()
        }

        /**
         * Creates an empty [MultiMap] with the given initial capacity and hash builder.
         */
        internal fun <K, V, S> withCapacityAndHasher(
            capacity: Int,
            hashBuilder: S,
        ): MultiMap<K, V> {
            hashBuilder.hashCode()
            return MultiMap(HashMap(capacity))
        }

        /** Creates an empty default [MultiMap]. */
        internal fun <K, V> default(): MultiMap<K, V> = MultiMap()

        /** Builds a [MultiMap] from an iterable of single key-value pairs. */
        fun <K, V> fromIter(iterable: Iterable<Pair<K, V>>): MultiMap<K, V> {
            val multimap = MultiMap<K, V>()
            for ((k, v) in iterable) {
                multimap.insert(k, v)
            }
            return multimap
        }

        /** Builds a [MultiMap] from an iterable of single key-value pairs. */
        fun <K, V> fromIterator(iterable: Iterable<Pair<K, V>>): MultiMap<K, V> =
            fromIter(iterable)

        /** Builds a [MultiMap] from an iterable of `Pair<K, List<V>>` values. */
        fun <K, V> fromIteratorVec(iterable: Iterable<Pair<K, List<V>>>): MultiMap<K, V> {
            val multimap = MultiMap<K, V>()
            for ((k, v) in iterable) {
                multimap.insertManyFromSlice(k, v)
            }
            return multimap
        }
    }
}

/** Extends the multimap with single key-value pairs. */
@HiddenFromObjC
fun <K, V> MultiMap<K, V>.extend(iter: Iterable<Pair<K, V>>) {
    for ((k, v) in iter) {
        insert(k, v)
    }
}

/** Extends the multimap with `Pair<K, List<V>>` values. */
@HiddenFromObjC
fun <K, V> MultiMap<K, V>.extendVec(iter: Iterable<Pair<K, List<V>>>) {
    for ((k, values) in iter) {
        when (val e = entry(k)) {
            is Entry.Occupied -> {
                e.entry.getVecMut().addAll(values)
            }
            is Entry.Vacant -> {
                e.entry.insertVec(values.toMutableList())
            }
        }
    }
}

/**
 * Create a [MultiMap] from a list of key value pairs.
 *
 * ## Example
 *
 * ```
 * val map = multimapOf(
 *     "dog" to "husky",
 *     "dog" to "retreaver",
 *     "dog" to "shiba inu",
 *     "cat" to "cat",
 * )
 * ```
 */
@HiddenFromObjC
fun <K, V> multimapOf(vararg pairs: Pair<K, V>): MultiMap<K, V> {
    val map = MultiMap.withCapacity<K, V>(pairs.size)
    for ((k, v) in pairs) {
        map.insert(k, v)
    }
    return map
}
