# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/3 (100.0%)
- **Function parity:** 85/95 matched (target 115) — 89.5%
- **Class/type parity:** 6/11 matched (target 11) — 54.5%
- **Combined symbol parity:** 91/106 matched (target 126) — 85.8%
- **Average inline-code cosine:** 0.00 (function body across 2 matched files)
- **Average documentation cosine:** 0.64 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 3
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `multimap.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 108210.0
- **Functions:** 69/76 matched (target 96)
- **Missing functions:** `with_hasher`, `with_capacity_and_hasher`, `fmt`, `eq`, `from_iter`, `into_iter`, `size_hint`
- **Types:** 3/6 matched (target 4)
- **Missing types:** `Output`, `Item`, `IntoIter`
- **Tests:** 40/40 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 2

### 2. serde

- **Target:** `multimap.Serde [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51010.0
- **Functions:** 5/8 matched (target 6)
- **Missing functions:** `new`, `expecting`, `visit_map`
- **Types:** 0/2 matched
- **Missing types:** `MultiMapVisitor`, `Value`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/serde.rs` vs expected `serde.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/serde.rs` vs expected `serde.rs`
- **Proposed provenance header:** `// port-lint: source serde.rs` (current: `// port-lint: source src/serde.rs`)
- **Proposed provenance header:** `// port-lint: source serde.rs` (current: `// port-lint: source src/serde.rs`)
- **Lint issues:** 2

### 3. entry

- **Target:** `multimap.Entry [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/entry.rs` vs expected `entry.rs`
- **Proposed provenance header:** `// port-lint: source entry.rs` (current: `// port-lint: source src/entry.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

