/** Compose regressions for the capped launcher page rail. */
package com.fliplauncher.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/** Hosts isolated Compose rails; tests mutate only local UI state and fail through assertions. */
class PageIndicatorLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    /** Confirms ten logical pages are represented by eight fixed-size dots. */
    @Test
    fun cappedRailGroupsExtraPagesIntoDots() {
        val page = mutableIntStateOf(0)
        compose.setContent { TestRail(page.intValue, 10) }
        compose.onNodeWithContentDescription("Pages 1–2")
            .assertHeightIsEqualTo(5.dp).assertWidthIsEqualTo(5.dp).assertIsSelected()
        compose.runOnIdle { page.intValue = 1 }
        compose.onNodeWithContentDescription("Pages 1–2").assertIsSelected()
        compose.runOnIdle { page.intValue = 2 }
        compose.onNodeWithContentDescription("Pages 1–2").assertIsNotSelected()
        compose.onNodeWithContentDescription("Page 3").assertIsSelected()
        compose.onNodeWithContentDescription("Page 10").assertWidthIsEqualTo(5.dp)
    }

    /** Confirms reducing page count removes bucket labels and hides single-page rails. */
    @Test
    fun filteringUsesOneDotPerRemainingPage() {
        val count = mutableIntStateOf(10)
        compose.setContent { TestRail(0, count.intValue) }
        compose.runOnIdle { count.intValue = 3 }
        compose.onNodeWithContentDescription("Pages 1–2").assertDoesNotExist()
        compose.onNodeWithContentDescription("Page 3").assertHeightIsEqualTo(5.dp)
        compose.runOnIdle { count.intValue = 1 }
        compose.onNodeWithContentDescription("Page 1").assertDoesNotExist()
    }
}

/**
 * Hosts a rail with ample height for all eight capped dots.
 *
 * @param page Required zero-based selected page.
 * @param count Required nonnegative total page count.
 * @return Unit. Emits test UI only, with no expected errors or external effects.
 */
@Composable
private fun TestRail(page: Int, count: Int) {
    Box(Modifier.size(5.dp, 68.dp)) { PageIndicator(page, count) }
}
