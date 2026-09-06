/**
 * Defines the immutable state and value objects that describe FlipLauncher's keypad-first UI.
 *
 * The types in this file deliberately contain no Android dependencies so navigation behavior can
 * be tested independently from Compose and platform services.
 */
package com.fliplauncher.app.ui

/** Number of columns used by every Quick Launch grid. */
internal const val QuickLaunchColumnCount = 3

/** Number of favorites displayed on one Quick Launch LCD page. */
internal const val QuickLaunchPageSize = 6

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

/** Selects how telephone keypad presses are translated into an app search query. */
internal enum class SearchKeyboardFormat {
    T9,
    MultiPress,
}

/** Stores the two user-configurable launcher behaviors. */
internal data class LauncherSettings(
    val searchKeyboardFormat: SearchKeyboardFormat = SearchKeyboardFormat.T9,
    val touchToLaunchShortcuts: Boolean = false,
)

/** Identifies the three editable properties of a Quick Launch favorite. */
internal enum class FavoriteField {
    Icon,
    Name,
    App,
}

/** Identifies the single active interaction nested inside the favorite editor. */
internal enum class FavoriteEditorMode {
    Overview,
    NameEditing,
    IconPicker,
    AppPicker,
    Move,
    DeleteConfirmation,
}

/** Identifies one semantic action assigned to a colored contextual button. */
internal enum class SoftAction {
    None,
    Search,
    Quick,
    Settings,
    Backspace,
    ToggleSearchMode,
    Home,
    Edit,
    Add,
    Save,
    Move,
    Cancel,
    Back,
    Done,
    Delete,
    Text,
    Call,
}

/** Supplies the monochrome glyph chosen for a Quick Launch favorite. */
internal enum class FavoriteIcon {
    Phone,
    Message,
    Camera,
    Browser,
    Map,
    Photos,
    Calendar,
    Clock,
    Contacts,
    Mail,
    Music,
    Apps,
    Calculator,
    Weather,
    Notes,
    Video,
    Store,
    Files,
    Radio,
    Podcasts,
    Games,
    Wallet,
    Fitness,
    News,
    Bird,
    Home,
    Person,
    Groups,
    Favorite,
    Star,
    Work,
    School,
    Book,
    Lightbulb,
    Shopping,
    Restaurant,
    Coffee,
    Car,
    Bike,
    Train,
    Flight,
    Hotel,
    Park,
    Gas,
    Hospital,
    Medication,
    Emergency,
    Wifi,
    Bluetooth,
    Headphones,
    Microphone,
    Notifications,
    Security,
    Key,
    Print,
    QrCode,
    Translate,
    Explore,
    Savings,
    Cleaning,
}

/** Describes a Pixel-style mock app available to the Quick Launch editor. */
internal data class FavoriteTarget(
    val id: String,
    val appName: String,
    val packageName: String,
    val labels: List<String>,
    val defaultIcon: FavoriteIcon,
)

/** Identifies an app activity that can be launched from a Quick Launch favorite. */
internal data class FavoriteLaunchTarget(
    val appName: String,
    val packageName: String,
    val className: String? = null,
)

/** Represents one persisted Quick Launch slot; the launcher pages arbitrary slot counts. */
internal data class QuickLaunchFavorite(
    val target: FavoriteLaunchTarget,
    val label: String,
    val icon: FavoriteIcon,
)

/** Holds mutable-in-concept editor selections as an immutable value. */
internal data class FavoriteDraft(
    val target: FavoriteLaunchTarget? = null,
    val label: String = "",
    val icon: FavoriteIcon = FavoriteIcon.Apps,
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
    val selectedSettingIndex: Int = 0,
    val settings: LauncherSettings = LauncherSettings(),
    val editorField: FavoriteField = FavoriteField.Icon,
    val editorSlotIndex: Int = 0,
    val editorDraft: FavoriteDraft? = null,
    val isAddingFavorite: Boolean = false,
    val editorMode: FavoriteEditorMode = FavoriteEditorMode.Overview,
    val selectedFavoriteIconIndex: Int = 0,
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
        FavoriteTarget("calendar", "Calendar", "com.google.android.calendar", listOf("Calendar", "Agenda"), FavoriteIcon.Calendar),
        FavoriteTarget("clock", "Clock", "com.google.android.deskclock", listOf("Clock", "Alarms"), FavoriteIcon.Clock),
        FavoriteTarget("contacts", "Contacts", "com.google.android.contacts", listOf("Contacts", "People"), FavoriteIcon.Contacts),
        FavoriteTarget("gmail", "Gmail", "com.google.android.gm", listOf("Gmail", "Mail"), FavoriteIcon.Mail),
        FavoriteTarget("music", "Music", "com.youtube.music", listOf("Music", "Listen"), FavoriteIcon.Music),
    )

    /** Returns a target by identifier, or Phone when persisted data is invalid. */
    fun find(id: String): FavoriteTarget = all.firstOrNull { it.id == id } ?: all.first()

    /** Returns one initial favorite for every supplied mock target. */
    fun defaults(): List<QuickLaunchFavorite> = all.map { target ->
        QuickLaunchFavorite(
            target = FavoriteLaunchTarget(target.appName, target.packageName),
            label = target.labels.first(),
            icon = target.defaultIcon,
        )
    }
}
