/** Regression coverage for page rail capacity and overflow selection from issue #6. */
package com.fliplauncher.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exercises pure indicator rules without platform state or external side effects. */
class PageIndicatorTest {
    /** Checks exact fit, spare height, and unbounded constraints; returns Unit, with assertion failures only. */
    @Test
    fun completeDotsFitAvailableHeight() {
        assertEquals(3, visiblePageDotCount(3, 23f))
        assertEquals(3, visiblePageDotCount(3, 100f))
        assertEquals(3, visiblePageDotCount(3, Float.POSITIVE_INFINITY))
    }

    /** Checks overflow reservation at the capacity boundary; returns Unit, with assertion failures only. */
    @Test
    fun overflowReservesOneMarkerSlot() {
        assertEquals(2, visiblePageDotCount(4, 23f))
        assertEquals(2, visiblePageDotCount(100, 23f))
        assertEquals(1, visiblePageDotCount(3, 22f))
        assertEquals(1, visiblePageDotCount(3, 63f, markerSize = 14f, spacing = 11f))
    }

    /** Checks hidden and line-only rails; returns Unit, with assertion failures only. */
    @Test
    fun minimalLayoutsHaveNoDots() {
        assertEquals(0, visiblePageDotCount(0, 100f))
        assertEquals(0, visiblePageDotCount(1, 100f))
        listOf(0f, 1f, 2f, 4f, 5f).forEach { height ->
            assertEquals(0, visiblePageDotCount(10, height))
        }
    }

    /** Checks selection entering and leaving overflow; returns Unit, with assertion failures only. */
    @Test
    fun everyHiddenPageSelectsOverflow() {
        assertFalse(isPageOverflowSelected(1, 2))
        assertTrue(isPageOverflowSelected(2, 2))
        assertTrue(isPageOverflowSelected(99, 2))
        assertFalse(isPageOverflowSelected(0, 2))
        assertTrue(isPageOverflowSelected(0, 0))
    }
}
