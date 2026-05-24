# port-lint Proposed Changes

**Generated:** 2026-05-24
**Source:** tmp/multimap/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/multimap

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/multimap/Lib.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/multimap/LibTest.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/multimap/Serde.kt` | `// port-lint: source src/serde.rs` | `// port-lint: source serde.rs` | `serde.rs` | `port-lint provenance header matched only after fallback normalization: 'src/serde.rs' vs expected 'serde.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/multimap/SerdeTest.kt` | `// port-lint: source src/serde.rs` | `// port-lint: source serde.rs` | `serde.rs` | `port-lint provenance header matched only after fallback normalization: 'src/serde.rs' vs expected 'serde.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/multimap/Entry.kt` | `// port-lint: source src/entry.rs` | `// port-lint: source entry.rs` | `entry.rs` | `port-lint provenance header matched only after fallback normalization: 'src/entry.rs' vs expected 'entry.rs'` |
