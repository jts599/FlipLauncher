/** Provides pure state transitions for FlipLauncher's keypad-driven navigation. */
package com.fliplauncher.app.ui

/** Identifies directional input from the navigation keypad. */
internal enum class NavigationDirection { Up, Down, Left, Right }

/** Applies a dialable telephone key to Home, Search entry, or Dialer state. */
internal fun reduceTelephoneKey(state: LauncherUiState, key: String): LauncherUiState = when (state.screen) {
    LauncherScreen.Home -> if (key.isDialable()) state.copy(screen = LauncherScreen.Dialer, dialedNumber = key, message = null) else state
    LauncherScreen.Dialer -> state.copy(dialedNumber = state.dialedNumber + key, message = null)
    LauncherScreen.Search -> reduceSearchTelephoneKey(state, key)
    else -> state
}

/** Applies the Backspace key to typed Search and Dialer content. */
internal fun reduceBackspace(state: LauncherUiState): LauncherUiState = when (state.screen) {
    LauncherScreen.Search -> state.copy(searchDigits = state.searchDigits.dropLast(1), selectedSearchIndex = 0)
    LauncherScreen.Dialer -> state.copy(dialedNumber = state.dialedNumber.dropLast(1), message = null)
    else -> state
}

/** Moves the focused Search result, favorite slot, or editor field/value. */
internal fun reduceNavigation(
    state: LauncherUiState,
    direction: NavigationDirection,
    favorites: List<QuickLaunchFavorite>,
): LauncherUiState = when (state.screen) {
    LauncherScreen.Search -> reduceSearchNavigation(state, direction)
    LauncherScreen.QuickLaunch -> state.copy(
        selectedFavoriteIndex = moveGridIndex(
            state.selectedFavoriteIndex,
            direction,
            favorites.size,
            QuickLaunchColumnCount,
        ),
    )
    LauncherScreen.Settings -> state.copy(selectedSettingIndex = moveSettingIndex(state.selectedSettingIndex, direction))
    LauncherScreen.FavoriteEditor -> when (state.editorMode) {
        FavoriteEditorMode.IconPicker -> reduceIconPickerNavigation(state, direction)
        FavoriteEditorMode.Overview -> reduceEditorNavigation(state, direction)
        else -> state
    }
    else -> state
}

/** Opens the selected Quick Launch favorite for editing. */
internal fun openFavoriteEditor(state: LauncherUiState, favorites: List<QuickLaunchFavorite>): LauncherUiState {
    val favorite = favorites.getOrNull(state.selectedFavoriteIndex) ?: return state
    return state.copy(
        screen = LauncherScreen.FavoriteEditor,
        editorSlotIndex = state.selectedFavoriteIndex,
        editorField = FavoriteField.Icon,
        editorDraft = FavoriteDraft(favorite.target, favorite.label, favorite.icon),
        isAddingFavorite = false,
        editorMode = FavoriteEditorMode.Overview,
        message = null,
    )
}

/** Clears transient input and returns to Home. */
internal fun homeState(): LauncherUiState = LauncherUiState()

/** Returns whether a keypad token is valid in a telephone number. */
private fun String.isDialable(): Boolean = length == 1 && (first().isDigit() || this == "*" || this == "#")

/** Appends a T9 digit, ignores non-letter digits, or deletes the previous digit. */
private fun reduceSearchTelephoneKey(state: LauncherUiState, key: String): LauncherUiState {
    if (key == "*") return reduceBackspace(state)
    if (key !in "23456789") return state
    return state.copy(searchDigits = state.searchDigits + key, selectedSearchIndex = 0, message = null)
}

/** Moves the Search focus only while result browsing is active. */
private fun reduceSearchNavigation(state: LauncherUiState, direction: NavigationDirection): LauncherUiState {
    if (state.searchMode != SearchMode.Results) return state
    val offset = when (direction) {
        NavigationDirection.Up -> -1
        NavigationDirection.Down -> 1
        NavigationDirection.Left, NavigationDirection.Right -> 0
    }
    return state.copy(selectedSearchIndex = (state.selectedSearchIndex + offset).coerceAtLeast(0))
}

/**
 * Moves between whole Quick Launch pages while preserving the focused cell's position.
 *
 * @param selectedIndex Current selected favorite index.
 * @param direction Keypad direction; Down advances a page, Up returns a page, and horizontal input is ignored.
 * @param favoriteCount Number of configured favorites; zero produces index zero.
 * @return The corresponding favorite index on the requested page, clamped on incomplete final pages.
 * @throws None.
 * @sideEffects None.
 */
internal fun moveFavoritePageIndex(selectedIndex: Int, direction: NavigationDirection, favoriteCount: Int): Int {
    if (favoriteCount == 0) return 0
    val pageCount = (favoriteCount + QuickLaunchPageSize - 1) / QuickLaunchPageSize
    val normalizedIndex = selectedIndex.coerceIn(0, favoriteCount - 1)
    val currentPage = normalizedIndex / QuickLaunchPageSize
    val targetPage = when (direction) {
        NavigationDirection.Up -> (currentPage - 1).coerceAtLeast(0)
        NavigationDirection.Down -> (currentPage + 1).coerceAtMost(pageCount - 1)
        NavigationDirection.Left, NavigationDirection.Right -> currentPage
    }
    val positionInPage = normalizedIndex % QuickLaunchPageSize
    return (targetPage * QuickLaunchPageSize + positionInPage).coerceAtMost(favoriteCount - 1)
}

/** Moves focus through the three vertically arranged setting rows. */
private fun moveSettingIndex(index: Int, direction: NavigationDirection): Int = when (direction) {
    NavigationDirection.Up -> (index - 1).coerceAtLeast(0)
    NavigationDirection.Down -> (index + 1).coerceAtMost(2)
    NavigationDirection.Left, NavigationDirection.Right -> index
}

/**
 * Moves an index through a fixed-width grid without wrapping at its edges.
 *
 * @param index Currently focused zero-based item index.
 * @param direction Keypad direction to apply.
 * @param itemCount Number of available items; zero produces index zero.
 * @param columnCount Number of visual columns in the active grid.
 * @return A valid focused index, clamped to the first or final item when an edge is reached.
 */
private fun moveGridIndex(index: Int, direction: NavigationDirection, itemCount: Int, columnCount: Int): Int {
    if (itemCount == 0) return 0
    val candidate = when (direction) {
        NavigationDirection.Up -> index - columnCount
        NavigationDirection.Down -> index + columnCount
        NavigationDirection.Left -> if (index % columnCount == 0) index else index - 1
        NavigationDirection.Right -> if (index % columnCount == columnCount - 1) index else index + 1
    }
    return candidate.takeIf { it in 0 until itemCount } ?: index
}

/** Moves between editor fields without wrapping past the first or final value. */
private fun reduceEditorNavigation(state: LauncherUiState, direction: NavigationDirection): LauncherUiState {
    if (state.editorDraft == null) return state
    return when (direction) {
        NavigationDirection.Up -> state.copy(editorField = state.editorField.previousOrSame())
        NavigationDirection.Down -> state.copy(editorField = state.editorField.nextOrSame())
        NavigationDirection.Left, NavigationDirection.Right -> state
    }
}

/** Returns the preceding editor field, stopping at Icon. */
private fun FavoriteField.previousOrSame(): FavoriteField = FavoriteField.values()[maxOf(0, ordinal - 1)]

/** Returns the following editor field, stopping at App. */
private fun FavoriteField.nextOrSame(): FavoriteField = FavoriteField.values()[minOf(FavoriteField.values().lastIndex, ordinal + 1)]

/** Moves the focused icon within the three-column full-screen icon picker. */
private fun reduceIconPickerNavigation(state: LauncherUiState, direction: NavigationDirection): LauncherUiState = state.copy(
    selectedFavoriteIconIndex = moveGridIndex(state.selectedFavoriteIconIndex, direction, FavoriteIcon.values().size, QuickLaunchColumnCount),
)

/** Returns the closest valid Quick Launch neighbor without wrapping across grid edges. */
internal fun moveFavoriteIndex(index: Int, direction: NavigationDirection, itemCount: Int): Int =
    moveGridIndex(index, direction, itemCount, QuickLaunchColumnCount)
