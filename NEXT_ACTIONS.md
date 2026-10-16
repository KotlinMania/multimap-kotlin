# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/3 (100.0%)
- **Function parity:** 116/122 matched (target 130) — 95.1%
- **Class/type parity:** 7/11 matched (target 11) — 63.6%
- **Combined symbol parity:** 123/133 matched (target 141) — 92.5%
- **Average inline-code cosine:** 0.26 (function body across 3 matched files)
- **Average documentation cosine:** 0.67 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `multimap.Mod`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 100706.9
- **Functions:** 95/101 matched (target 108)
- **Missing functions:** `from_iter`, `into_iter`, `into_iter`, `extend`, `extend`, `extend`
- **Types:** 3/6 matched (target 4)
- **Missing types:** `Output`, `Item`, `IntoIter`
- **Tests:** 55/55 matched

### 2. serde

- **Target:** `multimap.Serde`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 11007.1
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Value`
- **Tests:** 3/3 matched

### 3. entry

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

