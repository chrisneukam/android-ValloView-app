/**
 * Theme configuration and Material Design 3 color schemes for ValloView.
 *
 * Defines dark and light color schemes using the Vallox brand palette, along
 * with the main [ValloViewTheme] composable that applies these schemes based
 * on system preferences.
 *
 * @author Christian Neukam
 * @version 1.0
 * @date 2026-06-08
 *
 * Copyright 2026 Christian Neukam. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codingsolutions.valloview.ui.theme

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

/**
 * Dark theme color scheme using Vallox brand colors.
 *
 * Configured with dark backgrounds and light text for low-light environments.
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = ValloxBlue,
        secondary = ValloxLightGray,
        tertiary = ValloxFadedBlue,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
    )

/**
 * Light theme color scheme using Vallox brand colors.
 *
 * Configured with light backgrounds and dark text for bright environments.
 */
private val LightColorScheme =
    lightColorScheme(
        primary = ValloxBlue,
        secondary = ValloxDarkGray,
        tertiary = ValloxLightBlue,
        background = Color.White,
        surface = ValloxLightGray,
        onPrimary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
    )

/**
 * Main theme composable for ValloView.
 *
 * Applies the appropriate color scheme (dark or light) based on system preferences
 * or explicit parameters. Material Design 3 dynamic color is disabled by default
 * to preserve brand consistency.
 *
 * @param darkTheme whether to use the dark color scheme.
 * Defaults to the system's dark mode preference via [isSystemInDarkTheme].
 * @param dynamicColor whether to enable Material Design 3 dynamic colors (Android 12+).
 * Defaults to `false` to prioritize brand colors over system-generated colors.
 * @param content the composable hierarchy to render under this theme.
 */
@Composable
fun ValloViewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to prioritize our branding colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
