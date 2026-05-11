// port-lint: source src/lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

package io.github.kotlinmania.multimap

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
