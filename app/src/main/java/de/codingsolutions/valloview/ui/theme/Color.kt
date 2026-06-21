/**
 * Color palette definitions for the ValloView app theme.
 *
 * Contains all color constants used throughout the Compose UI, organized by
 * shade and purpose. These colors are referenced in the theme configuration
 * to ensure consistent styling across the application.
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

import androidx.compose.ui.graphics.Color

/**
 * Primary brand color for the Vallox theme.
 *
 * A deep blue used for primary buttons, accents, and key UI elements.
 * Hex: #0033A0
 */
val ValloxBlue = Color(0xFF0033A0)

/**
 * Light blue variant for secondary emphasis.
 *
 * Used for lighter UI elements, hover states, and subtle accents.
 * Hex: #6685C6
 */
val ValloxLightBlue = Color(0xFF6685C6)

/**
 * Faded blue for muted backgrounds and borders.
 *
 * Provides reduced contrast for dividers, disabled states, and borders.
 * Hex: #A5B9C4
 */
val ValloxFadedBlue = Color(0xFFA5B9C4)

/**
 * Light gray for primary backgrounds and surfaces.
 *
 * Used for card backgrounds, input fields, and light surface areas.
 * Hex: #EBECed
 */
val ValloxLightGray = Color(0xFFEBECED)

/**
 * Dark gray for text and dark surface elements.
 *
 * Primary text color and dark background accents providing strong contrast.
 * Hex: #333333
 */
val ValloxDarkGray = Color(0xFF333333)
