# cook-card v3 — schema + samples (Cook redesign refactor reference)

The data shapes the app reads from Firestore (`cookCards` collection). Point the v3 DTO/parser work at these.

## Files
| File | What it is |
|---|---|
| **`cook-card.v3.schema.json`** | The JSON Schema (Draft 2020-12) every card conforms to — the authoritative contract. |
| **`cook-card-v3-sample-minimal.json`** | Clean 2-recipe example, hand-authored to demonstrate the cross-recipe **mise merge**. Start here. |
| **`cook-card-v3-sample-caribbean.json`** | A real 4-recipe card (the live `2026-06-21-caribbean-jerk` doc) — realistic data to test the parser. |

## What each sample demonstrates
- **minimal** — `lime` (preparation `juiced`) appears in *both* recipes and must merge by `(ingredientId, unit, preparation)`; `allspice` merges (2 tsp + 2 tsp → 4 tsp); `salt` is `combinable:false` and stays per-recipe. The smallest thing that proves the merge logic.
- **caribbean** — exercises everything: three recipes share a `white-rice` `backgroundable` cook step (shared-cook derivation), share `lime`/`cilantro`/`allspice` mise, and the dessert (coconut rice pudding) is **no-cook** → a single assemble/chill `cookPhase` step (the "trivial lane" case).

## Shape recap (full detail in the two handoff HTML docs)
- Top level: `meta` + `recipes[]`. **Each recipe is self-contained** — no shared `cookPlan`.
- `recipe.misePhase = { durationMinutes?, bowls[], standalones[] }`
- `recipe.cookPhase = { durationMinutes?, steps[] }` — the **first** step(s) are `backgroundable:true` (the old Phase 0).
- `recipe.prepSchedule[]` — do-ahead reminders (marinade, **thaw**, soak) with RELATIVE offsets (`night-before`, `T-36h`). ⚠️ v3 moved these from the v2 top-level `schedule` to **per-recipe** — so the **Schedule/Home** view now aggregates `recipes[].prepSchedule` instead of reading a top-level `schedule`. This is the one v3 data change *outside* the Cook tab.
- Every mise/cook ingredient carries a canonical `ingredientId`. **Shared lanes are app-generated**: mise = merge `bowls[].ingredients` on `(ingredientId, unit, preparation)`; cook = group `backgroundable` steps sharing `ingredientId`+`preparation`. There is **no** `sharedWith` / array `forRecipeId` — by design (enables custom cross-week cook plans).
- `recipe.perServing` = Σ `macroBreakdown.ingredients[]` (derived). Daily totals = Σ recipes' `perServing` (the consumer computes; not stored).
- Display titles ("Mise" / "Cook") and the per-phase ordinal are **client-side constants** — not in the card.
- **Bowl + cook-step ids are recipe-namespaced and globally unique within a card** (`{recipeId}-b{n}` / `{recipeId}-s{n}`). So the app can key per-bowl/step state on the id alone — *including a future multi-card custom plan of distinct recipes* — with no composite key needed. (`validate.py` hard-fails any duplicate bowl/step id in a card.)

> The authoritative prose contract is the **cook-card-toolchain consumer contract** (meal-prep skill repo → `cook-card-toolchain/SKILL.md`). Both samples validate against the schema here (0 errors).
