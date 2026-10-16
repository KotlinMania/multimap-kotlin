# multimap-kotlin

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/multimap-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/multimap-kotlin)
[![GitHub](https://img.shields.io/badge/GitHub-KotlinMania%2Fmultimap--kotlin-blue?logo=github)](https://github.com/KotlinMania/multimap-kotlin)
[![Build Status](https://img.shields.io/github/actions/workflow/status/KotlinMania/multimap-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/multimap-kotlin/actions)
[![License](https://img.shields.io/badge/License-MIT%20%2F%20Apache--2.0-blue.svg)](#license)

A **Kotlin Multiplatform** multimap collection (`MultiMap<K, V>`) that associates each key with an ordered list of multiple values. This library is a faithful 1:1 Kotlin Multiplatform port of the Rust [`multimap`](https://github.com/havarnov/multimap) crate.

`MultiMap<K, V>` is implemented as a lightweight, zero-overhead wrapper around `HashMap<K, MutableList<V>>`, preserving insertion order for values stored under the same key while providing O(1) average-time key lookup, rich entry-based mutation, flat and grouped iteration views, and built-in `kotlinx.serialization` and `serde-kotlin` support.

---

## Features

- **Multiple Values per Key**: Store one or many values under the same key without boilerplate list management.
- **First-Value & Full-Vector Access**: Query either the first inserted value (`map[key]`, `get(key)`, `getMut(key)`) or the entire value list (`getVec(key)`, `getVecMut(key)`).
- **In-Place Entry API**: Inspect, insert, or append to a key's value list in a single lookup via `entry(key)` (`Entry.Occupied` / `Entry.Vacant`, `orInsert`, `orInsertWith`, `orInsertVec`, `orInsertVecWith`).
- **Six Iteration Views**:
  - First-value per key: `iter()` / `iterMut()` (`Iter<K, V>` / `IterMut<K, V>` with `len()` and `sizeHint()`)
  - All value lists per key: `iterAll()` / `iterAllMut()` (`IterAll<K, V>` / `IterAllMut<K, V>`)
  - Flattened `(K, V)` pairs across all values: `flatIter()` / `flatIterMut()`
- **Serialization Ready**: Includes both a `kotlinx.serialization` `KSerializer` (`MultiMapSerializer`) and `serde-kotlin` `Serialize` / `Deserialize` adapters (`serializeMultiMap`, `multiMapDeserialize`, `MultiMapVisitor`).
- **Full Kotlin Multiplatform & Swift Export Support**: Compiles across JVM, Android, JavaScript, WebAssembly (WasmJS & WasmWASI), Linux, Windows, and Apple targets (macOS, iOS, tvOS, watchOS) with direct Swift Export and `XCFramework` packaging.

---

## Installation

`multimap-kotlin` is published to Maven Central under `io.github.kotlinmania:multimap-kotlin`.

### Gradle (Kotlin DSL — Multiplatform)

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlinmania:multimap-kotlin:0.1.4")
        }
    }
}
```

### Gradle (Kotlin DSL — JVM / Android)

```kotlin
dependencies {
    implementation("io.github.kotlinmania:multimap-kotlin:0.1.4")
}
```

### Gradle Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
multimap = "0.1.4"

[libraries]
multimap-kotlin = { module = "io.github.kotlinmania:multimap-kotlin", version.ref = "multimap" }
```

---

## Quick Start

```kotlin
import io.github.kotlinmania.multimap.MultiMap
import io.github.kotlinmania.multimap.multimapOf

fun main() {
    val map = MultiMap<String, Int>()

    map.insert("key1", 42)
    map.insert("key1", 1337)
    map.insert("key2", 2332)

    check(map["key1"] == 42)
    check(map.get("key1") == 42)
    check(map.getVec("key1") == listOf(42, 1337))
    check(map.isVec("key1"))
    check(!map.isVec("key2"))
}
```

---

## API Usage Guide

### Creating a `MultiMap`

Create an empty `MultiMap`, pre-allocate capacity, build one with `multimapOf`, or construct one from key-value iterables:

```kotlin
import io.github.kotlinmania.multimap.MultiMap
import io.github.kotlinmania.multimap.multimapOf

// Empty multimap
val empty = MultiMap<String, String>()

// Pre-allocated capacity
val withCap = MultiMap.withCapacity<String, Int>(20)

// Builder function
val breeds = multimapOf(
    "dog" to "husky",
    "dog" to "retriever",
    "dog" to "shiba inu",
    "cat" to "siamese",
)

// From an iterable of Pair<K, V>
val fromPairs = MultiMap.fromIter(
    listOf("a" to 1, "a" to 2, "b" to 3)
)

// From an iterable of Pair<K, List<V>>
val fromVectors = MultiMap.fromIteratorVec(
    listOf("a" to listOf(1, 2), "b" to listOf(3))
)
```

### Inserting & Extending Values

```kotlin
val map = MultiMap<String, Int>()

// Insert a single value for a key
map.insert("scores", 10)
map.insert("scores", 20)

// Insert multiple values from an Iterable<V>
map.insertMany("scores", listOf(30, 40))

// Insert multiple values from a Collection<V> slice
map.insertManyFromSlice("scores", listOf(50, 60))

// Extend with Pair<K, V> elements or another MultiMap
map.extend(listOf("bonus" to 5, "scores" to 70))

// Extend with Pair<K, List<V>> elements
map.extendVec(listOf("bonus" to listOf(10, 15)))
```

### Querying & Mutating Values

`MultiMap` distinguishes between accessing the **first** value associated with a key and accessing the **entire vector** of values for that key:

```kotlin
val map = multimapOf(
    "host" to "10.0.0.1",
    "host" to "10.0.0.2",
    "port" to "8080",
)

// Key presence and counts
check(map.containsKey("host"))
check(map.len() == 2)      // number of distinct keys
check(!map.isEmpty())

// First value for a key
val primaryHost: String? = map.get("host") // "10.0.0.1"
val indexedHost: String = map["host"]      // "10.0.0.1" (throws NoSuchElementException if missing)

// All values for a key
val allHosts: List<String>? = map.getVec("host") // ["10.0.0.1", "10.0.0.2"]

// Check whether a key has more than one value
check(map.isVec("host"))   // true (2 values)
check(!map.isVec("port"))  // false (1 value)

// Mutate the value list in place
map.getVecMut("host")?.add("10.0.0.3")

// Remove all values for a key and return them
val removedPorts: MutableList<String>? = map.remove("port") // ["8080"]

// Retain only matching (key, value) pairs; keys whose value lists become empty are removed automatically
map.retain { key, value -> key == "host" && value != "10.0.0.1" }
```

### Entry API (`Entry`, `OccupiedEntry`, `VacantEntry`)

Use `entry(key)` for in-place inspection and insertion without redundant hash lookups:

```kotlin
import io.github.kotlinmania.multimap.Entry
import io.github.kotlinmania.multimap.MultiMap

val map = MultiMap<String, Int>()

// Insert default first value if vacant, and return the mutable list of values remains accessible via orInsertVec
map.entry("counter").orInsert(1)
map.entry("counter").orInsertVec(mutableListOf(10, 20)).add(30)

check(map.getVec("counter") == listOf(1, 30))

// Pattern match on Entry.Occupied vs Entry.Vacant
when (val e = map.entry("counter")) {
    is Entry.Occupied -> {
        println("First value: ${e.entry.get()}")
        e.entry.getVecMut().add(40)
    }
    is Entry.Vacant -> {
        e.entry.insert(0)
    }
}
```

### Iteration Views

```kotlin
val map = multimapOf(
    "k1" to 10,
    "k1" to 20,
    "k2" to 30,
)

// 1. Iterate over keys and their FIRST value: Pair<K, V>
for ((key, firstValue) in map.iter()) {
    println("$key -> first: $firstValue")
}

// 2. Iterate over keys and their COMPLETE value list: Pair<K, List<V>>
for ((key, valueList) in map.iterAll()) {
    println("$key -> all: $valueList")
}

// 3. Iterate over every flattened (key, value) pair: Pair<K, V>
// Produces ("k1", 10), ("k1", 20), ("k2", 30)
for ((key, value) in map.flatIter()) {
    println("$key -> $value")
}

// 4. Direct for-in over MultiMap yields Pair<K, MutableList<V>>
for ((key, values) in map) {
    println("$key has ${values.size} value(s)")
}
```

### Serialization (`kotlinx.serialization` & `serde-kotlin`)

`MultiMap` is annotated with `@Serializable(with = MultiMapSerializer::class)` and serializes as a map from `K` to `List<V>`:

```kotlin
import io.github.kotlinmania.multimap.MultiMap
import io.github.kotlinmania.multimap.multimapOf
import kotlinx.serialization.Serializable

@Serializable
data class ServiceConfig(
    val routes: MultiMap<String, String> = multimapOf(),
)
```

For `serde-kotlin` token/format pipelines, use `serializeMultiMap`, `multiMapDeserialize`, and `MultiMapVisitor`.

---

## Supported Platforms

| Category | Targets |
|---|---|
| **JVM** | `jvm` (JDK 21 toolchain) |
| **Android** | Android KMP library (`compileSdk 34`, `minSdk 24`), `androidNativeArm64`, `androidNativeX64` |
| **Apple (macOS / iOS / tvOS / watchOS)** | `macosArm64`, `iosArm64`, `iosSimulatorArm64`, `iosX64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosArm64`, `watchosDeviceArm64`, `watchosSimulatorArm64` (includes Swift Export + `Multimap` `XCFramework`) |
| **Linux** | `linuxX64`, `linuxArm64` |
| **Windows** | `mingwX64` |
| **Web (JS & Wasm)** | `js` (Browser & Node.js), `wasmJs` (Browser & Node.js), `wasmWasi` (Node.js) |

---

## Building and Testing

```bash
# Run the full multiplatform test suite (JVM, Android host, macOS, JS, WasmJS, WasmWASI, and Swift Export)
./gradlew test

# Build all configured Kotlin Multiplatform targets, XCFramework, Swift Export package, Detekt, and Ktlint
./gradlew build
```

---

## License

Licensed under either of:

- **Apache License, Version 2.0** ([LICENSE](LICENSE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- **MIT License** (<https://opensource.org/licenses/MIT>)

at your option.

---

## Acknowledgments

This Kotlin Multiplatform library is a port of the original Rust [`multimap`](https://github.com/havarnov/multimap) crate ([crates.io/crates/multimap](https://crates.io/crates/multimap), [docs.rs/multimap](https://docs.rs/multimap)).

Special thanks to **[Håvar Nøvik (`@havarnov`)](https://github.com/havarnov)** and all contributors to [`havarnov/multimap`](https://github.com/havarnov/multimap) for designing and maintaining the original Rust implementation.

Kotlin Multiplatform port maintained by **Sydney Renee** and [The Solace Project](https://github.com/KotlinMania) for [KotlinMania](https://github.com/KotlinMania).
