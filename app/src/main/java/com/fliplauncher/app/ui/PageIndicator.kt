/** Shared Compose page rail and pure capacity rules for constrained launcher displays (issue #6). */
package com.fliplauncher.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val DotSize = 5f
private const val MarkerSpacing = 4f
private const val OverflowHeight = 2f
private const val InactiveAlpha = 0.35f

/**
 * Computes how many complete page dots fit, reserving a slot for overflow when necessary.
 * @param pageCount Required total page count; values below two produce no dots.
 * @param availableHeight Required nonnegative height; positive infinity is unbounded.
 * @param markerSize Optional positive slot height, default five; use the same units as availableHeight.
 * @param spacing Optional nonnegative gap, default four; use the same units as availableHeight.
 * @return Number of initial pages represented by dots; zero also covers line-only layouts.
 * No side effects or exceptions for supported inputs.
 */
internal fun visiblePageDotCount(
    pageCount: Int, availableHeight: Float, markerSize: Float = DotSize, spacing: Float = MarkerSpacing,
): Int {
    if (pageCount <= 1) return 0
    val capacity = ((availableHeight + spacing) / (markerSize + spacing)).toInt()
    return if (pageCount <= capacity) pageCount else (capacity - 1).coerceAtLeast(0)
}

/**
 * Determines whether the overflow marker represents the selected page.
 * @param pageIndex Required valid zero-based current page index.
 * @param visibleDotCount Required nonnegative number of initial pages with dots.
 * @return True for every page without a dot. No side effects or exceptions.
 */
internal fun isPageOverflowSelected(pageIndex: Int, visibleDotCount: Int): Boolean =
    pageIndex >= visibleDotCount

/**
 * Emits a height-adaptive rail whose overflow line represents all pages without dots.
 * @param pageIndex Required valid zero-based current page index.
 * @param pageCount Required total pages; values below two hide the rail.
 * @param modifier Optional placement constraints, defaulting to an empty modifier.
 * @return Unit. Emits Compose UI and accessibility semantics; no external effects or expected errors.
 */
@Composable
internal fun PageIndicator(pageIndex: Int, pageCount: Int, modifier: Modifier = Modifier) {
    if (pageCount <= 1) return
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val availableHeight = maxHeight.value
        // Match Compose's pixel rounding so fractional densities cannot squeeze the last dot.
        val dotCount = with(LocalDensity.current) {
            visiblePageDotCount(
                pageCount, constraints.maxHeight.toFloat(),
                DotSize.dp.roundToPx().toFloat(), MarkerSpacing.dp.roundToPx().toFloat(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(MarkerSpacing.dp)) {
            repeat(dotCount) { index ->
                PageMarker(index == pageIndex, false, "Page ${index + 1}")
            }
            if (dotCount < pageCount && availableHeight >= OverflowHeight) {
                PageMarker(
                    isPageOverflowSelected(pageIndex, dotCount), true, "Pages ${dotCount + 1}–$pageCount",
                    slotHeight = availableHeight.coerceAtMost(DotSize),
                )
            }
        }
    }
}

/**
 * Emits a fixed-size dot or horizontal overflow line with a shared selection treatment.
 * @param active Required flag controlling full ink opacity and selected semantics.
 * @param overflow Required flag choosing a line instead of a circular dot.
 * @param description Required accessible description of the represented page or range.
 * @param slotHeight Optional slot height in dp, at least two for overflow; defaults to five.
 * @return Unit. Emits Compose UI only; no external effects or expected errors.
 */
@Composable
private fun PageMarker(active: Boolean, overflow: Boolean, description: String, slotHeight: Float = DotSize) {
    Box(Modifier.size(DotSize.dp, slotHeight.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(DotSize.dp, if (overflow) OverflowHeight.dp else DotSize.dp)
                .background(
                    FlipColors.ScreenInk.copy(alpha = if (active) 1f else InactiveAlpha),
                    if (overflow) RectangleShape else CircleShape,
                )
                .semantics {
                    contentDescription = description
                    selected = active
                },
        )
    }
}
