package com.patchfox.mise.ui.nav

import kotlinx.serialization.Serializable

@Serializable sealed interface MiseRoute

@Serializable data object HomeRoute : MiseRoute
@Serializable data class CookRoute(
    val initialStepId: String? = null,
    /** "MISE" or "COOK" (CookStage.name); null leaves the current selection. */
    val initialPhase: String? = null,
) : MiseRoute
@Serializable data object RecipesRoute : MiseRoute

/** [cookCardId] null = this week's current card; non-null = a specific past card. */
@Serializable data class RecipeDetailRoute(
    val recipeId: String,
    val cookCardId: String? = null,
) : MiseRoute

@Serializable data class RecipeInstructionsRoute(
    val recipeId: String,
    val cookCardId: String? = null,
) : MiseRoute

/** Logical top-level destinations the bottom nav / rail switches between. */
enum class MiseDestination(val label: String) {
    Home("Schedule"),
    Cook("Cook"),
    Recipes("Recipes");

    fun matches(other: MiseDestination): Boolean = this == other

    fun route(): MiseRoute = when (this) {
        Home -> HomeRoute
        Cook -> CookRoute()
        Recipes -> RecipesRoute
    }
}
