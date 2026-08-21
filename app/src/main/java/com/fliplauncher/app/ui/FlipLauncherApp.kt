package com.fliplauncher.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Date

private const val MillisPerMinute = 60_000L
private const val UnknownBatteryLevel = -1
private const val QuickLaunchColumnCount = 3
private const val QuickLaunchPageSize = 6
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

/** Represents one visual key on the currently non-interactive numeric keypad. */
private data class KeyLabel(val primary: String, val secondary: String? = null)

/** Renders the static FlipLauncher home interface shown in the supplied HTML mockup. */
@Composable
fun FlipLauncherApp() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val controller = remember(context, scope) {
        LauncherController(scope, DataStoreFavoriteRepository(context), AndroidLaunchableAppCatalog(context), AndroidExternalNavigator(context))
    }
    val status = rememberLauncherStatus()
    LaunchedEffect(controller) { controller.load() }
    BackHandler { controller.goHome() }
    FlipLauncherTheme {
        FlipPhoneFrame(status = status, controller = controller, modifier = Modifier.fillMaxSize())
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

/** Draws the LCD texture, status values, and inactive screen-action glyphs. */
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
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 14.dp),
        )
        LauncherLcdContent(status, controller, Modifier.align(Alignment.Center).padding(horizontal = 14.dp, vertical = 38.dp))
        ScreenActions(softKeyLabels(controller.state), modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** Renders a decorative cellular signal readout without querying device telephony services. */
@Composable
private fun CellSignalReadout(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CellSignalBars()
        Text(
            text = "LTE",
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
        BatteryIcon()
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

/** Draws the static battery silhouette used beside the live charge percentage. */
@Composable
private fun BatteryIcon() {
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
                    .background(FlipColors.ScreenInk),
            )
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(5.dp)
                .background(FlipColors.ScreenInk),
        )
    }
}

/** Draws four stepped blocks to give the cellular signal meter a low-resolution LCD appearance. */
@Composable
private fun CellSignalBars() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        CellSignalBarHeights.forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .background(FlipColors.ScreenInk),
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
        LauncherScreen.QuickLaunch -> QuickLaunchLcdContent(state, controller.favorites, modifier)
        LauncherScreen.Settings -> FavoritesLcdContent("SETTINGS", state, controller.favorites, modifier)
        LauncherScreen.FavoriteEditor -> FavoriteEditorLcdContent(state, modifier)
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

/** Renders T9 input and the first three matching app labels in Search. */
@Composable
private fun SearchLcdContent(state: LauncherUiState, apps: List<LaunchableApp>, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LcdTitle("SEARCH")
        Text(if (state.searchDigits.isEmpty()) "TYPE 2–9" else state.searchDigits, color = FlipColors.ScreenInk, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(if (state.searchMode == SearchMode.Entry) "${apps.size} MATCHES" else "BROWSE RESULTS", color = FlipColors.ScreenInk.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        apps.take(3).forEachIndexed { index, app -> LcdRow(app.label, state.searchMode == SearchMode.Results && index == state.selectedSearchIndex) }
        if (apps.isEmpty()) LcdMessage("NO APPS FOUND")
        state.message?.let { LcdMessage(it) }
    }
}

/**
 * Renders the Quick Launch grid as large, centered app icons with compact labels.
 *
 * @param state Current launcher state; its selected favorite index receives the focus treatment.
 * @param favorites Configured Quick Launch entries, displayed in a two-column grid.
 * @param modifier Layout constraints supplied by the LCD container.
 */
@Composable
private fun QuickLaunchLcdContent(
    state: LauncherUiState,
    favorites: List<QuickLaunchFavorite>,
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
                                focused = firstItemIndex + rowIndex * QuickLaunchColumnCount + columnIndex == state.selectedFavoriteIndex,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            state.message?.let { LcdMessage(it) }
        }
        QuickLaunchPageIndicator(
            pageIndex = pageIndex,
            pageCount = pageCount,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/** Returns the number of six-slot LCD pages needed for this favorite collection. */
private fun List<QuickLaunchFavorite>.pageCount(): Int = (size + QuickLaunchPageSize - 1) / QuickLaunchPageSize

/**
 * Draws a compact page indicator only when the favorite grid overflows one LCD page.
 *
 * @param pageIndex Zero-based page containing the selected favorite.
 * @param pageCount Number of available pages; one or fewer hides the indicator.
 * @param modifier Positions the dot rail alongside the app grid.
 */
@Composable
private fun QuickLaunchPageIndicator(pageIndex: Int, pageCount: Int, modifier: Modifier = Modifier) {
    if (pageCount <= 1) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(FlipColors.ScreenInk.copy(alpha = if (index == pageIndex) 1f else 0.35f)),
            )
        }
    }
}

/** Renders the compact six-slot list used by the Settings page. */
@Composable
private fun FavoritesLcdContent(title: String, state: LauncherUiState, favorites: List<QuickLaunchFavorite>, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LcdTitle(title)
        favorites.chunked(2).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth()) { row.forEachIndexed { columnIndex, favorite ->
                FavoriteLcdRow(favorite, rowIndex * 2 + columnIndex == state.selectedFavoriteIndex, Modifier.weight(1f))
            } }
        }
        state.message?.let { LcdMessage(it) }
    }
}

/** Renders the currently editable favorite property values. */
@Composable
private fun FavoriteEditorLcdContent(state: LauncherUiState, modifier: Modifier) {
    val draft = state.editorDraft ?: return
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LcdTitle("EDIT FAVORITE")
        LcdRow("APP ${FavoriteTargets.find(draft.targetId).appName}", state.editorField == FavoriteField.App)
        LcdRow("NAME ${draft.label}", state.editorField == FavoriteField.Label)
        LcdRow("ICON ${draft.icon.name.uppercase()}", state.editorField == FavoriteField.Icon)
    }
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
private fun FavoriteLcdTile(favorite: QuickLaunchFavorite, focused: Boolean, modifier: Modifier = Modifier) {
    val focusModifier = if (focused) {
        Modifier.border(1.dp, FlipColors.ScreenInk, RoundedCornerShape(4.dp))
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .then(focusModifier)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FavoriteIconGraphic(favorite.icon, Modifier.size(29.dp))
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
 * @param modifier Controls the glyph's displayed size and placement.
 */
@Composable
private fun FavoriteIconGraphic(icon: FavoriteIcon, modifier: Modifier = Modifier) {
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
    }
    Icon(image, contentDescription = null, modifier = modifier.height(15.dp), tint = FlipColors.ScreenInk)
}

/** Renders decorative search, apps, and settings glyphs at the display's bottom edge. */
@Composable
private fun ScreenActions(labels: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .topBorder(FlipColors.ScreenInk.copy(alpha = 0.38f)),
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .insideDivider(index, FlipColors.ScreenInk.copy(alpha = 0.38f))
                    .semantics { contentDescription = "$label soft-key label" },
                contentAlignment = Alignment.Center,
            ) {
                SoftKeyIcon(label)
            }
        }
    }
}

/** Maps contextual actions onto the launcher-wide bold monochrome icon vocabulary. */
@Composable
private fun SoftKeyIcon(label: String) {
    if (label.isBlank()) return
    val image = when (label) {
        "SEARCH" -> Icons.Filled.Search
        "QUICK" -> Icons.Filled.Apps
        "SETTINGS" -> Icons.Filled.Settings
        "DELETE" -> Icons.Filled.Delete
        "TEXT" -> Icons.Filled.Textsms
        "CALL", "OPEN" -> Icons.Filled.Call
        "CLEAR", "CANCEL" -> Icons.Filled.Close
        "EDIT" -> Icons.Filled.Edit
        "SAVE" -> Icons.Filled.Save
        "HOME" -> Icons.Filled.Home
        "RESULTS" -> Icons.Filled.List
        "TYPE" -> Icons.Filled.Keyboard
        else -> Icons.Filled.Launch
    }
    Icon(image, contentDescription = label, modifier = Modifier.height(24.dp), tint = FlipColors.ScreenInk)
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
private fun softKeyLabels(state: LauncherUiState): List<String> = when (state.screen) {
    LauncherScreen.Home -> listOf("SEARCH", "QUICK", "SETTINGS")
    LauncherScreen.Search -> listOf("CLEAR", if (state.searchMode == SearchMode.Entry) "RESULTS" else "TYPE", "HOME")
    LauncherScreen.QuickLaunch -> listOf("EDIT", "", "HOME")
    LauncherScreen.Settings -> listOf("HOME", "EDIT", "QUICK")
    LauncherScreen.FavoriteEditor -> listOf("CANCEL", "SAVE", "")
    LauncherScreen.Dialer -> listOf("DELETE", "TEXT", "CALL")
}

/** Draws the three colored, intentionally inactive quick-action bars. */
@Composable
private fun QuickActionBars(controller: LauncherController) {
    val labels = softKeyLabels(controller.state)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(FlipColors.Red, FlipColors.Yellow, FlipColors.Green).forEachIndexed { index, color ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics { contentDescription = "${labels[index]} action" }
                    .clickable(enabled = labels[index].isNotBlank()) { controller.pressSoftKey(index) },
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
    val keys = if (controller.state.keypadMode() == KeypadMode.Telephone) KeyLabels else NavigationKeyLabels
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

/** Draws one visual key and its optional telephone-letter label. */
@Composable
private fun KeypadKey(key: KeyLabel, controller: LauncherController, modifier: Modifier = Modifier) {
    val enabled = key.primary.isNotBlank()
    val primaryWeight = if (key.primary in setOf("↑", "↓", "←", "→")) FontWeight.ExtraBold else FontWeight.Bold
    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = if (enabled) "Key ${key.primary}" else "Inactive keypad key" }
            .clickable(enabled = enabled) { controller.pressKey(key.primary) },
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
 */
private data class LauncherStatus(
    val timeText: String,
    val dateText: String,
    val batteryText: String,
)

/** Observes the local clock and battery broadcasts while the launcher composition is visible. */
@Composable
private fun rememberLauncherStatus(): LauncherStatus {
    val context = LocalContext.current
    var timeText by remember(context) { mutableStateOf(formattedTime(context)) }
    var dateText by remember(context) { mutableStateOf(formattedDate(context)) }
    var batteryText by remember { mutableStateOf("--%") }

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
    return LauncherStatus(timeText, dateText, batteryText)
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
