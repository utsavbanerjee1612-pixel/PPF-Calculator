package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CleanPrimaryDark,
    onPrimary = CleanOnPrimaryDark,
    primaryContainer = CleanPrimaryContainerDark,
    onPrimaryContainer = CleanOnPrimaryContainerDark,
    secondary = CleanPrimaryDark,
    secondaryContainer = CleanSecondaryContainerDark,
    onSecondaryContainer = CleanOnSecondaryContainerDark,
    tertiary = CleanTertiaryContainerDark,
    tertiaryContainer = CleanTertiaryContainerDark,
    onTertiaryContainer = CleanOnTertiaryContainerDark,
    background = CleanBackgroundDark,
    surface = CleanSurfaceDark,
    onBackground = CleanOnPrimaryContainerDark,
    onSurface = CleanOnPrimaryContainerDark,
    surfaceVariant = CleanSurfaceVariantDark,
    onSurfaceVariant = CleanOnSurfaceVariantDark,
    outline = CleanOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CleanPrimaryLight,
    onPrimary = CleanOnPrimaryLight,
    primaryContainer = CleanPrimaryContainerLight,
    onPrimaryContainer = CleanOnPrimaryContainerLight,
    secondary = CleanPrimaryLight,
    secondaryContainer = CleanSecondaryContainerLight,
    onSecondaryContainer = CleanOnSecondaryContainerLight,
    tertiary = CleanTertiaryContainerLight,
    tertiaryContainer = CleanTertiaryContainerLight,
    onTertiaryContainer = CleanOnTertiaryContainerLight,
    background = CleanBackgroundLight,
    surface = CleanSurfaceLight,
    onBackground = CleanOnSecondaryContainerLight,
    onSurface = CleanOnSecondaryContainerLight,
    surfaceVariant = CleanSurfaceVariantLight,
    onSurfaceVariant = CleanOnSurfaceVariantLight,
    outline = CleanOutlineLight
  )


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to preserve Clean Utility design look
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
