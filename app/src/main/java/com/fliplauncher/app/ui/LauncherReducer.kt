/** Provides pure state transitions for FlipLauncher's keypad-driven navigation. */
package com.fliplauncher.app.ui

private const val QuickLaunchColumnCount = 3
private const val SettingsColumnCount = 2

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
    LauncherScreen.QuickLaunch, LauncherScreen.Settings -> state.copy(
        selectedFavoriteIndex = moveGridIndex(
            index = state.selectedFavoriteIndex,
            direction = direction,
            itemCount = favorites.size,
            columnCount = favoriteColumnCount(state.screen),
        ),
    )
    LauncherScreen.FavoriteEditor -> reduceEditorNavigation(state, direction)
    else -> state
}

/** Opens the selected Quick Launch favorite for editing. */
internal fun openFavoriteEditor(state: LauncherUiState, favorites: List<QuickLaunchFavorite>): LauncherUiState {
    val favorite = favorites.getOrNull(state.selectedFavoriteIndex) ?: return state
    return state.copy(
        screen = LauncherScreen.FavoriteEditor,
        editorSlotIndex = state.selectedFavoriteIndex,
        editorField = FavoriteField.App,
        editorDraft = FavoriteDraft(favorite.targetId, favorite.label, favorite.icon),
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

/** Returns the column count used by the current favorite-focused LCD screen. */
private fun favoriteColumnCount(screen: LauncherScreen): Int = when (screen) {
    LauncherScreen.QuickLaunch -> QuickLaunchColumnCount
    LauncherScreen.Settings -> SettingsColumnCount
    else -> SettingsColumnCount
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
    return candidate.coerceIn(0, itemCount - 1)
}

/** Moves between editor fields or cycles the current draft value. */
private fun reduceEditorNavigation(state: LauncherUiState, direction: NavigationDirection): LauncherUiState {
    val draft = state.editorDraft ?: return state
    return when (direction) {
        NavigationDirection.Up -> state.copy(editorField = state.editorField.previous())
        NavigationDirection.Down -> state.copy(editorField = state.editorField.next())
        NavigationDirection.Left -> state.copy(editorDraft = draft.cycle(state.editorField, -1))
        NavigationDirection.Right -> state.copy(editorDraft = draft.cycle(state.editorField, 1))
    }
}

/** Returns the preceding editor field, wrapping from App to Icon. */
private fun FavoriteField.previous(): FavoriteField = FavoriteField.values()[(ordinal + FavoriteField.values().size - 1) % FavoriteField.values().size]

/** Returns the following editor field, wrapping from Icon to App. */
private fun FavoriteField.next(): FavoriteField = FavoriteField.values()[(ordinal + 1) % FavoriteField.values().size]

/** Cycles one favorite property while preserving the other draft selections. */
private fun FavoriteDraft.cycle(field: FavoriteField, step: Int): FavoriteDraft = when (field) {
    FavoriteField.App -> copy(targetId = FavoriteTargets.all.map { target -> target.id }.cycleValue(targetId, step)).withValidLabel()
    FavoriteField.Label -> copy(label = FavoriteTargets.find(targetId).labels.cycleValue(label, step))
    FavoriteField.Icon -> copy(icon = FavoriteIcon.values().toList().cycleValue(icon, step))
}

/** Replaces a label that is not valid for the selected target with that target's default label. */
private fun FavoriteDraft.withValidLabel(): FavoriteDraft {
    val target = FavoriteTargets.find(targetId)
    return if (label in target.labels) this else copy(label = target.labels.first())
}

/** Selects the next or previous item in a non-empty list, wrapping at both ends. */
private fun <T> List<T>.cycleValue(current: T, step: Int): T {
    val index = indexOf(current).coerceAtLeast(0)
    return this[(index + step + size) % size]
}
