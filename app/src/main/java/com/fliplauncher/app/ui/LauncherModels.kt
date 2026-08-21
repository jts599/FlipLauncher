/**
 * Defines the immutable state and value objects that describe FlipLauncher's keypad-first UI.
 *
 * The types in this file deliberately contain no Android dependencies so navigation behavior can
 * be tested independently from Compose and platform services.
 */
package com.fliplauncher.app.ui

/** Identifies the complete LCD view currently displayed by the launcher. */
internal enum class LauncherScreen {
    Home,
    Search,
    QuickLaunch,
    Settings,
    FavoriteEditor,
    Dialer,
}

/** Identifies the labels and behavior rendered on the physical-looking keypad. */
internal enum class KeypadMode {
    Telephone,
    Navigation,
}

/** Distinguishes T9 entry from focused result browsing in Search. */
internal enum class SearchMode {
    Entry,
    Results,
}

/** Identifies the three editable properties of a Quick Launch favorite. */
internal enum class FavoriteField {
    App,
    Label,
    Icon,
}

/** Supplies the monochrome glyph chosen for a Quick Launch favorite. */
internal enum class FavoriteIcon(val glyph: String) {
    Phone("☎"),
    Message("✉"),
    Camera("◉"),
    Browser("◎"),
    Map("⌖"),
    Photos("▣"),
}

/** Describes a Pixel-style mock app available to the Quick Launch editor. */
internal data class FavoriteTarget(
    val id: String,
    val appName: String,
    val packageName: String,
    val labels: List<String>,
    val defaultIcon: FavoriteIcon,
)

/** Represents one persisted slot in the six-item Quick Launch grid. */
internal data class QuickLaunchFavorite(
    val targetId: String,
    val label: String,
    val icon: FavoriteIcon,
)

/** Holds mutable-in-concept editor selections as an immutable value. */
internal data class FavoriteDraft(
    val targetId: String,
    val label: String,
    val icon: FavoriteIcon,
)

/** Describes one Android activity that FlipLauncher can show in Search and launch explicitly. */
internal data class LaunchableApp(
    val label: String,
    val packageName: String,
    val className: String,
)

/** Captures all view state required to draw and operate the launcher. */
internal data class LauncherUiState(
    val screen: LauncherScreen = LauncherScreen.Home,
    val searchMode: SearchMode = SearchMode.Entry,
    val searchDigits: String = "",
    val selectedSearchIndex: Int = 0,
    val selectedFavoriteIndex: Int = 0,
    val editorField: FavoriteField = FavoriteField.App,
    val editorSlotIndex: Int = 0,
    val editorDraft: FavoriteDraft? = null,
    val dialedNumber: String = "",
    val message: String? = null,
) {
    /** Returns the keypad presentation appropriate for the current state. */
    fun keypadMode(): KeypadMode = when (screen) {
        LauncherScreen.Home, LauncherScreen.Dialer -> KeypadMode.Telephone
        LauncherScreen.Search -> if (searchMode == SearchMode.Entry) KeypadMode.Telephone else KeypadMode.Navigation
        LauncherScreen.QuickLaunch, LauncherScreen.Settings, LauncherScreen.FavoriteEditor -> KeypadMode.Navigation
    }
}

/** Lists the static mock targets that seed and power the first Quick Launch editor. */
internal object FavoriteTargets {
    val all = listOf(
        FavoriteTarget("phone", "Phone", "com.google.android.dialer", listOf("Phone", "Call"), FavoriteIcon.Phone),
        FavoriteTarget("messages", "Messages", "com.google.android.apps.messaging", listOf("Messages", "Texts"), FavoriteIcon.Message),
        FavoriteTarget("camera", "Camera", "com.google.android.GoogleCamera", listOf("Camera", "Shoot"), FavoriteIcon.Camera),
        FavoriteTarget("chrome", "Chrome", "com.android.chrome", listOf("Chrome", "Web"), FavoriteIcon.Browser),
        FavoriteTarget("maps", "Maps", "com.google.android.apps.maps", listOf("Maps", "Navigate"), FavoriteIcon.Map),
        FavoriteTarget("photos", "Photos", "com.google.android.apps.photos", listOf("Photos", "Gallery"), FavoriteIcon.Photos),
    )

    /** Returns a target by identifier, or Phone when persisted data is invalid. */
    fun find(id: String): FavoriteTarget = all.firstOrNull { it.id == id } ?: all.first()

    /** Returns the six initial favorites, one for each mock target. */
    fun defaults(): List<QuickLaunchFavorite> = all.map { target ->
        QuickLaunchFavorite(target.id, target.labels.first(), target.defaultIcon)
    }
}
