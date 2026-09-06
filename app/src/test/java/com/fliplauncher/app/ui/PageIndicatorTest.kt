/** Regression coverage for the capped page indicator. */
package com.fliplauncher.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises pure bounded-dot rules without platform state or external side effects. */
class PageIndicatorTest {
    /** Confirms the rail shows one dot per page until reaching the eight-dot cap. */
    @Test
    fun dotCountIsCappedAtEight() {
        assertEquals(0, pageIndicatorDotCount(0))
        assertEquals(0, pageIndicatorDotCount(1))
        assertEquals(3, pageIndicatorDotCount(3))
        assertEquals(8, pageIndicatorDotCount(8))
        assertEquals(8, pageIndicatorDotCount(100))
    }

    /** Confirms logical pages share a dot when their count exceeds the cap. */
    @Test
    fun extraPagesAdvanceTheSelectedDotInBuckets() {
        assertEquals(0, selectedPageDotIndex(0, 10))
        assertEquals(0, selectedPageDotIndex(1, 10))
        assertEquals(1, selectedPageDotIndex(2, 10))
        assertEquals(4, selectedPageDotIndex(5, 10))
        assertEquals(7, selectedPageDotIndex(9, 10))
    }

    /** Confirms accessibility labels describe every logical page represented by a dot. */
    @Test
    fun dotDescriptionsNameTheirPageRanges() {
        assertEquals("Pages 1–2", pageDotDescription(0, 10))
        assertEquals("Page 3", pageDotDescription(1, 10))
        assertEquals("Pages 6–7", pageDotDescription(4, 10))
        assertEquals("Page 10", pageDotDescription(7, 10))
    }
}
