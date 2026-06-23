package com.patchfox.mise.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.patchfox.mise.ui.screen.cook.CookScreen
import com.patchfox.mise.ui.screen.home.HomeScreen
import com.patchfox.mise.ui.screen.recipe.RecipeDetailScreen
import com.patchfox.mise.ui.screen.recipe.RecipeInstructionsScreen
import com.patchfox.mise.ui.screen.recipe.RecipesScreen
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.window.WindowSize

/**
 * Authenticated-app navigation graph. Login is handled outside the NavHost
 * (see [com.patchfox.mise.ui.MiseApp]) so the graph's start destination is
 * always [HomeRoute] — keeping popUpTo / saveState / restoreState consistent.
 */
@Composable
fun MiseNavHost(
    navController: NavHostController,
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                windowSize = windowSize,
                onWalkPlan = { navController.navigate(CookRoute(initialPhase = CookStage.COOK.name)) },
                onOpenStage = { stage ->
                    navController.navigate(CookRoute(initialPhase = stage.name))
                },
                onOpenRecipe = { id -> navController.navigate(RecipeDetailRoute(id.value)) },
            )
        }
        composable<CookRoute> { backStackEntry ->
            val route: CookRoute = backStackEntry.toRoute()
            CookScreen(
                windowSize = windowSize,
                initialStepId = route.initialStepId,
                initialStage = route.initialPhase?.let { name ->
                    runCatching { CookStage.valueOf(name) }.getOrNull()
                },
            )
        }
        composable<RecipesRoute> {
            RecipesScreen(
                windowSize = windowSize,
                onOpenRecipe = { cookCardId, id ->
                    navController.navigate(RecipeDetailRoute(id.value, cookCardId))
                },
            )
        }
        composable<RecipeDetailRoute> { backStackEntry ->
            val route: RecipeDetailRoute = backStackEntry.toRoute()
            RecipeDetailScreen(
                recipeId = route.recipeId,
                windowSize = windowSize,
                onBack = { navController.popBackStack() },
                onOpenInstructions = { recipeId ->
                    navController.navigate(RecipeInstructionsRoute(recipeId, route.cookCardId))
                },
            )
        }
        composable<RecipeInstructionsRoute> { backStackEntry ->
            val route: RecipeInstructionsRoute = backStackEntry.toRoute()
            RecipeInstructionsScreen(
                recipeId = route.recipeId,
                windowSize = windowSize,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
