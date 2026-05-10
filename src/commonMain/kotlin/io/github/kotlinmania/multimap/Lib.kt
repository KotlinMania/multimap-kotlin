// port-lint: source src/lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

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
package io.github.kotlinmania.multimap

// Upstream re-exports `std::collections::hash_map::Iter as IterAll` and
// `IterMut as IterAllMut` from the crate root. Kotlin's standard library does
// not vend a separate immutable / mutable map iterator pair, so the port
// surfaces those views directly through the [MultiMap.iterAll] and
// [MultiMap.iterAllMut] methods, which yield `Pair<K, MutableList<V>>` and
// `Pair<K, MutableList<V>>` respectively (mutability of the backing list
// matches the Rust iterator's mutability).
//
// Upstream `MultiMap<K, V, S = RandomState>` carries a `BuildHasher` generic
// so callers can swap the standard library hasher. Kotlin's `MutableMap`
// hides hasher selection, so the port collapses to `MultiMap<K, V>`. The
// upstream `with_hasher` and `with_capacity_and_hasher` constructors have no
// faithful Kotlin counterpart and therefore do not appear here.

class MultiMap<K, V> private constructor(
    internal val inner: MutableMap<K, MutableList<V>>,
) : Iterable<Pair<K, MutableList<V>>> {

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
     * map.insertManyFromSlice("key", intArrayOf(42, 43))
     * ```
     *
     * The Rust upstream constrains this method to `V: Clone` and accepts a
     * borrowed slice. Kotlin doesn't distinguish slice-ownership, so the port
     * accepts any [List].
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
     * map.getMut(1)?.let { /* in Kotlin, mutate via getVecMut(1)?.set(0, 99) */ }
     * map.getVecMut(1)?.set(0, 99)
     * check(map[1] == 99)
     * ```
     *
     * Kotlin returns the value by copy; to overwrite the first slot, use
     * [getVecMut] and assign through the returned list.
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
     *
     * Kotlin's [MutableMap] does not expose its load capacity, so the port
     * approximates this with the current size, which is always a valid lower
     * bound on the capacity.
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
     * val pairs = map.iter().toMutableList()
     * pairs.sortBy { it.first }
     * check(pairs == mutableListOf(1 to 42, 3 to 2332, 4 to 1991))
     * ```
     */
    fun iter(): Iter<K, V> = Iter(inner.entries.iterator())

    /**
     * A mutable iterator visiting pairs of each key and its first value
     * in arbitrary order. Iterator element type is `Pair<K, V>`.
     *
     * Kotlin returns values by copy, so mutating through the iterator's
     * `second` does not write back to the underlying map. To overwrite the
     * first slot, use [getVecMut] on the key.
     *
     * See [flatIterMut] for visiting all key-value pairs,
     * or [iterAllMut] for visiting each key and its vector of values.
     */
    fun iterMut(): IterMut<K, V> = IterMut(inner.entries.iterator())

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
    fun iterAll(): Iterable<Pair<K, MutableList<V>>> =
        inner.entries.map { it.key to it.value }

    /**
     * An iterator visiting all key-value pairs in arbitrary order. The iterator returns
     * a pair of the key and the corresponding key's vector. Iterator element type is
     * `Pair<K, MutableList<V>>`.
     *
     * Mutations to the returned [MutableList] are visible in the [MultiMap]
     * because the lists are the same instances stored inside the map.
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
    fun iterAllMut(): Iterable<Pair<K, MutableList<V>>> =
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
     *
     * Kotlin returns iteration values by copy, so the upstream pattern
     * `*value *= key` does not translate directly. Mutate through
     * [getVecMut] for each key instead.
     */
    fun flatIterMut(): Iterable<Pair<K, V>> =
        iterAllMut().flatMap { (k, v) -> v.map { i -> k to i } }

    /**
     * Gets the specified key's corresponding entry in the map for in-place manipulation.
     * It's possible to both manipulate the vector and the 'value' (the first value in the
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
    fun entry(k: K): Entry<K, V> {
        return if (inner.containsKey(k)) {
            Entry.Occupied(OccupiedEntry(inner, k))
        } else {
            Entry.Vacant(VacantEntry(inner, k))
        }
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

    // impl Index<&Q> for MultiMap
    /**
     * Mirrors the upstream `Index::index` impl: returns the first value in the
     * vector for the given key, throwing [IllegalStateException] when the key
     * is missing or the vector is empty (`panic!("no entry found for key")`
     * / `panic!("no value found for key")`).
     *
     * Kotlin's `m[k]` operator already maps to [get], which returns `V?` and
     * matches the upstream `MultiMap::get` semantics; the panicking variant
     * is exposed under [index] to preserve both halves of the upstream API.
     */
    fun index(k: K): V {
        val list = inner[k] ?: error("no entry found for key")
        return list.firstOrNull() ?: error("no value found for key")
    }

    // impl Debug for MultiMap
    override fun toString(): String {
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

    // impl PartialEq for MultiMap
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
        // Mirror upstream PartialEq+Eq with a hash that respects the (key, vector) view.
        var h = 0
        for ((k, v) in inner) {
            h += (k?.hashCode() ?: 0) xor v.hashCode()
        }
        return h
    }

    // impl IntoIterator for &MultiMap (and &mut MultiMap and MultiMap)
    override fun iterator(): Iterator<Pair<K, MutableList<V>>> = iterAll().iterator()

    companion object {
        /**
         * Creates an empty multimap with the given initial capacity.
         *
         * # Examples
         *
         * ```
         * val map: MultiMap<String, Long> = MultiMap.withCapacity(20)
         * ```
         *
         * Kotlin's [MutableMap] does not honour an initial-capacity hint
         * across every target backend; the port still accepts the value so
         * the upstream API surface is preserved.
         */
        fun <K, V> withCapacity(capacity: Int): MultiMap<K, V> {
            return MultiMap(HashMap(capacity))
        }

        // impl Default for MultiMap
        /** Mirrors Rust `Default` for the [MultiMap] type. */
        fun <K, V> default(): MultiMap<K, V> = MultiMap()

        // impl FromIterator<(K, V)> for MultiMap
        /** Builds a [MultiMap] from an iterable of single key-value pairs. */
        fun <K, V> fromIterator(iterable: Iterable<Pair<K, V>>): MultiMap<K, V> {
            val multimap = MultiMap<K, V>()
            for ((k, v) in iterable) {
                multimap.insert(k, v)
            }
            return multimap
        }

        // impl FromIterator<(K, Vec<V>)> for MultiMap
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

// impl Extend<(K, V)> for MultiMap
/** Extends the multimap with single key-value pairs. */
fun <K, V> MultiMap<K, V>.extend(iter: Iterable<Pair<K, V>>) {
    for ((k, v) in iter) {
        insert(k, v)
    }
}

// impl Extend<(K, Vec<V>)> for MultiMap
/** Extends the multimap with `Pair<K, List<V>>` values. */
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
 * Iterator visiting pairs of each key and its first value in arbitrary order.
 *
 * Mirrors the upstream `Iter<'a, K, V>` struct.
 */
class Iter<K, V> internal constructor(
    private val inner: Iterator<Map.Entry<K, MutableList<V>>>,
) : Iterator<Pair<K, V>> {
    private var pending: Pair<K, V>? = null

    private fun advance(): Pair<K, V>? {
        while (inner.hasNext()) {
            val (k, v) = inner.next()
            val first = v.firstOrNull()
            if (first != null) return k to first
        }
        return null
    }

    override fun hasNext(): Boolean {
        if (pending != null) return true
        pending = advance()
        return pending != null
    }

    override fun next(): Pair<K, V> {
        val ready = pending
        pending = null
        return ready ?: advance() ?: throw NoSuchElementException()
    }
}

/**
 * Mutable iterator visiting pairs of each key and its first value in arbitrary
 * order.
 *
 * Mirrors the upstream `IterMut<'a, K, V>` struct. Mutations through the
 * yielded [Pair.second] in Kotlin are by copy; to write back into the map use
 * [MultiMap.getVecMut].
 */
class IterMut<K, V> internal constructor(
    private val inner: Iterator<Map.Entry<K, MutableList<V>>>,
) : Iterator<Pair<K, V>> {
    private var pending: Pair<K, V>? = null

    private fun advance(): Pair<K, V>? {
        while (inner.hasNext()) {
            val (k, v) = inner.next()
            val first = v.firstOrNull()
            if (first != null) return k to first
        }
        return null
    }

    override fun hasNext(): Boolean {
        if (pending != null) return true
        pending = advance()
        return pending != null
    }

    override fun next(): Pair<K, V> {
        val ready = pending
        pending = null
        return ready ?: advance() ?: throw NoSuchElementException()
    }
}

/**
 * Create a [MultiMap] from a list of key value pairs.
 *
 * Mirrors the upstream `multimap!` macro.
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
fun <K, V> multimapOf(vararg pairs: Pair<K, V>): MultiMap<K, V> {
    val map = MultiMap.withCapacity<K, V>(pairs.size)
    for ((k, v) in pairs) {
        map.insert(k, v)
    }
    return map
}
