package com.patchfox.mise

import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.patchfox.mise.ui.nav.CookRoute
import com.patchfox.mise.ui.nav.HomeRoute
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.nav.RecipeDetailRoute
import com.patchfox.mise.ui.nav.RecipesRoute
import com.patchfox.mise.ui.nav.SummaryRoute
import com.patchfox.mise.ui.navigateTopLevel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the recurring bug where tapping the "Schedule" item in the
 * bottom navigation bar fails to return to the Schedule (Home) screen.
 *
 * Drives the real route graph and the real [navigateTopLevel] logic — the exact
 * function the Schedule button invokes via `BottomNav.onSelect` — against a
 * [TestNavHostController]. No Compose UI rule / Espresso, so it is unaffected by
 * the InputManager incompatibility on API 36 devices.
 */
@RunWith(AndroidJUnit4::class)
class BottomNavScheduleNavigationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun newNavController(): TestNavHostController {
        lateinit var nav: TestNavHostController
        instrumentation.runOnMainSync {
            nav = TestNavHostController(instrumentation.targetContext).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = HomeRoute) {
                    composable<HomeRoute> {}
                    composable<CookRoute> {}
                    composable<SummaryRoute> {}
                    composable<RecipesRoute> {}
                    composable<RecipeDetailRoute> {}
                }
            }
        }
        return nav
    }

    /** Performs a bottom-nav tab switch, exactly as `BottomNav.onSelect` does. */
    private fun TestNavHostController.tapTab(dest: MiseDestination) {
        instrumentation.runOnMainSync { navigateTopLevel(this, dest) }
    }

    private fun TestNavHostController.currentTab(): MiseDestination? {
        var found: MiseDestination? = null
        instrumentation.runOnMainSync {
            val route = currentDestination?.route
            found = when {
                route == null -> null
                route.startsWith(HomeRoute::class.qualifiedName!!) -> MiseDestination.Home
                route.startsWith(CookRoute::class.qualifiedName!!) -> MiseDestination.Cook
                route.startsWith(SummaryRoute::class.qualifiedName!!) -> MiseDestination.Summary
                route.startsWith(RecipesRoute::class.qualifiedName!!) -> MiseDestination.Recipes
                else -> null
            }
        }
        return found
    }

    @Test
    fun scheduleTab_afterVisitingCook_returnsToScheduleScreen() {
        val nav = newNavController()
        assertEquals(MiseDestination.Home, nav.currentTab())

        nav.tapTab(MiseDestination.Cook)
        assertEquals(MiseDestination.Cook, nav.currentTab())

        nav.tapTab(MiseDestination.Home)
        assertEquals(MiseDestination.Home, nav.currentTab())
    }

    @Test
    fun scheduleTab_afterVisitingRecipesViaCook_returnsToScheduleScreen() {
        val nav = newNavController()

        nav.tapTab(MiseDestination.Cook)
        nav.tapTab(MiseDestination.Recipes)
        assertEquals(MiseDestination.Recipes, nav.currentTab())

        nav.tapTab(MiseDestination.Home)
        assertEquals(MiseDestination.Home, nav.currentTab())
    }

    @Test
    fun scheduleTab_repeatedTabSwitches_alwaysLandsOnScheduleScreen() {
        val nav = newNavController()

        nav.tapTab(MiseDestination.Cook)
        nav.tapTab(MiseDestination.Home)
        nav.tapTab(MiseDestination.Recipes)
        nav.tapTab(MiseDestination.Home)
        nav.tapTab(MiseDestination.Summary)
        nav.tapTab(MiseDestination.Home)
        assertEquals(MiseDestination.Home, nav.currentTab())
    }

    /** Revisiting a tab triggers restoreState — the most likely source of a misroute. */
    @Test
    fun scheduleTab_afterRevisitingCookTwice_returnsToScheduleScreen() {
        val nav = newNavController()

        nav.tapTab(MiseDestination.Cook)
        nav.tapTab(MiseDestination.Home)
        nav.tapTab(MiseDestination.Cook) // restoreState restores the saved Cook entry
        nav.tapTab(MiseDestination.Home)
        assertEquals(MiseDestination.Home, nav.currentTab())
    }

    /**
     * The app also reaches Cook via plain `navigate(CookRoute(initialPhase = ...))`
     * from content (cook-phase rows, "walk the plan"). Tapping Schedule afterwards
     * must still return to the Schedule screen.
     */
    @Test
    fun scheduleTab_afterDeepNavigationToCookWithArgs_returnsToScheduleScreen() {
        val nav = newNavController()

        instrumentation.runOnMainSync {
            nav.navigate(CookRoute(initialPhase = "Phase1"))
        }
        assertEquals(MiseDestination.Cook, nav.currentTab())

        nav.tapTab(MiseDestination.Home)
        assertEquals(MiseDestination.Home, nav.currentTab())
    }
}
