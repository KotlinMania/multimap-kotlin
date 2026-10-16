// port-lint: source lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

package io.github.kotlinmania.multimap

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
//
// A map implementation which allows storing multiple values per key.
//
// The interface is roughly based on Kotlin's MutableMap, but is changed
// and extended to accomodate the multi-value use case. In fact, MultiMap is
// implemented mostly as a thin wrapper around a backing map and stores its
// values as a MutableList per key.
//
// Values are guaranteed to be in insertion order as long as not manually
// changed. Keys are not ordered. Multiple idential key-value-pairs can exist
// in the MultiMap. A key can exist in the MultiMap with no associated value.
