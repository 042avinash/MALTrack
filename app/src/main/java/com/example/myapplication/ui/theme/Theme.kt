package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.local.ThemePalette
import com.example.myapplication.data.local.ThemeCustomColors

private val DefaultDarkColors = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DefaultLightColors = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val AnimeNightDark = darkColorScheme(
    primary = Color(0xFFD2C1FF), onPrimary = Color(0xFF34205D),
    primaryContainer = Color(0xFF4A3676), onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFFFFB2A3), onSecondary = Color(0xFF641D16),
    secondaryContainer = Color(0xFF80332A), onSecondaryContainer = Color(0xFFFFDAD3),
    tertiary = Color(0xFFFFB68D), onTertiary = Color(0xFF512300),
    background = Color(0xFF14121B), onBackground = Color(0xFFE8E0F0),
    surface = Color(0xFF14121B), onSurface = Color(0xFFE8E0F0),
    surfaceVariant = Color(0xFF494551), onSurfaceVariant = Color(0xFFCAC4D2)
)
private val AnimeNightLight = lightColorScheme(
    primary = Color(0xFF59428C), onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF), onPrimaryContainer = Color(0xFF211047),
    secondary = Color(0xFFB64E42), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD3), onSecondaryContainer = Color(0xFF410004),
    tertiary = Color(0xFF8C4F20), onTertiary = Color.White,
    background = Color(0xFFFCF8FF), onBackground = Color(0xFF1C1B22),
    surface = Color(0xFFFCF8FF), onSurface = Color(0xFF1C1B22),
    surfaceVariant = Color(0xFFE8E0EC), onSurfaceVariant = Color(0xFF49454F)
)

private val MangaPaperDark = darkColorScheme(
    primary = Color(0xFFFFB6A5), onPrimary = Color(0xFF5D1A0D),
    primaryContainer = Color(0xFF7D3020), onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFD7C3A5), onSecondary = Color(0xFF392F20),
    secondaryContainer = Color(0xFF514633), onSecondaryContainer = Color(0xFFF4E0C1),
    tertiary = Color(0xFFB9D0A8), onTertiary = Color(0xFF25351D),
    background = Color(0xFF1B1916), onBackground = Color(0xFFE9E1D8),
    surface = Color(0xFF1B1916), onSurface = Color(0xFFE9E1D8),
    surfaceVariant = Color(0xFF4B4640), onSurfaceVariant = Color(0xFFCEC5BA)
)
private val MangaPaperLight = lightColorScheme(
    primary = Color(0xFF854331), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD0), onPrimaryContainer = Color(0xFF391A10),
    secondary = Color(0xFF715B3D), onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E0BC), onSecondaryContainer = Color(0xFF281A05),
    tertiary = Color(0xFF526849), onTertiary = Color.White,
    background = Color(0xFFFFF8F0), onBackground = Color(0xFF211A16),
    surface = Color(0xFFFFF8F0), onSurface = Color(0xFF211A16),
    surfaceVariant = Color(0xFFF1E4D9), onSurfaceVariant = Color(0xFF51453D)
)

private val MalInspiredDark = darkColorScheme(
    primary = Color(0xFF77D5DE), onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF005057), onPrimaryContainer = Color(0xFF9CF1FA),
    secondary = Color(0xFF77D5DE), onSecondary = Color(0xFF00363B),
    secondaryContainer = Color(0xFF005057), onSecondaryContainer = Color(0xFF9CF1FA),
    tertiary = Color(0xFFFFD27D), onTertiary = Color(0xFF402D00),
    background = Color(0xFF101820), onBackground = Color(0xFFDCE3EC),
    surface = Color(0xFF101820), onSurface = Color(0xFFDCE3EC),
    surfaceVariant = Color(0xFF414A54), onSurfaceVariant = Color(0xFFC1CAD5)
)
private val MalInspiredLight = lightColorScheme(
    primary = Color(0xFF006A70), onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF1FA), onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFF006970), onSecondary = Color.White,
    secondaryContainer = Color(0xFF9CF1FA), onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFF775A00), onTertiary = Color.White,
    background = Color(0xFFF7F9FF), onBackground = Color(0xFF171C22),
    surface = Color(0xFFF7F9FF), onSurface = Color(0xFF171C22),
    surfaceVariant = Color(0xFFDDE3ED), onSurfaceVariant = Color(0xFF414750)
)

private val GraphiteDark = darkColorScheme(
    primary = Color(0xFFD0D0D6), onPrimary = Color(0xFF2C2C30),
    primaryContainer = Color(0xFF45454A), onPrimaryContainer = Color(0xFFF4F4F5),
    secondary = Color(0xFF9DD8CA), onSecondary = Color(0xFF12382F),
    secondaryContainer = Color(0xFF2B5046), onSecondaryContainer = Color(0xFFB9F4E5),
    tertiary = Color(0xFFFFB2C1), onTertiary = Color(0xFF5F112B),
    background = Color(0xFF19171C), onBackground = Color(0xFFE9E0E9),
    surface = Color(0xFF19171C), onSurface = Color(0xFFE9E0E9),
    surfaceVariant = Color(0xFF49434C), onSurfaceVariant = Color(0xFFCBC3CD)
)
private val GraphiteLight = lightColorScheme(
    primary = Color(0xFF52525B), onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E4E7), onPrimaryContainer = Color(0xFF18181B),
    secondary = Color(0xFF35695B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F4E5), onSecondaryContainer = Color(0xFF002117),
    tertiary = Color(0xFF9A4057), onTertiary = Color.White,
    background = Color(0xFFFFF7FB), onBackground = Color(0xFF201A21),
    surface = Color(0xFFFFF7FB), onSurface = Color(0xFF201A21),
    surfaceVariant = Color(0xFFECE1EB), onSurfaceVariant = Color(0xFF4B454E)
)

private val WinterDark = darkColorScheme(
    primary = Color(0xFF9CCBFA),
    onPrimary = Color(0xFF102F4D),
    primaryContainer = Color(0xFF294969),
    onPrimaryContainer = Color(0xFFE1F1FF),
    secondary = Color(0xFF9CCBFA),
    onSecondary = Color(0xFF102F4D),
    secondaryContainer = Color(0xFF294969),
    onSecondaryContainer = Color(0xFFE1F1FF),
    tertiary = Color(0xFF9CCBFA),
    onTertiary = Color(0xFF102F4D),
    tertiaryContainer = Color(0xFF294969),
    onTertiaryContainer = Color(0xFFE1F1FF)
)
private val WinterLight = lightColorScheme(
    primary = Color(0xFF315B8A),
    primaryContainer = Color(0xFFDCEBFA),
    onPrimaryContainer = Color(0xFF142E49),
    secondary = Color(0xFF315B8A),
    secondaryContainer = Color(0xFFDCEBFA),
    onSecondaryContainer = Color(0xFF142E49),
    tertiary = Color(0xFF315B8A),
    tertiaryContainer = Color(0xFFDCEBFA),
    onTertiaryContainer = Color(0xFF142E49)
)
private val SpringDark = darkColorScheme(
    primary = Color(0xFFF3A9C4),
    onPrimary = Color(0xFF52233A),
    primaryContainer = Color(0xFF60364A),
    onPrimaryContainer = Color(0xFFFFE3ED),
    secondary = Color(0xFFF3A9C4),
    onSecondary = Color(0xFF52233A),
    secondaryContainer = Color(0xFF60364A),
    onSecondaryContainer = Color(0xFFFFE3ED),
    tertiary = Color(0xFFF3A9C4),
    onTertiary = Color(0xFF52233A),
    tertiaryContainer = Color(0xFF60364A),
    onTertiaryContainer = Color(0xFFFFE3ED)
)
private val SpringLight = lightColorScheme(
    primary = Color(0xFFAD4F75),
    primaryContainer = Color(0xFFF8DDE8),
    onPrimaryContainer = Color(0xFF4D1930),
    secondary = Color(0xFFAD4F75),
    secondaryContainer = Color(0xFFF8DDE8),
    onSecondaryContainer = Color(0xFF4D1930),
    tertiary = Color(0xFFAD4F75),
    tertiaryContainer = Color(0xFFF8DDE8),
    onTertiaryContainer = Color(0xFF4D1930)
)
private val SummerDark = darkColorScheme(
    primary = Color(0xFFA7D8A7),
    onPrimary = Color(0xFF173B25),
    primaryContainer = Color(0xFF2C5038),
    onPrimaryContainer = Color(0xFFE0F4DF),
    secondary = Color(0xFFA7D8A7),
    onSecondary = Color(0xFF173B25),
    secondaryContainer = Color(0xFF2C5038),
    onSecondaryContainer = Color(0xFFE0F4DF),
    tertiary = Color(0xFFA7D8A7),
    onTertiary = Color(0xFF173B25),
    tertiaryContainer = Color(0xFF2C5038),
    onTertiaryContainer = Color(0xFFE0F4DF)
)
private val SummerLight = lightColorScheme(
    primary = Color(0xFF34764A),
    primaryContainer = Color(0xFFDBEFDE),
    onPrimaryContainer = Color(0xFF173B25),
    secondary = Color(0xFF34764A),
    secondaryContainer = Color(0xFFDBEFDE),
    onSecondaryContainer = Color(0xFF173B25),
    tertiary = Color(0xFF34764A),
    tertiaryContainer = Color(0xFFDBEFDE),
    onTertiaryContainer = Color(0xFF173B25)
)
private val FallDark = darkColorScheme(
    primary = Color(0xFFFFB36B),
    onPrimary = Color(0xFF4A2500),
    primaryContainer = Color(0xFF633D20),
    onPrimaryContainer = Color(0xFFFFE9D4),
    secondary = Color(0xFFFFB36B),
    onSecondary = Color(0xFF4A2500),
    secondaryContainer = Color(0xFF633D20),
    onSecondaryContainer = Color(0xFFFFE9D4),
    tertiary = Color(0xFFFFB36B),
    onTertiary = Color(0xFF4A2500),
    tertiaryContainer = Color(0xFF633D20),
    onTertiaryContainer = Color(0xFFFFE9D4)
)
private val FallLight = lightColorScheme(
    primary = Color(0xFFB95B2A),
    primaryContainer = Color(0xFFFFE3C4),
    onPrimaryContainer = Color(0xFF4E260E),
    secondary = Color(0xFFB95B2A),
    secondaryContainer = Color(0xFFFFE3C4),
    onSecondaryContainer = Color(0xFF4E260E),
    tertiary = Color(0xFFB95B2A),
    tertiaryContainer = Color(0xFFFFE3C4),
    onTertiaryContainer = Color(0xFF4E260E)
)

private fun readableContentColor(color: Color) = if (color.luminance() > 0.5f) Color.Black else Color.White

private fun blendColors(from: Color, to: Color, fraction: Float) = Color(
    red = from.red + (to.red - from.red) * fraction,
    green = from.green + (to.green - from.green) * fraction,
    blue = from.blue + (to.blue - from.blue) * fraction,
    alpha = 1f
)

private fun customColorScheme(custom: ThemeCustomColors, darkTheme: Boolean) = run {
    val primary = Color(custom.primary)
    val primaryContainer = if (darkTheme) {
        blendColors(primary, Color.Black, 0.64f)
    } else {
        blendColors(primary, Color.White, 0.84f)
    }
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    colors.copy(
        primary = primary,
        onPrimary = readableContentColor(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = readableContentColor(primaryContainer),
        secondary = primary,
        onSecondary = readableContentColor(primary),
        secondaryContainer = primaryContainer,
        onSecondaryContainer = readableContentColor(primaryContainer),
        tertiary = primary,
        onTertiary = readableContentColor(primary),
        tertiaryContainer = primaryContainer,
        onTertiaryContainer = readableContentColor(primaryContainer)
    )
}

@Composable
fun MALTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.SYSTEM,
    customColors: ThemeCustomColors = ThemeCustomColors(),
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sourceColorScheme = when (palette) {
        ThemePalette.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (darkTheme) DefaultDarkColors else DefaultLightColors
        ThemePalette.ANIME_NIGHT -> if (darkTheme) AnimeNightDark else AnimeNightLight
        ThemePalette.MANGA_PAPER -> if (darkTheme) MangaPaperDark else MangaPaperLight
        ThemePalette.MAL_INSPIRED -> if (darkTheme) MalInspiredDark else MalInspiredLight
        ThemePalette.SOFT_PASTEL -> if (darkTheme) GraphiteDark else GraphiteLight
        ThemePalette.WINTER -> if (darkTheme) WinterDark else WinterLight
        ThemePalette.SPRING -> if (darkTheme) SpringDark else SpringLight
        ThemePalette.SUMMER -> if (darkTheme) SummerDark else SummerLight
        ThemePalette.FALL -> if (darkTheme) FallDark else FallLight
        ThemePalette.CUSTOM -> customColorScheme(customColors, darkTheme)
    }
    val accentBackground = sourceColorScheme.primaryContainer
    val accentText = sourceColorScheme.onPrimaryContainer
    val selectedColorScheme = sourceColorScheme.copy(
        primary = accentBackground,
        onPrimary = accentText,
        primaryContainer = accentBackground,
        onPrimaryContainer = accentText,
        secondary = accentBackground,
        onSecondary = accentText,
        secondaryContainer = accentBackground,
        onSecondaryContainer = accentText,
        tertiary = accentBackground,
        onTertiary = accentText,
        tertiaryContainer = accentBackground,
        onTertiaryContainer = accentText
    )
    val colorScheme = if (amoled) {
        selectedColorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF151515)
        )
    } else {
        selectedColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
