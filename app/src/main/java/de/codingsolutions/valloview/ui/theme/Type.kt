/**
 * Typography configuration for the ValloView app theme.
 *
 * Defines Material Design 3 text styles used throughout the Compose UI,
 * providing consistent font properties such as size, weight, and line height
 * across all textual elements.
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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material Design 3 typography scale for ValloView.
 *
 * Provides a cohesive set of text styles, starting with [bodyLarge] as the
 * primary body text style. Additional styles can be added as needed for
 * headings, display text, labels, and other semantic text categories.
 */
val Typography =
    Typography(
        /**
         * Large body text used for primary content and descriptions.
         *
         * - Font size: 16 sp
         * - Line height: 24 sp
         * - Letter spacing: 0.5 sp
         * - Weight: Normal
         */
        bodyLarge =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
    )
