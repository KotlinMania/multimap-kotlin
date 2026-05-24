# multimap-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fmultimap--kotlin-blue.svg)](https://github.com/KotlinMania/multimap-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/multimap-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/multimap-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/multimap-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/multimap-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`havarnov/multimap`](https://github.com/havarnov/multimap).

**Original Project:** This port is based on [`havarnov/multimap`](https://github.com/havarnov/multimap). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `havarnov/multimap`

> The text below is reproduced and lightly edited from [`https://github.com/havarnov/multimap`](https://github.com/havarnov/multimap). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

[![crates.io](https://img.shields.io/crates/v/multimap.svg)](https://crates.io/crates/multimap)
[![docs.rs](https://docs.rs/multimap/badge.svg)](https://docs.rs/multimap)

## Multimap implementation for Rust

This is a multimap implementation for Rust. Implemented as a thin wrapper around
`std::collections::HashMap`.

## Example

````rust
extern crate multimap;

use multimap::MultiMap;

fn main () {
    let mut map = MultiMap::new();

    map.insert("key1", 42);
    map.insert("key1", 1337);
    map.insert("key2", 2332);

    assert_eq!(map["key1"], 42);
    assert_eq!(map.get("key1"), Some(&42));
    assert_eq!(map.get_vec("key1"), Some(&vec![42, 1337]));
}
````

## Changelog

### 0.10.1

* Fix docs for flat_iter #45
* Clippy warnings

### 0.10.0

* Added `FromIterator<(K, Vec<V>)>` [#48](https://github.com/havarnov/multimap/pull/48).

### 0.9.1

* Fixes a bug where iteration would panic on empty (inner) vectors [#46](https://github.com/havarnov/multimap/issues/46).

### 0.9.0

* Added ```flat_iter``` and ```flat_iter_mut```
* Fixed bug where ```get``` and ```get_mut``` could panic.

### 0.8.3

* `multimap!` macro fixes; allow trailing comma, naming hygiene and create with
  enough capacity for all elements.

### 0.8.2

* Added ```#![forbid(unsafe_code)]```.

### 0.8.1

* Fixed wrong link to documentation in `Cargo.toml`.

### 0.8.0

* Added ```MultiMap::insert_many```.
* Added ```MultiMap::insert_many_from_slice```.

### 0.7.0

* Added possibility to replace the default hasher for the underlying
  ```HashMap```.
* Fix build warning by removing an unnecessary ```mut```.

## License

Licensed under either of
 * Apache License, Version 2.0 ([LICENSE-APACHE](https://github.com/havarnov/multimap/blob/HEAD/LICENSE-APACHE) or
   https://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](https://github.com/havarnov/multimap/blob/HEAD/LICENSE-MIT) or
   https://opensource.org/licenses/MIT)
at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:multimap-kotlin:0.1.2")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`havarnov/multimap`](https://github.com/havarnov/multimap). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the multimap authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`havarnov/multimap`](https://github.com/havarnov/multimap) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
