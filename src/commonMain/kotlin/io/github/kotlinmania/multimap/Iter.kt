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

/**
 * Iterator visiting pairs of each key and its first value in arbitrary order.
 */
@HiddenFromObjC
class Iter<K, V> internal constructor(
    private val inner: Iterator<Map.Entry<K, MutableList<V>>>,
    private var remaining: Int = 0,
) : Iterator<Pair<K, V>> {
    private var pending: Pair<K, V>? = null

    private fun advance(): Pair<K, V>? {
        while (inner.hasNext()) {
            val (k, v) = inner.next()
            if (remaining > 0) {
                remaining -= 1
            }
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

    /** Returns the bounds on the remaining length of the iterator. */
    fun sizeHint(): Pair<Int, Int?> {
        val count = len()
        return count to count
    }

    /** Returns the exact remaining length of the iterator. */
    fun len(): Int = remaining + if (pending != null) 1 else 0
}
