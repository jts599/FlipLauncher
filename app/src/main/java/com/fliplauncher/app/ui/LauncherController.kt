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
        val direction = key.directionOrNull()
        if (direction != null) state = reduceNavigation(state, direction, favorites).clampSearchIndex(filteredApps())
        if (key == "OK") selectFocusedItem()
    }

    /** Executes the action represented by a red, yellow, or green LCD soft-key label. */
    fun pressSoftKey(index: Int) {
        when (state.screen) {
            LauncherScreen.Home -> openHomeDestination(index)
            LauncherScreen.Search -> handleSearchAction(index)
            LauncherScreen.QuickLaunch -> handleQuickAction(index)
            LauncherScreen.Settings -> handleSettingsAction(index)
            LauncherScreen.FavoriteEditor -> handleEditorAction(index)
            LauncherScreen.Dialer -> handleDialerAction(index)
        }
    }

    /** Returns to the stable Home view and discards transient view-specific input. */
    fun goHome() { state = homeState() }

    /** Returns the apps currently matching the user's T9 digit sequence. */
    fun filteredApps(): List<LaunchableApp> = filterAppsByT9(apps, state.searchDigits)

    /** Opens Home's ordered Search, Quick Launch, or Settings destination. */
    private fun openHomeDestination(index: Int) {
        state = when (index) {
            0 -> state.copy(screen = LauncherScreen.Search, searchMode = SearchMode.Entry, message = null)
            1 -> state.copy(screen = LauncherScreen.QuickLaunch, message = null)
            else -> state.copy(screen = LauncherScreen.Settings, message = null)
        }
    }

    /** Clears Search, switches entry/result mode, or returns Home. */
    private fun handleSearchAction(index: Int) {
        state = when (index) {
            0 -> state.copy(searchDigits = "", selectedSearchIndex = 0, message = null)
            1 -> state.copy(searchMode = state.searchMode.toggle(), selectedSearchIndex = 0)
            else -> homeState()
        }
    }

    /** Returns Home, launches the selected favorite, or opens its settings grid. */
    private fun handleQuickAction(index: Int) {
        when (index) {
            0 -> goHome()
            1 -> launchFavorite()
            else -> state = state.copy(screen = LauncherScreen.Settings, message = null)
        }
    }

    /** Returns Home, opens the selected editor, or returns to Quick Launch. */
    private fun handleSettingsAction(index: Int) {
        state = when (index) {
            0 -> homeState()
            1 -> openFavoriteEditor(state, favorites)
            else -> state.copy(screen = LauncherScreen.QuickLaunch, message = null)
        }
    }

    /** Cancels or persists the current Quick Launch favorite edit. */
    private fun handleEditorAction(index: Int) {
        if (index == 0) state = state.copy(screen = LauncherScreen.Settings, editorDraft = null)
        if (index == 1) saveFavorite()
    }

    /** Clears, deletes, or hands the current number to Android's dialer. */
    private fun handleDialerAction(index: Int) {
        when (index) {
            0 -> deleteDialedCharacter()
            1 -> openTextMessage()
            else -> openDialer()
        }
    }

    /** Launches the active search item or favorite, or opens the active settings slot. */
    private fun selectFocusedItem() {
        when (state.screen) {
            LauncherScreen.Search -> filteredApps().getOrNull(state.selectedSearchIndex)?.let { launchApp(it) }
            LauncherScreen.QuickLaunch -> launchFavorite()
            LauncherScreen.Settings -> state = openFavoriteEditor(state, favorites)
            else -> Unit
        }
    }

    /** Launches a selected real application and returns Home only after a successful handoff. */
    private fun launchApp(app: LaunchableApp) = finishHandoff(navigator.launchApp(app), "App unavailable")

    /** Launches a selected mock favorite package and displays a local failure if unavailable. */
    private fun launchFavorite() {
        val favorite = favorites.getOrNull(state.selectedFavoriteIndex) ?: return
        finishHandoff(navigator.launchFavorite(FavoriteTargets.find(favorite.targetId)), "Favorite unavailable")
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

    /** Replaces one slot and saves it asynchronously after an editor confirmation. */
    private fun saveFavorite() {
        val draft = state.editorDraft ?: return
        val updated = favorites.toMutableList().apply { this[state.editorSlotIndex] = QuickLaunchFavorite(draft.targetId, draft.label, draft.icon) }
        favorites = updated
        state = state.copy(screen = LauncherScreen.Settings, editorDraft = null, message = "Saved")
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
