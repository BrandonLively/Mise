package com.patchfox.mise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFont_Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.patchfox.mise.R

private val gfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val InstrumentSerifGf = GoogleFont("Instrument Serif")
private val GeistGf = GoogleFont("Geist")
private val JetBrainsMonoGf = GoogleFont("JetBrains Mono")

val InstrumentSerifFamily = FontFamily(
    GoogleFont_Font(googleFont = InstrumentSerifGf, fontProvider = gfProvider, weight = FontWeight.Normal),
    GoogleFont_Font(googleFont = InstrumentSerifGf, fontProvider = gfProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
)

val GeistFamily = FontFamily(
    GoogleFont_Font(googleFont = GeistGf, fontProvider = gfProvider, weight = FontWeight.Normal),
    GoogleFont_Font(googleFont = GeistGf, fontProvider = gfProvider, weight = FontWeight.Medium),
    GoogleFont_Font(googleFont = GeistGf, fontProvider = gfProvider, weight = FontWeight.SemiBold),
    GoogleFont_Font(googleFont = GeistGf, fontProvider = gfProvider, weight = FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
    GoogleFont_Font(googleFont = JetBrainsMonoGf, fontProvider = gfProvider, weight = FontWeight.Normal),
    GoogleFont_Font(googleFont = JetBrainsMonoGf, fontProvider = gfProvider, weight = FontWeight.Medium),
    GoogleFont_Font(googleFont = JetBrainsMonoGf, fontProvider = gfProvider, weight = FontWeight.SemiBold),
)

@Immutable
data class MiseTextStyles(
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val body: TextStyle,
    val bodyEmphasis: TextStyle,
    val small: TextStyle,
    val micro: TextStyle,
    val clock: TextStyle,
    val clockLarge: TextStyle,
)

val DefaultMiseTextStyles = MiseTextStyles(
    display = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 60.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.025).em,
    ),
    h1 = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.02).em,
    ),
    h2 = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.015).em,
    ),
    h3 = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.01).em,
    ),
    h4 = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    body = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyEmphasis = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    small = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    micro = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.06.em,
    ),
    clock = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 18.sp,
    ),
    clockLarge = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
)

val LocalMiseTextStyles = staticCompositionLocalOf { DefaultMiseTextStyles }

internal fun buildMaterial3Typography(s: MiseTextStyles): Typography = Typography(
    displayLarge = s.display,
    displayMedium = s.display.copy(fontSize = 48.sp, lineHeight = 50.sp),
    displaySmall = s.h1,
    headlineLarge = s.h1,
    headlineMedium = s.h2,
    headlineSmall = s.h3,
    titleLarge = s.h4,
    titleMedium = s.bodyEmphasis,
    titleSmall = s.bodyEmphasis.copy(fontSize = 14.sp),
    bodyLarge = s.body,
    bodyMedium = s.body,
    bodySmall = s.small,
    labelLarge = s.bodyEmphasis,
    labelMedium = s.small,
    labelSmall = s.micro,
)
