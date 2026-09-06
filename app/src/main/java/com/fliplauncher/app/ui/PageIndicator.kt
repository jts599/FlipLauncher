/** Shared Compose page rail and pure bounded-dot rules for launcher displays. */
package com.fliplauncher.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val DotSize = 5f
private const val MarkerSpacing = 4f
private const val MaximumPageDots = 8
private const val InactiveAlpha = 0.35f

/**
 * Returns the bounded number of dots used to represent the available pages.
 *
 * @param pageCount Total number of logical pages; values below two do not need an indicator.
 * @return A count from zero through eight. Counts above eight are grouped into eight dot buckets.
 * @throws None.
 * @sideEffects None.
 */
internal fun pageIndicatorDotCount(pageCount: Int): Int = pageCount.coerceAtMost(MaximumPageDots).takeIf { it > 1 } ?: 0

/**
 * Maps a logical page to its bounded indicator dot.
 *
 * @param pageIndex Zero-based logical page index; out-of-range values are clamped.
 * @param pageCount Total number of logical pages; values below two return zero.
 * @return Zero-based selected dot index. Multiple pages share a dot when more than eight pages exist.
 * @throws None.
 * @sideEffects None.
 */
internal fun selectedPageDotIndex(pageIndex: Int, pageCount: Int): Int {
    val dotCount = pageIndicatorDotCount(pageCount)
    if (dotCount == 0) return 0
    val clampedPageIndex = pageIndex.coerceIn(0, pageCount - 1)
    return clampedPageIndex * dotCount / pageCount
}

/**
 * Describes the logical pages represented by one bounded indicator dot.
 *
 * @param dotIndex Zero-based dot index; out-of-range values are clamped.
 * @param pageCount Total number of logical pages; values below two return an empty label.
 * @return Accessible singular or inclusive-range page label for the dot.
 * @throws None.
 * @sideEffects None.
 */
internal fun pageDotDescription(dotIndex: Int, pageCount: Int): String {
    val dotCount = pageIndicatorDotCount(pageCount)
    if (dotCount == 0) return ""
    val clampedDotIndex = dotIndex.coerceIn(0, dotCount - 1)
    val firstPage = ceilingDivision(clampedDotIndex * pageCount, dotCount)
    val lastPage = ceilingDivision((clampedDotIndex + 1) * pageCount, dotCount) - 1
    return if (firstPage == lastPage) "Page ${firstPage + 1}" else "Pages ${firstPage + 1}–${lastPage + 1}"
}

/**
 * Divides positive values while rounding the quotient upward.
 *
 * @param dividend Nonnegative value to divide.
 * @param divisor Required positive divisor.
 * @return The smallest integer not less than the exact quotient.
 * @throws None for the internally supplied positive divisor.
 * @sideEffects None.
 */
private fun ceilingDivision(dividend: Int, divisor: Int): Int = (dividend + divisor - 1) / divisor

/**
 * Emits a page rail with no more than eight dots.
 *
 * @param pageIndex Zero-based current logical page index.
 * @param pageCount Total logical page count; values below two hide the rail.
 * @param modifier Optional placement constraints, defaulting to an empty modifier.
 * @return Unit. Emits Compose UI and accessibility semantics; no external effects or expected errors.
 */
@Composable
internal fun PageIndicator(pageIndex: Int, pageCount: Int, modifier: Modifier = Modifier) {
    val dotCount = pageIndicatorDotCount(pageCount)
    if (dotCount == 0) return
    val selectedDotIndex = selectedPageDotIndex(pageIndex, pageCount)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(MarkerSpacing.dp)) {
        repeat(dotCount) { dotIndex ->
            PageDot(
                active = dotIndex == selectedDotIndex,
                description = pageDotDescription(dotIndex, pageCount),
            )
        }
    }
}

/**
 * Emits one fixed-size page dot.
 *
 * @param active Whether the dot represents the current logical page.
 * @param description Accessible description of the logical page or page range represented by this dot.
 * @return Unit. Emits Compose UI only; no external effects or expected errors.
 */
@Composable
private fun PageDot(active: Boolean, description: String) {
    Box(Modifier.size(DotSize.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(DotSize.dp)
                .background(FlipColors.ScreenInk.copy(alpha = if (active) 1f else InactiveAlpha), CircleShape)
                .semantics {
                    contentDescription = description
                    selected = active
                },
        )
    }
}
