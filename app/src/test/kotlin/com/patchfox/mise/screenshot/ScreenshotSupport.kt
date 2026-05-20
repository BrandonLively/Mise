package com.patchfox.mise.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.patchfox.mise.data.cookcard.CookCardParser
import com.patchfox.mise.domain.mapper.toDomain
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.ui.component.BottomNav
import com.patchfox.mise.ui.component.MiseNavRail
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.theme.DefaultMiseTextStyles
import com.patchfox.mise.ui.theme.MiseTextStyles
import com.patchfox.mise.ui.theme.MiseTheme
import com.patchfox.mise.ui.theme.MiseTokens

/** Robolectric device qualifiers — phone portrait and tablet landscape. */
const val PHONE_QUALIFIERS = "w393dp-h851dp-xxhdpi"
const val TABLET_QUALIFIERS = "w1280dp-h800dp-xhdpi"

/** Height reserved for the bottom navigation bar — matches MiseApp. */
private val BOTTOM_NAV_HEIGHT = 86.dp

/** Width of the tablet navigation rail — matches MiseApp. */
private val NAV_RAIL_WIDTH = 96.dp

private object ScreenshotResources

/**
 * Parses the bundled `sample-cook-card.json` test resource into a domain [CookCard].
 * Pure JVM (kotlinx-serialization + the domain mapper) — safe under Robolectric.
 */
fun loadSampleCookCard(): CookCard {
    val stream = checkNotNull(
        ScreenshotResources::class.java.getResourceAsStream("/sample-cook-card.json"),
    ) { "sample-cook-card.json not found on the test classpath" }
    val json = stream.bufferedReader().use { it.readText() }
    return CookCardParser.parse(json).toDomain()
}

/**
 * The app's text styles with system font families substituted for the Google
 * Downloadable Fonts (Instrument Serif / Geist / JetBrains Mono). Screenshot
 * tests have no Play Services, so downloadable fonts can't resolve —
 * serif/sans/mono keep the goldens stable and close to the real look.
 */
val ScreenshotTextStyles: MiseTextStyles = MiseTextStyles(
    display = DefaultMiseTextStyles.display.copy(fontFamily = FontFamily.Serif),
    h1 = DefaultMiseTextStyles.h1.copy(fontFamily = FontFamily.Serif),
    h2 = DefaultMiseTextStyles.h2.copy(fontFamily = FontFamily.Serif),
    h3 = DefaultMiseTextStyles.h3.copy(fontFamily = FontFamily.Serif),
    h4 = DefaultMiseTextStyles.h4.copy(fontFamily = FontFamily.Serif),
    body = DefaultMiseTextStyles.body.copy(fontFamily = FontFamily.SansSerif),
    bodyEmphasis = DefaultMiseTextStyles.bodyEmphasis.copy(fontFamily = FontFamily.SansSerif),
    small = DefaultMiseTextStyles.small.copy(fontFamily = FontFamily.SansSerif),
    micro = DefaultMiseTextStyles.micro.copy(fontFamily = FontFamily.Monospace),
    clock = DefaultMiseTextStyles.clock.copy(fontFamily = FontFamily.Monospace),
    clockLarge = DefaultMiseTextStyles.clockLarge.copy(fontFamily = FontFamily.Monospace),
)

private fun snapshot(name: String, content: @Composable () -> Unit) {
    captureRoboImage(filePath = "src/test/screenshots/$name.png") {
        MiseTheme(textStyles = ScreenshotTextStyles) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiseTokens.colors.bg),
            ) {
                content()
            }
        }
    }
}

/**
 * Renders a screen with no navigation chrome — for Login, which the running app
 * shows before sign-in, outside the bottom nav / rail.
 */
fun captureScreen(name: String, content: @Composable () -> Unit) {
    snapshot(name) { content() }
}

/**
 * Renders a screen with the phone bottom navigation bar, mirroring MiseApp:
 * the content is inset by the nav height and [BottomNav] is overlaid below.
 */
fun capturePhoneScreen(
    name: String,
    selected: MiseDestination,
    content: @Composable () -> Unit,
) {
    snapshot(name) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = BOTTOM_NAV_HEIGHT)) {
                content()
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomNav(selected = selected, onSelect = {})
            }
        }
    }
}

/**
 * Renders a screen with the tablet navigation rail on the leading edge,
 * mirroring MiseApp.
 */
fun captureTabletScreen(
    name: String,
    selected: MiseDestination,
    content: @Composable () -> Unit,
) {
    snapshot(name) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(NAV_RAIL_WIDTH).fillMaxSize()) {
                MiseNavRail(selected = selected, onSelect = {})
            }
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                content()
            }
        }
    }
}
