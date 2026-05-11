// port-lint: source src/lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

package io.github.kotlinmania.multimap

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
