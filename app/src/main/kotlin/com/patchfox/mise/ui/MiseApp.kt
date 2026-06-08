package com.patchfox.mise.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.patchfox.mise.ui.component.BottomNav
import com.patchfox.mise.ui.component.MiseNavRail
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.nav.CookRoute
import com.patchfox.mise.ui.nav.HomeRoute
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.nav.MiseNavHost
import com.patchfox.mise.ui.nav.RecipeDetailRoute
import com.patchfox.mise.ui.nav.RecipeInstructionsRoute
import com.patchfox.mise.ui.nav.RecipesRoute
import com.patchfox.mise.ui.nav.SummaryRoute
import com.patchfox.mise.ui.screen.login.LoginScreen
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.window.WindowSize
import com.patchfox.mise.ui.window.miseSize

@Composable
fun MiseApp(
    windowSize: WindowSizeClass,
    initialIntent: Intent? = null,
    viewModel: MiseAppViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()

    // Login lives OUTSIDE the NavHost. Keeping it out of the graph means the
    // graph's start destination is always HomeRoute, so popUpTo(HomeRoute) and
    // saveState/restoreState reconstruct the back stack consistently. (When
    // LoginRoute was the graph start, post-sign-in navigation referenced a
    // start destination no longer on the stack and landed on the wrong tab.)
    if (!authState.signedIn) {
        LoginScreen(onSignedIn = { /* authState recomposes MiseApp into the app. */ })
        return
    }

    SignedInApp(windowSize = windowSize, viewModel = viewModel)
}

@Composable
private fun SignedInApp(
    windowSize: WindowSizeClass,
    viewModel: MiseAppViewModel,
) {
    val doneTimerAlerts by viewModel.doneTimerAlerts.collectAsState()
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val size = windowSize.miseSize

    val currentDestination: MiseDestination = backStackEntry?.destination?.route?.let { route ->
        when {
            route.startsWith(HomeRoute::class.qualifiedName ?: "") -> MiseDestination.Home
            route.startsWith(CookRoute::class.qualifiedName ?: "") -> MiseDestination.Cook
            route.startsWith(SummaryRoute::class.qualifiedName ?: "") -> MiseDestination.Summary
            route.startsWith(RecipesRoute::class.qualifiedName ?: "") -> MiseDestination.Recipes
            route.startsWith(RecipeDetailRoute::class.qualifiedName ?: "") -> MiseDestination.Recipes
            route.startsWith(RecipeInstructionsRoute::class.qualifiedName ?: "") -> MiseDestination.Recipes
            else -> MiseDestination.Home
        }
    } ?: MiseDestination.Home

    // When the soft keyboard is up, drop the bottom-nav chrome entirely. With
    // edge-to-edge the keyboard slides up FROM the window edge; keeping the
    // 86dp bottom-nav reservation would leave focused text fields floating
    // 86dp above the keyboard with dead space between.
    val imeVisible = WindowInsets.isImeVisible

    Box(modifier = Modifier.fillMaxSize().background(MiseTokens.colors.bg)) {
        if (size == WindowSize.Compact) {
            // Phone — bottom nav over single pane
            Box(modifier = Modifier.fillMaxSize()) {
                MiseNavHost(
                    navController = nav,
                    windowSize = size,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (imeVisible) 0.dp else 86.dp),
                )
                if (!imeVisible) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        BottomNav(
                            selected = currentDestination,
                            onSelect = { dest -> navigateTopLevel(nav, dest) },
                        )
                    }
                }
            }
        } else {
            // Tablet — nav rail + content
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(96.dp).fillMaxSize()) {
                    MiseNavRail(
                        selected = currentDestination,
                        onSelect = { dest -> navigateTopLevel(nav, dest) },
                    )
                }
                MiseNavHost(
                    navController = nav,
                    windowSize = size,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Off-Phase-2 done-timer alert. Driven by the combine of TimerController
        // and CookSessionState in MiseAppViewModel — empty list means no dialog.
        if (doneTimerAlerts.isNotEmpty()) {
            DoneTimerAlertDialog(
                alerts = doneTimerAlerts,
                onDismissAll = {
                    doneTimerAlerts.forEach { viewModel.dismissTimer(it.id) }
                },
                onExtendTimer = viewModel::extendTimer,
                onJumpToStep = { stepId ->
                    nav.navigate(
                        CookRoute(initialStepId = stepId, initialPhase = PhaseTab.Phase2.name),
                    ) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Composable
private fun DoneTimerAlertDialog(
    alerts: List<DoneTimerAlert>,
    onDismissAll: () -> Unit,
    onExtendTimer: (String) -> Unit,
    onJumpToStep: (String?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* Modal — alarm is sounding, user must explicitly act. */ },
        title = {
            Text(
                if (alerts.size == 1) "Timer done" else "${alerts.size} timers done",
                style = MiseTokens.text.h4,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { entry ->
                    DoneTimerRow(
                        entry = entry,
                        onExtend = { onExtendTimer(entry.id) },
                        onJumpToStep = { onJumpToStep(entry.stepId) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissAll) { Text("Dismiss all") }
        },
    )
}

@Composable
private fun DoneTimerRow(
    entry: DoneTimerAlert,
    onExtend: () -> Unit,
    onJumpToStep: () -> Unit,
) {
    // Whole row is tappable to jump back to the cook step; +1 min sits on the
    // trailing edge. Dismiss intentionally lives at the dialog footer only so
    // the user has to deliberately silence everything in one gesture.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJumpToStep() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = entry.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.accent
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(tint, CircleShape),
        )
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.padding(end = 8.dp)) {
            if (!entry.recipeEmoji.isNullOrBlank()) {
                Text(
                    "${entry.recipeEmoji} ${entry.title}",
                    style = MiseTokens.text.bodyEmphasis,
                    color = MiseTokens.colors.ink,
                )
            } else {
                Text(entry.title, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
            }
            Text(
                "Tap to view step",
                style = MiseTokens.text.micro,
                color = MiseTokens.colors.ink3,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onExtend) { Text("+1 min") }
    }
}

/**
 * Switches between top-level bottom-nav destinations. `internal` so the
 * navigation behavior can be exercised directly from instrumented tests.
 *
 * Deterministic by design: pop everything above Home, then show the target tab,
 * so the back stack always ends as `[Home]` or `[Home, dest]` regardless of how
 * it was built. `saveState` / `restoreState` were intentionally removed — they
 * rely on every tab navigation going through this one call, but the app also
 * reaches Cook via plain `navigate(CookRoute(initialPhase = ...))` from content
 * (cook-phase rows, "Walk the plan"). Those plain navigations corrupted the
 * save/restore bookkeeping, so tapping Schedule left a stale Cook entry on top.
 */
internal fun navigateTopLevel(
    nav: NavController,
    dest: MiseDestination,
) {
    nav.navigate(dest.route()) {
        popUpTo(HomeRoute)
        launchSingleTop = true
    }
}
