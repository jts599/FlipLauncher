/** Compose regressions for constrained page rails, filtering, and overflow selection. */
package com.fliplauncher.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import org.junit.Rule
import org.junit.Test

/** Hosts isolated Compose rails; tests mutate only local UI state and fail through assertions. */
class PageIndicatorLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    /** Verifies full dots and selection beyond overflow; returns Unit, with assertion failures only. */
    @Test
    fun constrainedRailKeepsDotsWholeAndOverflowSelected() {
        val page = mutableIntStateOf(1)
        compose.setContent { TestRail(23, page.intValue, 10) }
        compose.onNodeWithContentDescription("Page 2")
            .assertHeightIsEqualTo(5.dp).assertWidthIsEqualTo(5.dp).assertIsSelected()
        compose.onNodeWithContentDescription("Pages 3–10")
            .assertHeightIsEqualTo(2.dp).assertWidthIsEqualTo(5.dp).assertIsNotSelected()
        for (index in listOf(2, 5, 9)) {
            compose.runOnIdle { page.intValue = index }
            compose.onNodeWithContentDescription("Pages 3–10").assertIsSelected()
        }
        compose.runOnIdle { page.intValue = 0 }
        compose.onNodeWithContentDescription("Page 1").assertIsSelected()
        compose.onNodeWithContentDescription("Pages 3–10").assertIsNotSelected()
    }

    /** Verifies filtering recalculates markers; returns Unit, mutating local state with assertion failures only. */
    @Test
    fun filteringRemovesOverflowWhenPagesFit() {
        val count = mutableIntStateOf(10)
        compose.setContent { TestRail(23, 0, count.intValue) }
        compose.runOnIdle { count.intValue = 3 }
        compose.onNodeWithContentDescription("Pages 3–10").assertDoesNotExist()
        compose.onNodeWithContentDescription("Page 3").assertHeightIsEqualTo(5.dp)
        compose.runOnIdle { count.intValue = 1 }
        compose.onNodeWithContentDescription("Page 1").assertDoesNotExist()
    }

    /** Verifies minimal height shows a line without dots; returns Unit, with assertion failures only. */
    @Test
    fun tinyRailShowsOnlyOverflow() {
        compose.setContent { TestRail(2, 9, 10) }
        compose.onNodeWithContentDescription("Pages 1–10").assertHeightIsEqualTo(2.dp).assertIsSelected()
        compose.onNodeWithContentDescription("Page 1").assertDoesNotExist()
    }

    /** Verifies resizing updates capacity and hides a line that cannot fit; returns Unit, with assertion failures only. */
    @Test
    fun resizingRecalculatesAvailableMarkers() {
        val height = mutableIntStateOf(23)
        compose.setContent { TestRail(height.intValue, 9, 10) }
        compose.runOnIdle { height.intValue = 32 }
        compose.onNodeWithContentDescription("Page 3").assertHeightIsEqualTo(5.dp)
        compose.onNodeWithContentDescription("Pages 4–10").assertIsSelected()
        compose.onNodeWithContentDescription("Pages 3–10").assertDoesNotExist()
        compose.runOnIdle { height.intValue = 1 }
        compose.onNodeWithContentDescription("Pages 1–10").assertDoesNotExist()
    }
}

/**
 * Hosts a rail at a deterministic density so capacity expectations do not depend on the test device.
 * @param height Required nonnegative available height in dp.
 * @param page Required valid zero-based selected page.
 * @param count Required nonnegative total pages.
 * @return Unit. Emits test UI only, with no expected errors or external effects.
 */
@Composable
private fun TestRail(height: Int, page: Int, count: Int) {
    CompositionLocalProvider(LocalDensity provides Density(1f)) {
        Box(Modifier.size(5.dp, height.dp)) { PageIndicator(page, count) }
    }
}
