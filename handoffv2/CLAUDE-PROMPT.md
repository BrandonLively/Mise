# Kickoff prompt — Cook tab v3 refactor

> Copy everything below the line into a fresh Claude Code session **started in the `Mise` project root** (`C:\Users\Brand\StudioProjects\Mise`). It has the codebase live; this brief supplies the v3 context.

---

You're refactoring the **Cook tab** of this Kotlin / Jetpack Compose app to the new **cook-card v3** data schema and the per-recipe-lanes redesign. Everything you need is in `handoffv2/`.

## Read first, in this order
1. `handoffv2/cook-card-v3-README.md` — orientation + the shape recap.
2. `handoffv2/Cook Redesign - Design Delta.html` — the what-and-why (UI model: lanes, Mise/Cook toggle, command bar, shared lane).
3. `handoffv2/Cook Redesign - Android Handoff.html` — the how (composables, ViewModel/state, tests, acceptance criteria).
4. `handoffv2/cook-card.v3.schema.json` + the two `cook-card-v3-sample-*.json` — the exact data contract + real/minimal examples (both validate against the schema).
5. `handoffv2/reference-kotlin/` — **starting-point Kotlin you adapt**: `CookCardV3Dto.kt` (parse layer), `CookGuides.kt` (domain + the two `BuildGuides` use-cases with the merge logic), `CookGuidesTest.kt` (executable spec for the merge). These are pure reference (not wired in); reconcile types/packages with the app's actual layout.

## The essential v3 facts (don't skip)
- **Each recipe is self-contained**: `recipe.misePhase` (`bowls` + `standalones`) and `recipe.cookPhase` (`steps`). There is **no** top-level `cookPlan`, no `phase-0/1/2`, no `schedule`/`macroReconciliation`/`household`/`substitutions`. The card is `meta` + `recipes[]`.
- **Phase 0 is gone** — long-running kickoffs are the leading `cookPhase.steps` flagged **`backgroundable: true`** (each pairs with a `timer`). Surface them first.
- **Shared lanes are APP-GENERATED** from the recipes in the current plan — there is no server-authored shared data:
  - **Mise**: merge `misePhase.bowls[].ingredients` across recipes by **`(ingredientId, unit, preparation)`**; `combinable: false` never pools.
  - **Cook**: group `backgroundable` steps that share an ingredient **signature** (same set of `(ingredientId, preparation)`) — so plain rice hoists, coconut "rice & peas" does not.
  - This keys off canonical `ingredientId`, which is what makes **custom local cook plans** (recipes mixed from any week) work. **Do NOT add `sharedWith` or rely on an array `forRecipeId`** — they don't exist in v3, on purpose. (Earlier handoff drafts said to add them; that guidance was corrected in the HTML — ignore any residual mention except where it says "do not".)
- **Derived, not stored**: `recipe.perServing` = Σ `macroBreakdown.ingredients[]`; daily totals = Σ recipes' `perServing` (compute consumer-side). Phase titles ("Mise"/"Cook") and ordinals are client-side constants.

## Hard cutover — no back-compat needed
- The app is **single-user (just the owner) and not live**; a recent cook is done. **Breakage is fine.** Do a clean v3 cutover — **do not** branch on `schemaVersion` or keep v2 paths.
- The current `data/dto/CookCardDto.kt` **will fail to parse v3** (it requires `cookPlan`, `schedule`, `meta.household`, `meta.dailyActual`, `recipe.ingredients` — all removed in v3). Replace the cook-card DTOs with the v3 set, update the mapper (`domain/mapper/CookCardMappers.kt`), and delete the now-dead v2 cook DTOs.
- All 6 Firestore `cookCards` docs are already v3 (the app reads them via `FirestoreCookCardSource`, ordered by `uploadedAt`). `google-services.json` is in place.

## Scope
- **Replace**: the Cook-tab UI + `CookViewModel`, the cook-card DTOs + mapper for the cook/mise shapes, and add the two `domain/usecase/` `BuildGuides` use-cases (mise merge + cook hoist).
- **Keep**: theme/tokens/type, Nav3 graph, Hilt modules, Firebase auth + sync, Login, Home/Schedule, Summary, Recipe drill-down, and the timer infrastructure (AlarmManager/foreground service) — only re-home running timers into the command bar per the handoff.
- Follow the handoff's composable tree, command-bar/lane specs, and **acceptance criteria**; port the reference unit tests (`CookGuidesTest`) and add the Compose/ViewModel tests the handoff lists.

## Workflow
- Build + test in place: `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` (plus the Compose tests). Iterate on real errors.
- Start by getting **parse + the two use-cases** green against the sample JSONs, then build the UI on top.

## When the v3 intent is unclear
The authoritative prose is the **cook-card-toolchain consumer contract** (in the meal-prep skills repo: `~/.claude/skills/meal-prep-shop/cook-card-toolchain/SKILL.md`). If something's still ambiguous, ask the user — they can consult the session that authored v3.

Deliver: a compiling app whose Cook tab renders per-recipe lanes + app-derived shared lanes for both Mise and Cook, parsing the live v3 cards, with the listed tests passing.
