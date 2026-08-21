/** Coordinates state transitions and Android side effects for the styled launcher UI. */
package com.fliplauncher.app.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Owns launcher UI state while keeping rendering composables free of platform effects. */
@Stable
internal class LauncherController(
    private val scope: CoroutineScope,
    private val favoritesRepository: FavoriteRepository,
    private val appCatalog: LaunchableAppCatalog,
    private val navigator: ExternalNavigator,
) {
    var state by mutableStateOf(LauncherUiState())
        private set
    var favorites by mutableStateOf(FavoriteTargets.defaults())
        private set
    var apps by mutableStateOf(emptyList<LaunchableApp>())
        private set
    var isAppPickerVisible by mutableStateOf(false)
        private set
    private var editorFavorites by mutableStateOf(emptyList<QuickLaunchFavorite>())
    private var nameBeforeEditing = ""
    private var favoritesBeforeMove = emptyList<QuickLaunchFavorite>()
    private var slotBeforeMove = 0
    private var moveOpenedFromQuickLaunch = false

    /** Returns the unsaved favorite ordering shown while the editor is in Move mode. */
    fun editorPreviewFavorites(): List<QuickLaunchFavorite> = editorFavorites

    /** Loads persisted favorites and launcher activities without blocking the main thread. */
    fun load() {
        scope.launch {
            favorites = favoritesRepository.load()
            apps = withContext(Dispatchers.Default) { appCatalog.load() }
        }
    }

    /** Handles a visible physical keypad label according to the current keypad mode. */
    fun pressKey(key: String) {
        if (state.keypadMode() == KeypadMode.Telephone) {
            state = reduceTelephoneKey(state, key)
            return
        }
        if (state.screen == LauncherScreen.FavoriteEditor && state.editorMode in setOf(FavoriteEditorMode.NameEditing, FavoriteEditorMode.AppPicker)) return
        val direction = key.directionOrNull()
        if (state.screen == LauncherScreen.FavoriteEditor && state.editorMode == FavoriteEditorMode.Move && direction != null) {
            moveFavorite(direction)
            return
        }
        if (direction != null) state = reduceNavigation(state, direction, favorites).clampSearchIndex(filteredApps())
        if (key == "OK") selectFocusedItem()
    }

    /** Enters shortcut reordering when OK is held on the Quick Launch grid. */
    fun longPressKey(key: String) {
        if (key != "OK" || state.screen != LauncherScreen.QuickLaunch) return
        openSelectedFavoriteEditor()
        moveOpenedFromQuickLaunch = true
        beginMoveMode()
    }

    /** Executes one typed contextual action without relying on its visual position or label. */
    fun pressSoftAction(action: SoftAction) = when (action) {
        SoftAction.Search -> state = state.copy(screen = LauncherScreen.Search, searchMode = SearchMode.Entry, message = null)
        SoftAction.Quick -> state = state.copy(screen = LauncherScreen.QuickLaunch, message = null)
        SoftAction.Settings -> state = state.copy(screen = LauncherScreen.Settings, message = null)
        SoftAction.Clear -> state = state.copy(searchDigits = "", selectedSearchIndex = 0, message = null)
        SoftAction.ToggleSearchMode -> state = state.copy(searchMode = state.searchMode.toggle(), selectedSearchIndex = 0)
        SoftAction.Home -> goHome()
        SoftAction.Edit -> openSelectedFavoriteEditor()
        SoftAction.Add -> openNewFavoriteEditor()
        SoftAction.Save -> saveFavorite()
        SoftAction.Move -> beginMoveMode()
        SoftAction.Cancel -> cancelFavoriteEditor()
        SoftAction.Back -> handleBack()
        SoftAction.Done -> finishEditorSubmode()
        SoftAction.Delete -> if (state.screen == LauncherScreen.FavoriteEditor) beginDeleteConfirmation() else deleteDialedCharacter()
        SoftAction.Text -> openTextMessage()
        SoftAction.Call -> openDialer()
        SoftAction.None -> Unit
    }

    /** Moves back exactly one navigation level, cancelling only the active nested interaction. */
    fun handleBack() {
        if (state.screen != LauncherScreen.FavoriteEditor) { goHome(); return }
        when (state.editorMode) {
            FavoriteEditorMode.NameEditing -> cancelNameEditing()
            FavoriteEditorMode.IconPicker -> cancelIconSelection()
            FavoriteEditorMode.AppPicker -> dismissAppPicker()
            FavoriteEditorMode.Move -> cancelMoveMode()
            FavoriteEditorMode.DeleteConfirmation -> cancelDeleteConfirmation()
            FavoriteEditorMode.Overview -> cancelFavoriteEditor()
        }
    }

    /** Returns to the stable Home view and discards transient view-specific input. */
    fun goHome() { state = homeState() }

    /** Returns the apps currently matching the user's T9 digit sequence. */
    fun filteredApps(): List<LaunchableApp> = filterAppsByT9(apps, state.searchDigits)

    /** Launches the active search item or favorite, or opens the active settings slot. */
    private fun selectFocusedItem() {
        when (state.screen) {
            LauncherScreen.Search -> filteredApps().getOrNull(state.selectedSearchIndex)?.let { launchApp(it) }
            LauncherScreen.QuickLaunch -> launchFavorite()
            LauncherScreen.Settings -> openSelectedFavoriteEditor()
            LauncherScreen.FavoriteEditor -> selectEditorModeItem()
            else -> Unit
        }
    }

    /** Opens one shared editor session from either Quick Launch or Settings. */
    private fun openSelectedFavoriteEditor() {
        editorFavorites = favorites
        state = openFavoriteEditor(state, favorites)
    }

    /** Accepts the focused item according to the current nested editor mode. */
    private fun selectEditorModeItem() = when (state.editorMode) {
        FavoriteEditorMode.Overview -> selectEditorField()
        FavoriteEditorMode.IconPicker -> selectFavoriteIcon()
        FavoriteEditorMode.Move -> finishMoveMode()
        FavoriteEditorMode.DeleteConfirmation -> confirmDeleteFavorite()
        FavoriteEditorMode.NameEditing, FavoriteEditorMode.AppPicker -> Unit
    }

    /** Launches a selected real application and returns Home only after a successful handoff. */
    private fun launchApp(app: LaunchableApp) = finishHandoff(navigator.launchApp(app), "App unavailable")

    /** Launches a selected mock favorite package and displays a local failure if unavailable. */
    private fun launchFavorite() {
        val favorite = favorites.getOrNull(state.selectedFavoriteIndex) ?: return
        finishHandoff(navigator.launchFavorite(favorite.target), "Favorite unavailable")
    }

    /** Opens Android's dialer when a number exists, otherwise leaves concise LCD feedback. */
    private fun openDialer() {
        handOffDialedNumber("Enter a number", "Dialer unavailable", navigator::openDialer)
    }

    /** Opens Android's messaging flow and clears the number before reporting any local failure. */
    private fun openTextMessage() {
        handOffDialedNumber("Enter a number", "Messaging unavailable", navigator::openTextMessage)
    }

    /** Deletes one dialed character and returns Home after deleting the final character. */
    private fun deleteDialedCharacter() {
        val reduced = reduceBackspace(state)
        state = if (reduced.dialedNumber.isEmpty()) homeState() else reduced
    }

    /** Clears the Dialer state before handing a number to an Android activity. */
    private fun handOffDialedNumber(
        emptyMessage: String,
        unavailableMessage: String,
        handoff: (String) -> HandoffResult,
    ) {
        val number = state.dialedNumber
        if (number.isEmpty()) { state = state.copy(message = emptyMessage); return }
        state = homeState()
        if (handoff(number) == HandoffResult.Unavailable) state = state.copy(message = unavailableMessage)
    }

    /** Translates Android handoff success and failure into the requested launcher behavior. */
    private fun finishHandoff(result: HandoffResult, message: String) {
        state = if (result == HandoffResult.Started) homeState() else state.copy(message = message)
    }

    /** Opens a draft session for a new favorite appended after the current configured slots. */
    private fun openNewFavoriteEditor() {
        editorFavorites = favorites + QuickLaunchFavorite(FavoriteLaunchTarget("", ""), "", FavoriteIcon.Apps)
        state = state.copy(screen = LauncherScreen.FavoriteEditor, editorSlotIndex = editorFavorites.lastIndex, editorDraft = FavoriteDraft(), editorField = FavoriteField.Icon, isAddingFavorite = true, editorMode = FavoriteEditorMode.Overview, message = null)
    }

    /** Opens the native picker used to populate the active draft's app target. */
    private fun openAppPicker() {
        isAppPickerVisible = true
        state = state.copy(editorMode = FavoriteEditorMode.AppPicker)
    }

    /** Applies the selected installed activity to the active draft and closes the picker. */
    fun selectPickedApp(app: LaunchableApp) {
        val draft = state.editorDraft ?: return
        state = state.copy(editorDraft = draft.copy(target = FavoriteLaunchTarget(app.label, app.packageName, app.className)), editorMode = FavoriteEditorMode.Overview, message = null)
        isAppPickerVisible = false
    }

    /** Closes the native app picker without changing the active draft. */
    fun dismissAppPicker() {
        isAppPickerVisible = false
        state = state.copy(editorMode = FavoriteEditorMode.Overview)
    }

    /** Starts software-keyboard editing for the active draft name. */
    private fun beginNameEditing() {
        nameBeforeEditing = state.editorDraft?.label.orEmpty()
        state = state.copy(editorMode = FavoriteEditorMode.NameEditing)
    }

    /** Replaces the active draft name with text supplied by the keyboard field. */
    fun updateFavoriteName(name: String) {
        val draft = state.editorDraft ?: return
        state = state.copy(editorDraft = draft.copy(label = name), message = null)
    }

    /** Accepts the staged name and returns to the editor overview. */
    fun finishNameEditing() { state = state.copy(editorMode = FavoriteEditorMode.Overview) }

    /** Restores the name present when keyboard editing began. */
    private fun cancelNameEditing() {
        val draft = state.editorDraft ?: return
        state = state.copy(editorDraft = draft.copy(label = nameBeforeEditing), editorMode = FavoriteEditorMode.Overview)
    }

    /** Opens the focused editor field's input interaction. */
    private fun selectEditorField() = when (state.editorField) {
        FavoriteField.Name -> beginNameEditing()
        FavoriteField.App -> openAppPicker()
        FavoriteField.Icon -> beginIconSelection()
    }

    /** Opens the full-screen icon grid with the draft's current icon focused. */
    private fun beginIconSelection() {
        val draft = state.editorDraft ?: return
        state = state.copy(editorMode = FavoriteEditorMode.IconPicker, selectedFavoriteIconIndex = FavoriteIcon.values().indexOf(draft.icon).coerceAtLeast(0))
    }

    /** Commits the focused icon choice and returns to the three-value editor display. */
    private fun selectFavoriteIcon() {
        val draft = state.editorDraft ?: return
        val icon = FavoriteIcon.values().getOrElse(state.selectedFavoriteIconIndex) { draft.icon }
        state = state.copy(editorDraft = draft.copy(icon = icon), editorMode = FavoriteEditorMode.Overview)
    }

    /** Leaves icon selection without changing the editor draft. */
    private fun cancelIconSelection() { state = state.copy(editorMode = FavoriteEditorMode.Overview) }

    /** Starts a reversible reorder submode using a snapshot for Back cancellation. */
    private fun beginMoveMode() {
        favoritesBeforeMove = editorFavorites
        slotBeforeMove = state.editorSlotIndex
        state = state.copy(editorMode = FavoriteEditorMode.Move)
    }

    /** Keeps the temporary order, persisting immediately for Quick Launch move-only sessions. */
    private fun finishMoveMode() {
        if (!moveOpenedFromQuickLaunch) { state = state.copy(editorMode = FavoriteEditorMode.Overview); return }
        favorites = editorFavorites
        val selectedIndex = state.editorSlotIndex
        moveOpenedFromQuickLaunch = false
        editorFavorites = emptyList()
        state = state.copy(screen = LauncherScreen.QuickLaunch, selectedFavoriteIndex = selectedIndex, editorDraft = null, editorMode = FavoriteEditorMode.Overview)
        scope.launch { favoritesRepository.save(favorites) }
    }

    /** Restores the order present when Move began and returns to the overview. */
    private fun cancelMoveMode() {
        editorFavorites = favoritesBeforeMove
        if (moveOpenedFromQuickLaunch) {
            moveOpenedFromQuickLaunch = false
            editorFavorites = emptyList()
            state = state.copy(screen = LauncherScreen.QuickLaunch, editorDraft = null, editorMode = FavoriteEditorMode.Overview, selectedFavoriteIndex = slotBeforeMove)
            return
        }
        state = state.copy(editorMode = FavoriteEditorMode.Overview, editorSlotIndex = slotBeforeMove.coerceAtMost(editorFavorites.lastIndex))
    }

    /** Accepts the current nested editor selection in the same way as keypad OK. */
    private fun finishEditorSubmode() = when (state.editorMode) {
        FavoriteEditorMode.NameEditing -> finishNameEditing()
        FavoriteEditorMode.IconPicker -> selectFavoriteIcon()
        FavoriteEditorMode.Move -> finishMoveMode()
        FavoriteEditorMode.DeleteConfirmation -> confirmDeleteFavorite()
        FavoriteEditorMode.AppPicker, FavoriteEditorMode.Overview -> Unit
    }

    /** Opens a reversible confirmation state before removing the active favorite. */
    private fun beginDeleteConfirmation() {
        if (editorFavorites.size <= 1) { state = state.copy(message = "KEEP ONE APP"); return }
        state = state.copy(editorMode = FavoriteEditorMode.DeleteConfirmation, message = null)
    }

    /** Leaves delete confirmation without changing the editor session. */
    private fun cancelDeleteConfirmation() { state = state.copy(editorMode = FavoriteEditorMode.Overview) }

    /** Removes the active favorite, persists the list, and returns to the nearest grid slot. */
    private fun confirmDeleteFavorite() {
        val removedIndex = state.editorSlotIndex
        val updated = editorFavorites.toMutableList().apply { removeAt(removedIndex) }
        favorites = updated
        editorFavorites = emptyList()
        state = state.copy(screen = LauncherScreen.QuickLaunch, selectedFavoriteIndex = removedIndex.coerceAtMost(updated.lastIndex), editorDraft = null, editorMode = FavoriteEditorMode.Overview, message = "Deleted")
        scope.launch { favoritesRepository.save(updated) }
    }

    /** Moves the active draft favorite to a neighboring Quick Launch grid slot. */
    private fun moveFavorite(direction: NavigationDirection) {
        val destination = moveFavoriteIndex(state.editorSlotIndex, direction, editorFavorites.size)
        if (destination == state.editorSlotIndex) return
        editorFavorites = editorFavorites.toMutableList().apply {
            val active = this[state.editorSlotIndex]
            this[state.editorSlotIndex] = this[destination]
            this[destination] = active
        }
        state = state.copy(editorSlotIndex = destination)
    }

    /** Discards draft properties and draft ordering, returning to Quick Launch. */
    private fun cancelFavoriteEditor() {
        editorFavorites = emptyList()
        state = state.copy(screen = LauncherScreen.QuickLaunch, editorDraft = null, editorMode = FavoriteEditorMode.Overview)
    }

    /** Validates, commits, and persists the complete edited favorite collection. */
    private fun saveFavorite() {
        val draft = state.editorDraft ?: return
        val target = draft.target
        if (target == null || draft.label.isBlank()) { state = state.copy(message = "SELECT APP AND NAME"); return }
        val updated = editorFavorites.toMutableList().apply { this[state.editorSlotIndex] = QuickLaunchFavorite(target, draft.label.trim(), draft.icon) }
        favorites = updated
        editorFavorites = emptyList()
        state = state.copy(screen = LauncherScreen.QuickLaunch, selectedFavoriteIndex = state.editorSlotIndex, editorDraft = null, isAddingFavorite = false, editorMode = FavoriteEditorMode.Overview, message = "Saved")
        scope.launch { favoritesRepository.save(updated) }
    }
}

/** Maps an LCD navigation label to a reducer direction. */
private fun String.directionOrNull(): NavigationDirection? = when (this) {
    "↑" -> NavigationDirection.Up
    "↓" -> NavigationDirection.Down
    "←" -> NavigationDirection.Left
    "→" -> NavigationDirection.Right
    else -> null
}

/** Switches Search between T9 entry and focused result navigation. */
private fun SearchMode.toggle(): SearchMode = if (this == SearchMode.Entry) SearchMode.Results else SearchMode.Entry

/** Prevents focus from pointing beyond the filtered Search result list. */
private fun LauncherUiState.clampSearchIndex(apps: List<LaunchableApp>): LauncherUiState =
    if (screen != LauncherScreen.Search || apps.isEmpty()) copy(selectedSearchIndex = 0)
    else copy(selectedSearchIndex = selectedSearchIndex.coerceAtMost(apps.lastIndex))
