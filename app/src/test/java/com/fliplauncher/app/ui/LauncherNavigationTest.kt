/** Verifies FlipLauncher's keypad navigation boundaries and nested editor cancellation rules. */
package com.fliplauncher.app.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises pure grid transitions and controller-level editor navigation with in-memory fakes. */
class LauncherNavigationTest {
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
        appCatalog = FakeAppCatalog,
        navigator = FakeNavigator,
    )
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
}
