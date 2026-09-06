/** Compose regression coverage for fixed-width icon-picker rows from issue #2. */
package com.fliplauncher.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/** Exercises the icon picker with an incomplete final row at a deterministic display density. */
class FavoriteIconPickerLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    /** Ensures final-row icons retain a single-column focus-box width instead of expanding. */
    @Test
    fun incompleteFinalRowKeepsIconCellsAligned() {
        compose.setContent { IncompleteIconPicker() }

        listOf("Mail", "Music").forEach { iconName ->
            compose.onNodeWithContentDescription("$iconName icon").assertWidthIsEqualTo(92.dp)
        }
    }
}

/** Hosts two final-row icons in a three-column, 300 dp-wide picker grid. */
@Composable
private fun IncompleteIconPicker() {
    CompositionLocalProvider(LocalDensity provides Density(1f)) {
        Box(Modifier.size(300.dp, 169.dp)) {
            FavoriteIconPickerGrid(
                icons = FavoriteIcon.values().take(11),
                firstIconIndex = 0,
                selectedIconIndex = 10,
            )
        }
    }
}
