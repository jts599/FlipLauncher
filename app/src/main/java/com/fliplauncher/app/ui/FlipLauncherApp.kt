package com.fliplauncher.app.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Date

private const val MillisPerMinute = 60_000L
private const val UnknownBatteryLevel = -1
private const val SearchResultWindowSize = 4
private const val FavoriteIconPageSize = 12
private val ScreenActionHeight = 42.dp
private val CellSignalBarHeights = listOf(6.dp, 10.dp, 14.dp, 18.dp)
private val KeyLabels = listOf(
    KeyLabel("1"), KeyLabel("2", "ABC"), KeyLabel("3", "DEF"),
    KeyLabel("4", "GHI"), KeyLabel("5", "JKL"), KeyLabel("6", "MNO"),
    KeyLabel("7", "PQRS"), KeyLabel("8", "TUV"), KeyLabel("9", "WXYZ"),
    KeyLabel("*"), KeyLabel("0", "+"), KeyLabel("#"),
)
private val NavigationKeyLabels = listOf(
    KeyLabel(""), KeyLabel("↑"), KeyLabel(""),
    KeyLabel("←"), KeyLabel("OK"), KeyLabel("→"),
    KeyLabel(""), KeyLabel("↓"), KeyLabel(""),
    KeyLabel(""), KeyLabel(""), KeyLabel(""),
)
private val VerticalNavigationKeyLabels = listOf(
    KeyLabel(""), KeyLabel("↑"), KeyLabel(""),
    KeyLabel(""), KeyLabel("OK"), KeyLabel(""),
    KeyLabel(""), KeyLabel("↓"), KeyLabel(""),
    KeyLabel(""), KeyLabel(""), KeyLabel(""),
)
private val DisabledKeyLabels = List(12) { KeyLabel("") }
private val ConfirmationKeyLabels = listOf(
    KeyLabel(""), KeyLabel(""), KeyLabel(""),
    KeyLabel(""), KeyLabel("OK"), KeyLabel(""),
    KeyLabel(""), KeyLabel(""), KeyLabel(""),
    KeyLabel(""), KeyLabel(""), KeyLabel(""),
)

/** Represents one visual key on the currently non-interactive numeric keypad. */
private data class KeyLabel(val primary: String, val secondary: String? = null)

/** Renders the static FlipLauncher home interface shown in the supplied HTML mockup. */
@Composable
fun FlipLauncherApp() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val controller = remember(context, scope) {
        LauncherController(
            scope,
            DataStoreFavoriteRepository(context),
            DataStoreLauncherSettingsRepository(context),
            AndroidLaunchableAppCatalog(context),
            AndroidExternalNavigator(context),
        )
    }
    val status = rememberLauncherStatus()
    LaunchedEffect(controller) { controller.load() }
    BackHandler { controller.handleBack() }
    FlipLauncherTheme {
        Box(Modifier.fillMaxSize()) {
            FlipPhoneFrame(status = status, controller = controller, modifier = Modifier.fillMaxSize())
            NativeAppPicker(
                visible = controller.isAppPickerVisible,
                apps = controller.apps,
                onDismiss = controller::dismissAppPicker,
                onSelect = controller::selectPickedApp,
            )
        }
    }
}

/** Draws the full-screen handset enclosure and arranges its screen, quick bars, and keypad. */
@Composable
private fun FlipPhoneFrame(status: LauncherStatus, controller: LauncherController, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RectangleShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(FlipColors.HousingTop, FlipColors.HousingBottom)))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            SpeakerSlot()
            Spacer(Modifier.height(12.dp))
            DisplayPanel(status = status, controller = controller, modifier = Modifier.weight(0.74f))
            Spacer(Modifier.height(14.dp))
            QuickActionBars(controller)
            Spacer(Modifier.height(14.dp))
            Keypad(controller, modifier = Modifier.weight(1.12f))
        }
    }
}

/** Draws the small speaker cutout above the display. */
@Composable
private fun SpeakerSlot() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.Black),
        )
    }
}

/** Draws the framed monochrome status screen. Screen symbols are intentionally decorative. */
@Composable
private fun DisplayPanel(status: LauncherStatus, controller: LauncherController, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .background(
                    Brush.verticalGradient(listOf(FlipColors.ScreenFrameTop, FlipColors.ScreenFrameBottom)),
                    RoundedCornerShape(18.dp),
                )
                .padding(10.dp)
                .background(
                    Brush.verticalGradient(listOf(FlipColors.BezelTop, FlipColors.BezelBottom)),
                    RoundedCornerShape(12.dp),
                )
                .padding(7.dp),
        ) {
            ScreenSurface(status = status, controller = controller)
        }
    }
}

/** Draws the LCD texture, status values, and soft-key glyphs in separate, non-overlapping regions. */
@Composable
private fun ScreenSurface(status: LauncherStatus, controller: LauncherController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(FlipColors.ScreenTop, FlipColors.ScreenBottom))),
    ) {
        BatteryReadout(
            batteryText = status.batteryText,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 14.dp),
        )
        CellSignalReadout(
            signalBars = status.signalBars,
            networkType = status.networkType,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 14.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(start = 14.dp, top = 38.dp, end = 14.dp, bottom = ScreenActionHeight),
            contentAlignment = Alignment.Center,
        ) {
            LauncherLcdContent(status, controller, Modifier)
        }
        ScreenActions(softActions(controller.state), controller.state, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** Renders the current cellular strength and radio technology in the LCD status row. */
@Composable
private fun CellSignalReadout(signalBars: Int, networkType: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CellSignalBars(signalBars)
        Text(
            text = networkType,
            color = FlipColors.ScreenInk,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

/** Renders the battery charge percentage beside a stylized battery outline. */
@Composable
private fun BatteryReadout(batteryText: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BatteryIcon(batteryFillFraction(batteryText))
        Spacer(Modifier.width(5.dp))
        Text(
            text = batteryText,
            color = FlipColors.ScreenInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

/**
 * Draws a battery silhouette whose fill reflects the live charge percentage.
 *
 * @param fillFraction Battery fill from zero through one. Values originate from [batteryFillFraction].
 */
@Composable
private fun BatteryIcon(fillFraction: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier
                .width(20.dp)
                .height(11.dp),
            shape = RectangleShape,
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, FlipColors.ScreenInk),
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFraction)
                        .background(FlipColors.ScreenInk),
                )
            }
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(5.dp)
                .background(FlipColors.ScreenInk),
        )
    }
}

/**
 * Converts the displayed battery percentage into a clamped icon fill fraction.
 *
 * @param batteryText Percentage text ending in `%`, or an unknown-state label.
 * @return A value from zero through one; malformed text produces an empty fill.
 */
internal fun batteryFillFraction(batteryText: String): Float =
    batteryText.removeSuffix("%").toIntOrNull()?.coerceIn(0, 100)?.div(100f) ?: 0f

/** Draws four stepped blocks to give the cellular signal meter a low-resolution LCD appearance. */
@Composable
private fun CellSignalBars(activeBarCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        CellSignalBarHeights.forEachIndexed { index, barHeight ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .background(FlipColors.ScreenInk.copy(alpha = if (index < activeBarCount) 1f else 0.18f)),
            )
        }
    }
}

/** Renders each state inside the existing LCD's central content area without adding touch targets. */
@Composable
private fun LauncherLcdContent(status: LauncherStatus, controller: LauncherController, modifier: Modifier) {
    val state = controller.state
    when (state.screen) {
        LauncherScreen.Home -> HomeLcdContent(status, state.message, modifier)
        LauncherScreen.Search -> SearchLcdContent(state, controller.filteredApps(), modifier)
        LauncherScreen.QuickLaunch -> QuickLaunchLcdContent(state, controller.favorites, controller::tapFavorite, modifier)
        LauncherScreen.Settings -> SettingsLcdContent(state, modifier)
        LauncherScreen.FavoriteEditor -> FavoriteEditorLcdContent(state, controller, modifier)
        LauncherScreen.Dialer -> DialerLcdContent(state, modifier)
    }
}

/** Preserves the original centered clock/date composition on Home. */
@Composable
private fun HomeLcdContent(status: LauncherStatus, message: String?, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        message?.let { LcdMessage(it) }
        Text(status.timeText, color = FlipColors.ScreenInk, fontSize = 48.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-4).sp)
        Text(status.dateText, color = FlipColors.ScreenInk.copy(alpha = 0.62f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
    }
}

/**
 * Renders a compact retro search field and the visible window of matching applications.
 *
 * @param state Current search query, interaction mode, and focused result index.
 * @param apps Applications matching the current T9 query, in display order.
 * @param modifier LCD layout constraints supplied by the phone display.
 */
@Composable
private fun SearchLcdContent(state: LauncherUiState, apps: List<LaunchableApp>, modifier: Modifier) {
    val firstVisibleIndex = searchResultWindowStart(state.selectedSearchIndex, state.searchMode)
    val visibleApps = apps.drop(firstVisibleIndex).take(SearchResultWindowSize)
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(end = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            SearchField(state.searchDigits, state.settings.searchKeyboardFormat)
            if (visibleApps.isEmpty()) {
                SearchEmptyState(Modifier.weight(1f))
                repeat(SearchResultWindowSize - 1) { Spacer(Modifier.weight(1f)) }
            } else {
                repeat(SearchResultWindowSize) { index ->
                    val app = visibleApps.getOrNull(index)
                    if (app == null) {
                        Spacer(Modifier.weight(1f))
                        return@repeat
                    }
                    SearchResultRow(
                        label = app.label,
                        focused = state.searchMode == SearchMode.Results && firstVisibleIndex + index == state.selectedSearchIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            state.message?.let { LcdMessage(it) }
        }
        PageIndicator(
            pageIndex = firstVisibleIndex / SearchResultWindowSize,
            pageCount = itemPageCount(apps.size, SearchResultWindowSize),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/** Returns the first item in the four-row page containing the focused search result. */
internal fun searchResultWindowStart(selectedIndex: Int, mode: SearchMode): Int {
    if (mode == SearchMode.Entry) return 0
    return selectedIndex.coerceAtLeast(0) / SearchResultWindowSize * SearchResultWindowSize
}

/** Draws the outlined T9 query field at the top of the search display. */
@Composable
private fun SearchField(query: String, format: SearchKeyboardFormat) {
    val placeholder = if (format == SearchKeyboardFormat.T9) "TYPE 2–9" else "TYPE LETTERS"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .border(1.dp, FlipColors.ScreenInk, RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(15.dp), tint = FlipColors.ScreenInk)
        Spacer(Modifier.width(7.dp))
        Text(
            text = query.ifEmpty { placeholder },
            color = FlipColors.ScreenInk.copy(alpha = if (query.isEmpty()) 0.58f else 1f),
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
        )
    }
}

/**
 * Draws one search result at the height allocated by its paged result slot.
 *
 * @param label Result label displayed to the user.
 * @param focused Whether keypad navigation currently targets this result.
 * @param modifier Layout constraints from the fixed four-row search result area.
 * @return Unit. Emits Compose UI only; no external effects or expected errors.
 */
@Composable
private fun SearchResultRow(label: String, focused: Boolean, modifier: Modifier = Modifier) {
    val background = if (focused) FlipColors.ScreenInk else Color.Transparent
    val foreground = if (focused) FlipColors.ScreenTop else FlipColors.ScreenInk
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .background(background, RoundedCornerShape(2.dp))
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (focused) "›" else " ", color = foreground, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(5.dp))
        Text(label, color = foreground, fontSize = 14.sp, fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
    }
}

/**
 * Displays concise feedback in an empty search result area.
 *
 * @param modifier Layout constraints supplied by the first reserved search result slot.
 * @return Unit. Emits Compose UI only; no external effects or expected errors.
 */
@Composable
private fun SearchEmptyState(modifier: Modifier = Modifier) {
    Text(
        text = "NO APPS FOUND",
        modifier = modifier.padding(horizontal = 7.dp, vertical = 6.dp),
        color = FlipColors.ScreenInk.copy(alpha = 0.68f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    )
}

/**
 * Renders the Quick Launch grid as large, centered app icons with compact labels.
 *
 * @param state Current launcher state; its selected favorite determines the page and receives focus outside touch mode.
 * @param favorites Configured Quick Launch entries, displayed in a two-column grid.
 * @param modifier Layout constraints supplied by the LCD container.
 */
@Composable
private fun QuickLaunchLcdContent(
    state: LauncherUiState,
    favorites: List<QuickLaunchFavorite>,
    onFavoriteTap: (Int) -> Unit,
    modifier: Modifier,
) {
    val pageIndex = state.selectedFavoriteIndex / QuickLaunchPageSize
    val pageCount = favorites.pageCount()
    val firstItemIndex = pageIndex * QuickLaunchPageSize
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            favorites.drop(firstItemIndex).take(QuickLaunchPageSize).chunked(QuickLaunchColumnCount).forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(QuickLaunchColumnCount) { columnIndex ->
                        val favorite = row.getOrNull(columnIndex)
                        if (favorite == null) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            FavoriteLcdTile(
                                favorite = favorite,
                                focused = !state.settings.touchToLaunchShortcuts &&
                                    firstItemIndex + rowIndex * QuickLaunchColumnCount + columnIndex == state.selectedFavoriteIndex,
                                modifier = Modifier.weight(1f),
                                onClick = { onFavoriteTap(firstItemIndex + rowIndex * QuickLaunchColumnCount + columnIndex) },
                                touchEnabled = state.settings.touchToLaunchShortcuts,
                            )
                        }
                    }
                }
            }
            state.message?.let { LcdMessage(it) }
        }
        PageIndicator(
            pageIndex = pageIndex,
            pageCount = pageCount,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/** Returns the number of six-slot LCD pages needed for this favorite collection. */
private fun List<QuickLaunchFavorite>.pageCount(): Int = (size + QuickLaunchPageSize - 1) / QuickLaunchPageSize

/** Returns the number of fixed-size pages needed to display an item count. */
private fun itemPageCount(itemCount: Int, pageSize: Int): Int = (itemCount + pageSize - 1) / pageSize

/** Renders the two launcher-wide preferences as bold keypad-focused rows. */
@Composable
private fun SettingsLcdContent(state: LauncherUiState, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        SettingRow("SEARCH KEYBOARD", if (state.settings.searchKeyboardFormat == SearchKeyboardFormat.T9) "T9" else "MULTI-PRESS", state.selectedSettingIndex == 0)
        SettingRow("TOUCH TO LAUNCH", if (state.settings.touchToLaunchShortcuts) "ON" else "OFF", state.selectedSettingIndex == 1)
        SettingRow("CHANGE LAUNCHER", "OPEN", state.selectedSettingIndex == 2)
        state.message?.let { LcdMessage(it) }
    }
}

/** Draws one labeled preference with its current value and inverse focused treatment. */
@Composable
private fun SettingRow(label: String, value: String, focused: Boolean) {
    val background = if (focused) FlipColors.ScreenInk else Color.Transparent
    val foreground = if (focused) FlipColors.ScreenTop else FlipColors.ScreenInk
    Row(
        Modifier.fillMaxWidth().height(43.dp).background(background, RoundedCornerShape(2.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(value, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    }
}

/** Renders and edits the icon, custom name, and installed app selected for one favorite. */
@Composable
private fun FavoriteEditorLcdContent(state: LauncherUiState, controller: LauncherController, modifier: Modifier) {
    val draft = state.editorDraft ?: return
    if (state.editorMode == FavoriteEditorMode.IconPicker) {
        FavoriteIconPickerLcdContent(state, modifier)
        return
    }
    if (state.editorMode == FavoriteEditorMode.Move) {
        FavoriteMoveLcdContent(state, controller.editorPreviewFavorites(), modifier)
        return
    }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        EditorValueIcon(draft.icon, state.editorField == FavoriteField.Icon)
        if (state.editorMode == FavoriteEditorMode.NameEditing) {
            EditorNameInput(draft, controller)
        } else {
            Text(
                text = draft.label.ifBlank { if (state.isAddingFavorite) "NEW APP" else "UNTITLED" },
                color = FlipColors.ScreenInk.copy(alpha = if (state.editorField == FavoriteField.Name) 1f else 0.68f),
                fontSize = 16.sp,
                fontWeight = if (state.editorField == FavoriteField.Name) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
        Text(
            text = draft.target?.packageName ?: "SELECT APP",
            color = FlipColors.ScreenInk.copy(alpha = if (state.editorField == FavoriteField.App) 1f else 0.52f),
            fontSize = 9.sp,
            fontWeight = if (state.editorField == FavoriteField.App) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
        if (state.editorMode == FavoriteEditorMode.DeleteConfirmation) LcdMessage("DELETE THIS APP?")
        state.message?.let { LcdMessage(it) }
    }
}

/** Draws the large chosen icon and outlines it while the icon value has keypad focus. */
@Composable
private fun EditorValueIcon(icon: FavoriteIcon, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .then(if (selected) Modifier.border(1.dp, FlipColors.ScreenInk, RoundedCornerShape(4.dp)) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        FavoriteIconGraphic(icon, iconSize = 42.dp)
    }
}

/** Renders the software keyboard field only after the focused display-name value is confirmed. */
@Composable
private fun EditorNameInput(draft: FavoriteDraft, controller: LauncherController) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
    BasicTextField(
        value = draft.label,
        onValueChange = controller::updateFavoriteName,
        modifier = Modifier.focusRequester(focusRequester).fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = FlipColors.ScreenInk, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { controller.finishNameEditing() }),
        decorationBox = { innerTextField -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { innerTextField() } },
    )
}

/** Displays every available monochrome icon in a full-screen three-column selection grid. */
@Composable
private fun FavoriteIconPickerLcdContent(state: LauncherUiState, modifier: Modifier) {
    val icons = FavoriteIcon.values().toList()
    val pageIndex = state.selectedFavoriteIconIndex / FavoriteIconPageSize
    val firstIconIndex = pageIndex * FavoriteIconPageSize
    Box(modifier.fillMaxSize()) {
        FavoriteIconPickerGrid(
            icons = icons.drop(firstIconIndex).take(FavoriteIconPageSize),
            firstIconIndex = firstIconIndex,
            selectedIconIndex = state.selectedFavoriteIconIndex,
            modifier = Modifier.fillMaxWidth().padding(end = 9.dp),
        )
        PageIndicator(pageIndex, itemPageCount(icons.size, FavoriteIconPageSize), Modifier.align(Alignment.CenterEnd))
    }
}

/**
 * Draws icon-picker rows with fixed-width slots, including blank trailing slots on incomplete rows.
 *
 * @param icons Icons displayed in this page of the picker.
 * @param firstIconIndex Zero-based index of [icons]' first item in the complete icon library.
 * @param selectedIconIndex Zero-based focused item index in the complete icon library.
 * @param modifier Optional placement constraints for the grid.
 * @return Unit. Emits Compose UI only; no external effects or expected errors.
 */
@Composable
internal fun FavoriteIconPickerGrid(
    icons: List<FavoriteIcon>,
    firstIconIndex: Int,
    selectedIconIndex: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        icons.chunked(QuickLaunchColumnCount).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                repeat(QuickLaunchColumnCount) { columnIndex ->
                    val icon = row.getOrNull(columnIndex)
                    if (icon == null) {
                        Spacer(Modifier.weight(1f))
                        return@repeat
                    }
                    val iconIndex = firstIconIndex + rowIndex * QuickLaunchColumnCount + columnIndex
                    IconPickerCell(icon, iconIndex == selectedIconIndex, Modifier.weight(1f))
                }
            }
        }
    }
}

/** Draws one fixed-size cell in the paged monochrome favorite-icon library. */
@Composable
private fun IconPickerCell(icon: FavoriteIcon, selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight().padding(horizontal = 4.dp)
            .semantics { contentDescription = "${icon.name} icon" }
            .then(if (selected) Modifier.border(1.dp, FlipColors.ScreenInk, RoundedCornerShape(4.dp)) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        FavoriteIconGraphic(icon, iconSize = 25.dp)
    }
}

/** Renders the editable Quick Launch grid during temporary reorder mode. */
@Composable
private fun FavoriteMoveLcdContent(state: LauncherUiState, favorites: List<QuickLaunchFavorite>, modifier: Modifier) {
    val pageIndex = state.editorSlotIndex / QuickLaunchPageSize
    val firstItemIndex = pageIndex * QuickLaunchPageSize
    Box(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            favorites.drop(firstItemIndex).take(QuickLaunchPageSize).chunked(QuickLaunchColumnCount).forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(QuickLaunchColumnCount) { columnIndex ->
                        val favorite = row.getOrNull(columnIndex)
                        if (favorite == null) Spacer(Modifier.weight(1f)) else FavoriteLcdTile(
                            favorite = favorite,
                            focused = firstItemIndex + rowIndex * QuickLaunchColumnCount + columnIndex == state.editorSlotIndex,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            LcdMessage("ARROWS TO MOVE")
        }
        PageIndicator(pageIndex, favorites.pageCount(), Modifier.align(Alignment.CenterEnd))
    }
}

/** Opens a native searchable bottom sheet for choosing the launcher activity behind a favorite. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NativeAppPicker(
    visible: Boolean,
    apps: List<LaunchableApp>,
    onDismiss: () -> Unit,
    onSelect: (LaunchableApp) -> Unit,
) {
    if (!visible) return
    var query by remember { mutableStateOf("") }
    val matches = remember(query, apps) { apps.filter { app -> app.label.contains(query, ignoreCase = true) } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text("Choose app", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search apps") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp).padding(top = 10.dp)) {
                items(matches, key = { app -> "${app.packageName}/${app.className}" }) { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(app) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InstalledAppIcon(app)
                        Spacer(Modifier.width(14.dp))
                        Text(text = app.label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
        }
    }
}

/** Displays an installed activity's launcher icon inside the native app picker row. */
@Composable
private fun InstalledAppIcon(app: LaunchableApp) {
    val context = LocalContext.current
    val icon = remember(app.packageName, app.className) {
        runCatching { context.packageManager.getActivityIcon(android.content.ComponentName(app.packageName, app.className)) }
            .getOrElse { runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull() }
    }
    AndroidView(
        factory = { viewContext -> ImageView(viewContext).apply { scaleType = ImageView.ScaleType.CENTER_INSIDE } },
        update = { imageView -> imageView.setImageDrawable(icon) },
        modifier = Modifier.size(36.dp),
    )
}

/** Renders Dialer's number and any local validation or handoff feedback. */
@Composable
private fun DialerLcdContent(state: LauncherUiState, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        LcdTitle("DIAL")
        Text(state.dialedNumber.ifEmpty { "ENTER NUMBER" }, color = FlipColors.ScreenInk, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        state.message?.let { LcdMessage(it) }
    }
}

/** Renders a compact LCD heading. */
@Composable private fun LcdTitle(text: String) = Text(text, color = FlipColors.ScreenInk, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
/** Renders a focused or ordinary LCD list row. */
@Composable private fun LcdRow(text: String, focused: Boolean, modifier: Modifier = Modifier) = Text(if (focused) "› $text" else "  $text", modifier, color = FlipColors.ScreenInk, fontSize = 11.sp, fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
/** Renders concise feedback inside the LCD. */
@Composable private fun LcdMessage(text: String) = Text(text, color = FlipColors.ScreenInk.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)

/**
 * Renders one Quick Launch entry with its app icon as the visual priority.
 *
 * @param favorite Favorite configuration supplying the icon and label.
 * @param focused Whether this tile is selected by the keypad cursor.
 * @param modifier Layout constraints for this grid cell.
 */
@Composable
private fun FavoriteLcdTile(
    favorite: QuickLaunchFavorite,
    focused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    touchEnabled: Boolean = false,
) {
    val focusModifier = if (focused) {
        Modifier.border(1.dp, FlipColors.ScreenInk, RoundedCornerShape(4.dp))
    } else {
        Modifier
    }
    Column(
        modifier = modifier.clickable(enabled = touchEnabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .then(focusModifier)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FavoriteIconGraphic(favorite.icon, iconSize = 29.dp)
        Text(
            text = favorite.label,
            color = FlipColors.ScreenInk,
            fontSize = 9.sp,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/** Renders a compact favorite row for Settings using the shared monochrome icon system. */
@Composable
private fun FavoriteLcdRow(favorite: QuickLaunchFavorite, focused: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (focused) "›" else " ", color = FlipColors.ScreenInk, fontWeight = FontWeight.Bold)
        FavoriteIconGraphic(favorite.icon, Modifier.padding(horizontal = 2.dp))
        Text(favorite.label, color = FlipColors.ScreenInk, fontSize = 11.sp, fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
    }
}

/**
 * Renders the filled monochrome glyph assigned to a favorite.
 *
 * @param icon Configured semantic app icon.
 * @param modifier Controls the glyph's placement.
 * @param iconSize Rendered size; callers increase it for Quick Launch tiles.
 */
@Composable
private fun FavoriteIconGraphic(icon: FavoriteIcon, modifier: Modifier = Modifier, iconSize: Dp = 15.dp) {
    val image = when (icon) {
        FavoriteIcon.Phone -> Icons.Filled.Call
        FavoriteIcon.Message -> Icons.Filled.Message
        FavoriteIcon.Camera -> Icons.Filled.PhotoCamera
        FavoriteIcon.Browser -> Icons.Filled.Language
        FavoriteIcon.Map -> Icons.Filled.Map
        FavoriteIcon.Photos -> Icons.Filled.Photo
        FavoriteIcon.Calendar -> Icons.Filled.CalendarToday
        FavoriteIcon.Clock -> Icons.Filled.Schedule
        FavoriteIcon.Contacts -> Icons.Filled.Contacts
        FavoriteIcon.Mail -> Icons.Filled.Mail
        FavoriteIcon.Music -> Icons.Filled.MusicNote
        FavoriteIcon.Apps -> Icons.Filled.Apps
        FavoriteIcon.Calculator -> Icons.Filled.Calculate
        FavoriteIcon.Weather -> Icons.Filled.Cloud
        FavoriteIcon.Notes -> Icons.Filled.Note
        FavoriteIcon.Video -> Icons.Filled.SmartDisplay
        FavoriteIcon.Store -> Icons.Filled.Storefront
        FavoriteIcon.Files -> Icons.Filled.Folder
        FavoriteIcon.Radio -> Icons.Filled.Radio
        FavoriteIcon.Podcasts -> Icons.Filled.Podcasts
        FavoriteIcon.Games -> Icons.Filled.SportsEsports
        FavoriteIcon.Wallet -> Icons.Filled.AccountBalanceWallet
        FavoriteIcon.Fitness -> Icons.Filled.FitnessCenter
        FavoriteIcon.News -> Icons.Filled.Newspaper
        FavoriteIcon.Bird -> Icons.Filled.FlutterDash
        FavoriteIcon.Home -> Icons.Filled.Home
        FavoriteIcon.Person -> Icons.Filled.Person
        FavoriteIcon.Groups -> Icons.Filled.Groups
        FavoriteIcon.Favorite -> Icons.Filled.Favorite
        FavoriteIcon.Star -> Icons.Filled.Star
        FavoriteIcon.Work -> Icons.Filled.Work
        FavoriteIcon.School -> Icons.Filled.School
        FavoriteIcon.Book -> Icons.Filled.Book
        FavoriteIcon.Lightbulb -> Icons.Filled.Lightbulb
        FavoriteIcon.Shopping -> Icons.Filled.ShoppingCart
        FavoriteIcon.Restaurant -> Icons.Filled.Restaurant
        FavoriteIcon.Coffee -> Icons.Filled.Coffee
        FavoriteIcon.Car -> Icons.Filled.DirectionsCar
        FavoriteIcon.Bike -> Icons.Filled.DirectionsBike
        FavoriteIcon.Train -> Icons.Filled.Train
        FavoriteIcon.Flight -> Icons.Filled.Flight
        FavoriteIcon.Hotel -> Icons.Filled.Hotel
        FavoriteIcon.Park -> Icons.Filled.Park
        FavoriteIcon.Gas -> Icons.Filled.LocalGasStation
        FavoriteIcon.Hospital -> Icons.Filled.LocalHospital
        FavoriteIcon.Medication -> Icons.Filled.Medication
        FavoriteIcon.Emergency -> Icons.Filled.Emergency
        FavoriteIcon.Wifi -> Icons.Filled.Wifi
        FavoriteIcon.Bluetooth -> Icons.Filled.Bluetooth
        FavoriteIcon.Headphones -> Icons.Filled.Headphones
        FavoriteIcon.Microphone -> Icons.Filled.Mic
        FavoriteIcon.Notifications -> Icons.Filled.Notifications
        FavoriteIcon.Security -> Icons.Filled.Security
        FavoriteIcon.Key -> Icons.Filled.Key
        FavoriteIcon.Print -> Icons.Filled.Print
        FavoriteIcon.QrCode -> Icons.Filled.QrCode
        FavoriteIcon.Translate -> Icons.Filled.Translate
        FavoriteIcon.Explore -> Icons.Filled.Explore
        FavoriteIcon.Savings -> Icons.Filled.Savings
        FavoriteIcon.Cleaning -> Icons.Filled.CleaningServices
    }
    Icon(image, contentDescription = null, modifier = modifier.size(iconSize), tint = FlipColors.ScreenInk)
}

/** Renders decorative search, apps, and settings glyphs at the display's bottom edge. */
@Composable
private fun ScreenActions(actions: List<SoftAction>, state: LauncherUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ScreenActionHeight)
            .topBorder(FlipColors.ScreenInk.copy(alpha = 0.38f)),
    ) {
        actions.forEachIndexed { index, action ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .insideDivider(index, FlipColors.ScreenInk.copy(alpha = 0.38f))
                    .semantics { contentDescription = "${action.name} soft-key label" },
                contentAlignment = Alignment.Center,
            ) {
                SoftKeyIcon(action, state)
            }
        }
    }
}

/** Maps contextual actions onto the launcher-wide bold monochrome icon vocabulary. */
@Composable
private fun SoftKeyIcon(action: SoftAction, state: LauncherUiState) {
    if (action == SoftAction.None) return
    val image = when (action) {
        SoftAction.Search -> Icons.Filled.Search
        SoftAction.Quick -> Icons.Filled.Apps
        SoftAction.Settings -> Icons.Filled.Settings
        SoftAction.Delete -> Icons.Filled.Delete
        SoftAction.Text -> Icons.Filled.Textsms
        SoftAction.Call -> Icons.Filled.Call
        SoftAction.Backspace -> Icons.AutoMirrored.Filled.Backspace
        SoftAction.Cancel, SoftAction.Back -> Icons.Filled.Close
        SoftAction.Edit -> Icons.Filled.Edit
        SoftAction.Add -> Icons.Filled.Add
        SoftAction.Move -> Icons.Filled.Launch
        SoftAction.Save, SoftAction.Done -> Icons.Filled.Save
        SoftAction.Home -> Icons.Filled.Home
        SoftAction.ToggleSearchMode -> if (state.searchMode == SearchMode.Entry) Icons.Filled.UnfoldMore else Icons.Filled.Keyboard
        SoftAction.None -> return
    }
    Icon(image, contentDescription = action.name, modifier = Modifier.height(24.dp), tint = FlipColors.ScreenInk)
}

/** Draws the top boundary for the screen action strip without enclosing its outer edges. */
private fun Modifier.topBorder(color: Color): Modifier = drawBehind {
    drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
}

/** Draws a divider before every screen action except the leftmost action. */
private fun Modifier.insideDivider(index: Int, color: Color): Modifier {
    if (index == 0) return this
    return drawBehind {
        drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.dp.toPx())
    }
}

/** Returns the LCD labels activated by the red, yellow, and green action bars. */
private fun softActions(state: LauncherUiState): List<SoftAction> = when {
    state.screen == LauncherScreen.FavoriteEditor && state.editorMode in setOf(FavoriteEditorMode.IconPicker, FavoriteEditorMode.Move, FavoriteEditorMode.NameEditing, FavoriteEditorMode.DeleteConfirmation) ->
        listOf(SoftAction.Back, SoftAction.None, SoftAction.Done)
    state.screen == LauncherScreen.FavoriteEditor && state.editorMode == FavoriteEditorMode.AppPicker ->
        listOf(SoftAction.None, SoftAction.None, SoftAction.None)
    else -> when (state.screen) {
        LauncherScreen.Home -> listOf(SoftAction.Search, SoftAction.Quick, SoftAction.Settings)
        LauncherScreen.Search -> listOf(SoftAction.Backspace, SoftAction.ToggleSearchMode, SoftAction.Home)
        LauncherScreen.QuickLaunch -> listOf(SoftAction.Edit, SoftAction.Add, SoftAction.Home)
        LauncherScreen.Settings -> listOf(SoftAction.None, SoftAction.None, SoftAction.Home)
        LauncherScreen.FavoriteEditor -> listOf(SoftAction.Save, SoftAction.Delete, SoftAction.Cancel)
        LauncherScreen.Dialer -> listOf(SoftAction.Backspace, SoftAction.Text, SoftAction.Call)
    }
}

/** Draws the three colored, intentionally inactive quick-action bars. */
@Composable
private fun QuickActionBars(controller: LauncherController) {
    val actions = softActions(controller.state)
    val haptics = LocalHapticFeedback.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(FlipColors.Red, FlipColors.Yellow, FlipColors.Green).forEachIndexed { index, color ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics { contentDescription = "${actions[index].name} action" }
                    .clickable(enabled = actions[index] != SoftAction.None) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        controller.pressSoftAction(actions[index])
                    },
                shape = RoundedCornerShape(99.dp),
                color = FlipColors.KeyBottom,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}

/** Draws the twelve visual keypad buttons. No tap or hardware-key behavior is attached yet. */
@Composable
private fun Keypad(controller: LauncherController, modifier: Modifier = Modifier) {
    val keys = keypadLabels(controller.state)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            keys.chunked(3).forEach { row ->
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key -> KeypadKey(key = key, controller = controller, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * Returns only the physical controls that have meaning in the current launcher mode.
 *
 * Touch-mode Quick Launch exposes vertical page navigation only because individual icon focus is
 * intentionally disabled (issue #7).
 */
private fun keypadLabels(state: LauncherUiState): List<KeyLabel> = when (state.screen) {
    LauncherScreen.Home, LauncherScreen.Dialer -> KeyLabels
    LauncherScreen.Search -> if (state.searchMode == SearchMode.Entry) KeyLabels else VerticalNavigationKeyLabels
    LauncherScreen.QuickLaunch -> if (state.settings.touchToLaunchShortcuts) VerticalNavigationKeyLabels else NavigationKeyLabels
    LauncherScreen.Settings -> NavigationKeyLabels
    LauncherScreen.FavoriteEditor -> when (state.editorMode) {
        FavoriteEditorMode.Overview -> VerticalNavigationKeyLabels
        FavoriteEditorMode.IconPicker, FavoriteEditorMode.Move -> NavigationKeyLabels
        FavoriteEditorMode.DeleteConfirmation -> ConfirmationKeyLabels
        FavoriteEditorMode.NameEditing, FavoriteEditorMode.AppPicker -> DisabledKeyLabels
    }
}

/** Draws one visual key and its optional telephone-letter label. */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun KeypadKey(key: KeyLabel, controller: LauncherController, modifier: Modifier = Modifier) {
    val enabled = key.primary.isNotBlank()
    val haptics = LocalHapticFeedback.current
    val supportsLongPress = key.primary == "OK" && controller.state.screen == LauncherScreen.QuickLaunch
    val primaryWeight = if (key.primary in setOf("↑", "↓", "←", "→")) FontWeight.ExtraBold else FontWeight.Bold
    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = if (enabled) "Key ${key.primary}" else "Inactive keypad key" }
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    controller.pressKey(key.primary)
                },
                onLongClick = if (supportsLongPress) {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        controller.longPressKey(key.primary)
                    }
                } else null,
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(FlipColors.KeyTop, FlipColors.KeyBottom)))
                .padding(horizontal = 6.dp, vertical = 8.dp),
        ) {
            Text(
                text = key.primary,
                modifier = Modifier.align(Alignment.Center),
                color = FlipColors.KeyText,
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = primaryWeight,
                textAlign = TextAlign.Center,
            )
            key.secondary?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 42.dp),
                    color = FlipColors.KeySubtext,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * Holds the live system values required by the mockup's status screen.
 *
 * @property timeText Localized current time for the centered LCD readout.
 * @property dateText Localized current date shown beneath the centered time.
 * @property batteryText Formatted battery percentage, or an unknown-state label.
 * @property signalBars Number of active cellular bars from zero through four.
 * @property networkType Concise cellular radio label, or `--` when unavailable.
 */
private data class LauncherStatus(
    val timeText: String,
    val dateText: String,
    val batteryText: String,
    val signalBars: Int,
    val networkType: String,
)

/** Holds cellular values supplied by Android telephony callbacks. */
private data class CellularStatus(val signalBars: Int = 0, val networkType: String = "--")

/** Observes the local clock and battery broadcasts while the launcher composition is visible. */
@Composable
private fun rememberLauncherStatus(): LauncherStatus {
    val context = LocalContext.current
    var timeText by remember(context) { mutableStateOf(formattedTime(context)) }
    var dateText by remember(context) { mutableStateOf(formattedDate(context)) }
    var batteryText by remember { mutableStateOf("--%") }
    val cellularStatus = rememberCellularStatus(context)
    val wifiStatus = rememberWifiStatus(context)
    val connectionStatus = if (wifiStatus.isConnected) wifiStatus.asCellularStatus() else cellularStatus

    LaunchedEffect(context) {
        while (true) {
            timeText = formattedTime(context)
            dateText = formattedDate(context)
            delay(delayUntilNextMinute(System.currentTimeMillis()))
        }
    }
    DisposableEffect(context) {
        val receiver = BatteryReceiver { batteryText = it }
        val intent = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        batteryText = intent?.batteryText() ?: batteryText
        onDispose { context.unregisterReceiver(receiver) }
    }
    return LauncherStatus(timeText, dateText, batteryText, connectionStatus.signalBars, connectionStatus.networkType)
}

/** Holds the active Wi-Fi state used to override cellular status when connected. */
private data class WifiStatus(val isConnected: Boolean = false, val signalBars: Int = 0)

/** Converts a connected Wi-Fi reading into the shared LCD connection representation. */
private fun WifiStatus.asCellularStatus(): CellularStatus = CellularStatus(signalBars, "WI-FI")

/** Observes the active default network and publishes Wi-Fi RSSI while it is connected. */
@Composable
private fun rememberWifiStatus(context: Context): WifiStatus {
    var status by remember { mutableStateOf(WifiStatus()) }
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        val publishCurrentStatus = {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            status = capabilities.toWifiStatus(wifiManager)
        }
        val callback = wifiNetworkCallback(context, publishCurrentStatus)
        connectivityManager.registerDefaultNetworkCallback(callback)
        publishCurrentStatus()
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }
    return status
}

/** Creates a default-network callback that safely returns updates to the main thread. */
private fun wifiNetworkCallback(context: Context, publish: () -> Unit): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = context.mainExecutor.execute(publish)
        override fun onLost(network: Network) = context.mainExecutor.execute(publish)
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) =
            context.mainExecutor.execute(publish)
    }

/** Returns a four-bar Wi-Fi reading when these capabilities represent active Wi-Fi. */
@Suppress("DEPRECATION")
private fun NetworkCapabilities?.toWifiStatus(wifiManager: WifiManager): WifiStatus {
    if (this?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) return WifiStatus()
    val level = WifiManager.calculateSignalLevel(wifiManager.connectionInfo.rssi, CellSignalBarHeights.size + 1)
    return WifiStatus(isConnected = true, signalBars = level.coerceIn(0, CellSignalBarHeights.size))
}

/** Requests phone-state access and observes signal changes while the launcher is visible. */
@Composable
private fun rememberCellularStatus(context: Context): CellularStatus {
    var permissionGranted by remember(context) { mutableStateOf(context.hasPhoneStatePermission()) }
    var status by remember { mutableStateOf(CellularStatus()) }
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionRequest.launch(Manifest.permission.READ_PHONE_STATE)
    }
    DisposableEffect(context, permissionGranted) {
        if (!permissionGranted) return@DisposableEffect onDispose {}
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val listener = cellularPhoneStateListener(telephonyManager) { status = it }
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_DATA_CONNECTION_STATE)
        onDispose { telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE) }
    }
    return status
}

/** Returns whether the app may read cellular signal and network information. */
private fun Context.hasPhoneStatePermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

/** Creates the legacy-compatible listener needed on the app's Android 10 minimum SDK. */
@Suppress("DEPRECATION")
private fun cellularPhoneStateListener(
    telephonyManager: TelephonyManager,
    onStatusChanged: (CellularStatus) -> Unit,
): PhoneStateListener = object : PhoneStateListener() {
    private var signalBars = 0

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        signalBars = signalStrength.level.coerceIn(0, CellSignalBarHeights.size)
        onStatusChanged(CellularStatus(signalBars, telephonyManager.safeNetworkType().networkLabel()))
    }

    override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
        onStatusChanged(CellularStatus(signalBars, networkType.networkLabel()))
    }
}

/** Reads the current data radio type without allowing device-specific failures into the UI. */
private fun TelephonyManager.safeNetworkType(): Int = try {
    dataNetworkType
} catch (_: SecurityException) {
    TelephonyManager.NETWORK_TYPE_UNKNOWN
}

/** Converts Android radio constants into compact labels suitable for the LCD. */
private fun Int.networkLabel(): String = when (this) {
    TelephonyManager.NETWORK_TYPE_NR -> "5G"
    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
    TelephonyManager.NETWORK_TYPE_HSPAP -> "H+"
    TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA -> "H"
    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_TD_SCDMA, TelephonyManager.NETWORK_TYPE_EVDO_0,
    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD -> "3G"
    TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
    TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
    TelephonyManager.NETWORK_TYPE_1xRTT -> "1X"
    TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
    else -> "--"
}

/** Formats the current time using the user's Android 12/24-hour preference. */
private fun formattedTime(context: Context): String = DateFormat.getTimeFormat(context).format(Date())

/** Formats the current date using the user's locale preference. */
private fun formattedDate(context: Context): String = DateFormat.getDateFormat(context).format(Date())

/** Returns the positive duration until the next minute boundary. */
private fun delayUntilNextMinute(currentTimeMillis: Long): Long =
    MillisPerMinute - currentTimeMillis % MillisPerMinute

/** Publishes a formatted charge value whenever Android emits its sticky battery broadcast. */
private class BatteryReceiver(private val onBatteryChanged: (String) -> Unit) : BroadcastReceiver() {
    /** Delivers the current battery percentage. The broadcast can be absent on unusual devices. */
    override fun onReceive(context: Context?, intent: Intent?) {
    onBatteryChanged(intent?.batteryText() ?: "--%")
    }
}

/** Converts battery intent metadata into the display string used by the LCD screen. */
private fun Intent.batteryText(): String {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, UnknownBatteryLevel)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, UnknownBatteryLevel)
    if (level < 0 || scale <= 0) return "--%"
    return "${level * 100 / scale}%"
}
