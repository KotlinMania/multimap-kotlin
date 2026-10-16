# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/3 (66.7%)
- **Function parity:** 20/21 matched (target 40) — 95.2%
- **Class/type parity:** 3/5 matched (target 7) — 60.0%
- **Combined symbol parity:** 23/26 matched (target 47) — 88.5%
- **Average inline-code cosine:** 0.20 (function body across 2 matched files)
- **Average documentation cosine:** 1.00 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. serde

- **Target:** `multimap.Serde`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 31007.7
- **Functions:** 7/8 matched (target 27)
- **Missing functions:** `new`
- **Types:** 0/2 matched
- **Missing types:** `MultiMapVisitor`, `Value`
- **Tests:** 3/3 matched

### 2. entry

- **Target:** `multimap.Entry`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 1608.2
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

