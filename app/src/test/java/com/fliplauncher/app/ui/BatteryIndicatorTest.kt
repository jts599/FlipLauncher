/** Regression coverage for the battery icon fill reported in issue #1. */
package com.fliplauncher.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks conversion of battery readouts into bounded icon fill fractions. */
class BatteryIndicatorTest {
    /** Ensures empty, partial, full, and malformed readouts receive the expected icon fill. */
    @Test
    fun batteryFillTracksDisplayedPercentage() {
        assertEquals(0f, batteryFillFraction("0%"), 0f)
        assertEquals(0.42f, batteryFillFraction("42%"), 0f)
        assertEquals(1f, batteryFillFraction("100%"), 0f)
        assertEquals(0f, batteryFillFraction("--%"), 0f)
    }

    /** Ensures unexpected readouts cannot draw outside the battery outline. */
    @Test
    fun batteryFillIsClampedToTheBatteryOutline() {
        assertEquals(0f, batteryFillFraction("-5%"), 0f)
        assertEquals(1f, batteryFillFraction("150%"), 0f)
    }
}
