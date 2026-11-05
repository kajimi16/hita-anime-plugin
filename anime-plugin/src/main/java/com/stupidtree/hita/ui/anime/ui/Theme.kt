package com.stupidtree.hita.ui.anime.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animeko 默认主题的种子色（`DefaultSeedColor = Color(0xFF4F378B)`）
 * 以及 Material 3 紫色基线配色。宿主只提供 Material 2，这里用等价的
 * `lightColors` / `darkColors` 表达，避免引入 Material 3 运行时。
 */
object AnimePalette {
    val Seed = Color(0xFF4F378B)

    // Light
    val LightPrimary = Color(0xFF4F378B)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPrimaryContainer = Color(0xFFEADDFF)
    val LightOnPrimaryContainer = Color(0xFF21005D)
    val LightSecondary = Color(0xFF625B71)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightSecondaryContainer = Color(0xFFE8DEF8)
    val LightOnSecondaryContainer = Color(0xFF1D192B)
    val LightBackground = Color(0xFFFEF7FF)
    val LightSurface = Color(0xFFFEF7FF)
    val LightSurfaceVariant = Color(0xFFE7E0EC)
    val LightOnSurface = Color(0xFF1D1B20)
    val LightOnSurfaceVariant = Color(0xFF49454F)
    val LightOutline = Color(0xFF79747E)

    // Dark
    val DarkPrimary = Color(0xFFD0BCFF)
    val DarkOnPrimary = Color(0xFF381E72)
    val DarkPrimaryContainer = Color(0xFF4F378B)
    val DarkOnPrimaryContainer = Color(0xFFEADDFF)
    val DarkSecondary = Color(0xFFCCC2DC)
    val DarkOnSecondary = Color(0xFF332D41)
    val DarkSecondaryContainer = Color(0xFF4A4458)
    val DarkOnSecondaryContainer = Color(0xFFE8DEF8)
    val DarkBackground = Color(0xFF141218)
    val DarkSurface = Color(0xFF1D1B20)
    val DarkSurfaceVariant = Color(0xFF49454F)
    val DarkOnSurface = Color(0xFFE6E0E9)
    val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
    val DarkOutline = Color(0xFF938F99)
}

object AnimeDimens {
    val screenPadding = 20.dp
    val sectionGap = 22.dp
    val cardRadius = 20.dp
    val posterRadius = 18.dp
    val chipRadius = 50.dp
    val compactRadius = 14.dp
    val gridGap = 10.dp

    /** Animeko 的封面统一 9:16。 */
    const val PosterAspect = 9f / 16f
}

private val AnimeTypography = Typography(
    h4 = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    h6 = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    subtitle1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    subtitle2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    body1 = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    body2 = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    caption = TextStyle(fontSize = 11.sp, lineHeight = 15.sp),
    overline = TextStyle(fontSize = 10.sp, letterSpacing = 0.5.sp),
)

private val AnimeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

private val LightColors = lightColors(
    primary = AnimePalette.LightPrimary,
    primaryVariant = AnimePalette.LightPrimaryContainer,
    secondary = AnimePalette.LightSecondary,
    secondaryVariant = AnimePalette.LightSecondaryContainer,
    background = AnimePalette.LightBackground,
    surface = AnimePalette.LightSurface,
    error = Color(0xFFB3261E),
    onPrimary = AnimePalette.LightOnPrimary,
    onSecondary = AnimePalette.LightOnSecondary,
    onBackground = AnimePalette.LightOnSurface,
    onSurface = AnimePalette.LightOnSurface,
    onError = Color.White,
)

private val DarkColors = darkColors(
    primary = AnimePalette.DarkPrimary,
    primaryVariant = AnimePalette.DarkPrimaryContainer,
    secondary = AnimePalette.DarkSecondary,
    secondaryVariant = AnimePalette.DarkSecondaryContainer,
    background = AnimePalette.DarkBackground,
    surface = AnimePalette.DarkSurface,
    error = Color(0xFFF2B8B5),
    onPrimary = AnimePalette.DarkOnPrimary,
    onSecondary = AnimePalette.DarkOnSecondary,
    onBackground = AnimePalette.DarkOnSurface,
    onSurface = AnimePalette.DarkOnSurface,
    onError = Color(0xFF601410),
)

@Composable
fun AnimeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (dark) DarkColors else LightColors,
        typography = AnimeTypography,
        shapes = AnimeShapes,
        content = content,
    )
}

/** 当前主题下的弱化前景色，统一透明度，避免每处自己写 alpha。 */
@Composable
fun mutedOnSurface(alpha: Float = 0.62f): Color =
    MaterialTheme.colors.onSurface.copy(alpha = alpha)
