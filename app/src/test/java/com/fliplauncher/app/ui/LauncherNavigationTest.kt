/** Verifies FlipLauncher's keypad navigation boundaries and nested editor cancellation rules. */
package com.fliplauncher.app.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises pure grid transitions and controller-level editor navigation with in-memory fakes. */
class LauncherNavigationTest {
    /** Locks the intentionally curated icon library to the requested sixty choices. */
    @Test
    fun favoriteIconLibraryContainsExactlySixtyChoices() {
        assertEquals(60, FavoriteIcon.values().size)
        assertEquals(true, FavoriteIcon.values().contains(FavoriteIcon.Bird))
    }

    /** Confirms loading never appends defaults over a deliberately shortened configuration. */
    @Test
    fun configuredFavoritesAreNotReseeded() {
        val configured = FavoriteTargets.defaults().take(4).reversed()

        val loaded = configured.withSeedFavorites()

        assertEquals(configured, loaded)
    }

    /** Confirms the Search backspace action removes one digit instead of clearing the query. */
    @Test
    fun searchBackspaceRemovesOneDigit() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Search)
        controller.pressKey("2")
        controller.pressKey("3")

        controller.pressSoftAction(SoftAction.Backspace)

        assertEquals("2", controller.state.searchDigits)
    }

    /** Confirms result browsing pages the focused item into the visible four-row window. */
    @Test
    fun searchResultWindowTracksFocusedPage() {
        assertEquals(0, searchResultWindowStart(3, SearchMode.Results))
        assertEquals(4, searchResultWindowStart(4, SearchMode.Results))
        assertEquals(8, searchResultWindowStart(11, SearchMode.Results))
    }

    /** Confirms entry mode always previews the first matches regardless of stale focus state. */
    @Test
    fun searchEntryWindowStartsAtFirstResult() {
        assertEquals(0, searchResultWindowStart(7, SearchMode.Entry))
    }

    /** Confirms OK changes keyboard format and multi-press cycles a repeated key. */
    @Test
    fun keyboardSettingControlsSearchEntry() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Settings)
        controller.pressKey("OK")
        controller.pressSoftAction(SoftAction.Search)

        controller.pressKey("2")
        controller.pressKey("2")

        assertEquals(SearchKeyboardFormat.MultiPress, controller.state.settings.searchKeyboardFormat)
        assertEquals("B", controller.state.searchDigits)
    }

    /** Confirms the second Settings row toggles touch launch and survives Home navigation. */
    @Test
    fun touchSettingSurvivesReturningHome() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Settings)
        controller.pressKey("↓")
        controller.pressKey("OK")

        controller.pressSoftAction(SoftAction.Home)

        assertEquals(true, controller.state.settings.touchToLaunchShortcuts)
    }

    /** Confirms Settings navigation reaches and stops on the launcher-selection action. */
    @Test
    fun settingsNavigationStopsAtChangeLauncher() {
        val base = LauncherUiState(screen = LauncherScreen.Settings)

        val second = reduceNavigation(base, NavigationDirection.Down, emptyList())
        val third = reduceNavigation(second, NavigationDirection.Down, emptyList())
        val pastThird = reduceNavigation(third, NavigationDirection.Down, emptyList())

        assertEquals(2, pastThird.selectedSettingIndex)
    }

    /** Confirms editor focus stops at its first and final vertically arranged properties. */
    @Test
    fun editorOverviewDoesNotWrap() {
        val base = LauncherUiState(screen = LauncherScreen.FavoriteEditor, editorField = FavoriteField.Icon, editorDraft = FavoriteDraft())

        val aboveFirst = reduceNavigation(base, NavigationDirection.Up, FavoriteTargets.defaults())
        val belowLast = reduceNavigation(base.copy(editorField = FavoriteField.App), NavigationDirection.Down, FavoriteTargets.defaults())

        assertEquals(FavoriteField.Icon, aboveFirst.editorField)
        assertEquals(FavoriteField.App, belowLast.editorField)
    }

    /** Confirms an absent cell in an incomplete grid cannot cause a diagonal focus jump. */
    @Test
    fun incompleteGridStopsAtMissingCell() {
        val favorites = FavoriteTargets.defaults().take(5)
        val state = LauncherUiState(screen = LauncherScreen.QuickLaunch, selectedFavoriteIndex = 2)

        val result = reduceNavigation(state, NavigationDirection.Down, favorites)

        assertEquals(2, result.selectedFavoriteIndex)
    }

    /** Confirms touch-mode page keys preserve focus while directional keys still navigate the grid. */
    @Test
    fun touchShortcutsSupportFocusedGridNavigationAndPaging() {
        val favorites = FavoriteTargets.defaults()
        val state = LauncherUiState(
            screen = LauncherScreen.QuickLaunch,
            selectedFavoriteIndex = 2,
            settings = LauncherSettings(touchToLaunchShortcuts = true),
        )

        val nextGridRow = reduceNavigation(state, NavigationDirection.Down, favorites)
        val nextPage = moveFavoritePageIndex(state.selectedFavoriteIndex, NavigationDirection.Down, favorites.size)
        val previousPage = moveFavoritePageIndex(nextPage, NavigationDirection.Up, favorites.size)

        assertEquals(5, nextGridRow.selectedFavoriteIndex)
        assertEquals(QuickLaunchPageSize + 2, nextPage)
        assertEquals(2, previousPage)
    }

    /** Confirms dedicated page labels are handled only after touch-mode Quick Launch is enabled. */
    @Test
    fun touchShortcutsUseDedicatedPageKeys() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Settings)
        controller.pressKey("↓")
        controller.pressKey("OK")
        controller.pressSoftAction(SoftAction.Quick)

        controller.pressKey("PG↓")

        assertEquals(QuickLaunchPageSize, controller.state.selectedFavoriteIndex)
    }

    /** Confirms Back from name entry restores the value present before the keyboard opened. */
    @Test
    fun backFromNameEditingRevertsTypedText() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Quick)
        controller.pressSoftAction(SoftAction.Edit)
        controller.pressKey("↓")
        controller.pressKey("OK")
        controller.updateFavoriteName("Changed")

        controller.handleBack()

        assertEquals("Phone", controller.state.editorDraft?.label)
        assertEquals(FavoriteEditorMode.Overview, controller.state.editorMode)
    }

    /** Confirms Settings initializes the shared draft collection before Save replaces a slot. */
    @Test
    fun settingsEditorCanSaveSafely() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Settings)
        controller.pressSoftAction(SoftAction.Edit)

        controller.pressSoftAction(SoftAction.Save)

        assertEquals(LauncherScreen.QuickLaunch, controller.state.screen)
    }

    /** Confirms holding OK enters move mode and Done persists the reordered Quick Launch grid. */
    @Test
    fun longPressOkMovesSelectedFavorite() {
        val controller = controller()
        val initiallyFirst = controller.favorites.first()
        controller.pressSoftAction(SoftAction.Quick)
        controller.longPressKey("OK")
        controller.pressKey("→")

        controller.pressSoftAction(SoftAction.Done)

        assertEquals(LauncherScreen.QuickLaunch, controller.state.screen)
        assertEquals(1, controller.state.selectedFavoriteIndex)
        assertEquals(initiallyFirst, controller.favorites[1])
    }

    /** Confirms Delete requires Done in its confirmation mode before removing a favorite. */
    @Test
    fun deleteRequiresConfirmation() {
        val controller = controller()
        controller.pressSoftAction(SoftAction.Quick)
        controller.pressSoftAction(SoftAction.Edit)
        val originalCount = controller.favorites.size

        controller.pressSoftAction(SoftAction.Delete)
        assertEquals(FavoriteEditorMode.DeleteConfirmation, controller.state.editorMode)
        assertEquals(originalCount, controller.favorites.size)

        controller.pressSoftAction(SoftAction.Done)
        assertEquals(originalCount - 1, controller.favorites.size)
        assertEquals(LauncherScreen.QuickLaunch, controller.state.screen)
    }

    /** Creates a controller whose collaborators perform no platform or persistence effects. */
    private fun controller(): LauncherController = LauncherController(
        scope = CoroutineScope(Dispatchers.Unconfined),
        favoritesRepository = FakeFavoriteRepository,
        settingsRepository = FakeSettingsRepository,
        appCatalog = FakeAppCatalog,
        navigator = FakeNavigator,
    )
}

/** Keeps setting tests deterministic without Android DataStore. */
private object FakeSettingsRepository : LauncherSettingsRepository {
    override suspend fun load(): LauncherSettings = LauncherSettings()
    override suspend fun save(settings: LauncherSettings) = Unit
}

/** Supplies deterministic seed favorites without external storage. */
private object FakeFavoriteRepository : FavoriteRepository {
    override suspend fun load(): List<QuickLaunchFavorite> = FavoriteTargets.defaults()
    override suspend fun save(favorites: List<QuickLaunchFavorite>) = Unit
}

/** Supplies no installed applications because navigation tests do not launch picker results. */
private object FakeAppCatalog : LaunchableAppCatalog {
    override fun load(): List<LaunchableApp> = emptyList()
}

/** Accepts every handoff without touching Android activities. */
private object FakeNavigator : ExternalNavigator {
    override fun launchApp(app: LaunchableApp): HandoffResult = HandoffResult.Started
    override fun launchFavorite(target: FavoriteLaunchTarget): HandoffResult = HandoffResult.Started
    override fun openDialer(number: String): HandoffResult = HandoffResult.Started
    override fun openTextMessage(number: String): HandoffResult = HandoffResult.Started
    override fun openHomeSettings(): HandoffResult = HandoffResult.Started
}
