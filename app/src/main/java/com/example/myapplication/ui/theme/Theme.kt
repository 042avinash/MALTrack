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
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.local.ThemePalette

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
    primary = Color(0xFF9ECAFF), onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D), onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF77D5DE), onSecondary = Color(0xFF00363B),
    secondaryContainer = Color(0xFF005057), onSecondaryContainer = Color(0xFF9CF1FA),
    tertiary = Color(0xFFFFD27D), onTertiary = Color(0xFF402D00),
    background = Color(0xFF101820), onBackground = Color(0xFFDCE3EC),
    surface = Color(0xFF101820), onSurface = Color(0xFFDCE3EC),
    surfaceVariant = Color(0xFF414A54), onSurfaceVariant = Color(0xFFC1CAD5)
)
private val MalInspiredLight = lightColorScheme(
    primary = Color(0xFF145B96), onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E4FF), onPrimaryContainer = Color(0xFF001C35),
    secondary = Color(0xFF006970), onSecondary = Color.White,
    secondaryContainer = Color(0xFF9CF1FA), onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFF775A00), onTertiary = Color.White,
    background = Color(0xFFF7F9FF), onBackground = Color(0xFF171C22),
    surface = Color(0xFFF7F9FF), onSurface = Color(0xFF171C22),
    surfaceVariant = Color(0xFFDDE3ED), onSurfaceVariant = Color(0xFF414750)
)

private val SoftPastelDark = darkColorScheme(
    primary = Color(0xFFD8BFFF), onPrimary = Color(0xFF392751),
    primaryContainer = Color(0xFF503D69), onPrimaryContainer = Color(0xFFF0DBFF),
    secondary = Color(0xFF9DD8CA), onSecondary = Color(0xFF12382F),
    secondaryContainer = Color(0xFF2B5046), onSecondaryContainer = Color(0xFFB9F4E5),
    tertiary = Color(0xFFFFB2C1), onTertiary = Color(0xFF5F112B),
    background = Color(0xFF19171C), onBackground = Color(0xFFE9E0E9),
    surface = Color(0xFF19171C), onSurface = Color(0xFFE9E0E9),
    surfaceVariant = Color(0xFF49434C), onSurfaceVariant = Color(0xFFCBC3CD)
)
private val SoftPastelLight = lightColorScheme(
    primary = Color(0xFF705586), onPrimary = Color.White,
    primaryContainer = Color(0xFFF0DBFF), onPrimaryContainer = Color(0xFF281438),
    secondary = Color(0xFF35695B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F4E5), onSecondaryContainer = Color(0xFF002117),
    tertiary = Color(0xFF9A4057), onTertiary = Color.White,
    background = Color(0xFFFFF7FB), onBackground = Color(0xFF201A21),
    surface = Color(0xFFFFF7FB), onSurface = Color(0xFF201A21),
    surfaceVariant = Color(0xFFECE1EB), onSurfaceVariant = Color(0xFF4B454E)
)

@Composable
fun MALTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.SYSTEM,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val selectedColorScheme = when (palette) {
        ThemePalette.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (darkTheme) DefaultDarkColors else DefaultLightColors
        ThemePalette.ANIME_NIGHT -> if (darkTheme) AnimeNightDark else AnimeNightLight
        ThemePalette.MANGA_PAPER -> if (darkTheme) MangaPaperDark else MangaPaperLight
        ThemePalette.MAL_INSPIRED -> if (darkTheme) MalInspiredDark else MalInspiredLight
        ThemePalette.SOFT_PASTEL -> if (darkTheme) SoftPastelDark else SoftPastelLight
    }
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
