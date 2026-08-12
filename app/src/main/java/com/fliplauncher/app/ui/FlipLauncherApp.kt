package com.fliplauncher.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Date

private const val MillisPerMinute = 60_000L
private const val UnknownBatteryLevel = -1
private val PhoneAspectRatio = 390f / 844f
private val KeyLabels = listOf(
    KeyLabel("1"), KeyLabel("2", "ABC"), KeyLabel("3", "DEF"),
    KeyLabel("4", "GHI"), KeyLabel("5", "JKL"), KeyLabel("6", "MNO"),
    KeyLabel("7", "PQRS"), KeyLabel("8", "TUV"), KeyLabel("9", "WXYZ"),
    KeyLabel("*"), KeyLabel("0", "+"), KeyLabel("#"),
)

/** Represents one visual key on the currently non-interactive numeric keypad. */
private data class KeyLabel(val primary: String, val secondary: String? = null)

/** Renders the static FlipLauncher home interface shown in the supplied HTML mockup. */
@Composable
fun FlipLauncherApp() {
    val status = rememberLauncherStatus()

    FlipLauncherTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(FlipColors.BackdropStart, FlipColors.BackdropEnd))),
            contentAlignment = Alignment.Center,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val phoneHeight = minOf(maxHeight, maxWidth / PhoneAspectRatio)
                FlipPhoneFrame(status = status, height = phoneHeight)
            }
        }
    }
}

/** Draws the handset enclosure and arranges its screen, quick bars, and keypad. */
@Composable
private fun FlipPhoneFrame(status: LauncherStatus, height: Dp) {
    Surface(
        modifier = Modifier
            .height(height)
            .aspectRatio(PhoneAspectRatio)
            .shadow(24.dp, RoundedCornerShape(36.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(36.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(FlipColors.HousingTop, FlipColors.HousingBottom)))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            SpeakerSlot()
            Spacer(Modifier.height(12.dp))
            DisplayPanel(status = status, modifier = Modifier.weight(0.74f))
            Spacer(Modifier.height(14.dp))
            QuickActionBars()
            Spacer(Modifier.height(14.dp))
            Keypad(modifier = Modifier.weight(1.12f))
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
private fun DisplayPanel(status: LauncherStatus, modifier: Modifier = Modifier) {
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
            ScreenSurface(status = status)
        }
    }
}

/** Draws the LCD texture, status values, and inactive screen-action glyphs. */
@Composable
private fun ScreenSurface(status: LauncherStatus) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(FlipColors.ScreenTop, FlipColors.ScreenBottom)))
            .lcdGrid()
            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = status.timeText,
                    color = FlipColors.ScreenInk,
                    fontSize = 48.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-4).sp,
                )
                Text(
                    text = status.batteryText,
                    color = FlipColors.ScreenInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
            WeatherReadout()
        }
        Spacer(Modifier.weight(1f))
        ScreenActions()
    }
}

/** Renders the placeholder weather data from the visual reference without any network dependency. */
@Composable
private fun WeatherReadout() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "☀", color = FlipColors.ScreenInk, fontSize = 30.sp)
            Spacer(Modifier.width(5.dp))
            Text(text = "84°", color = FlipColors.ScreenInk, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "H:88° L:72°",
            color = FlipColors.ScreenInk,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

/** Renders decorative search, apps, and settings glyphs at the display's bottom edge. */
@Composable
private fun ScreenActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("⌕", "▤", "⚙").forEach { glyph ->
            Text(text = glyph, color = FlipColors.ScreenInk, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Draws the three colored, intentionally inactive quick-action bars. */
@Composable
private fun QuickActionBars() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(FlipColors.Red, FlipColors.Yellow, FlipColors.Green).forEachIndexed { index, color ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics { contentDescription = "Inactive action bar ${index + 1}" },
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
private fun Keypad(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF050605), FlipColors.HousingBottom)), RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KeyLabels.chunked(3).forEach { row ->
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key -> KeypadKey(key = key, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Draws one visual key and its optional telephone-letter label. */
@Composable
private fun KeypadKey(key: KeyLabel, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Inactive keypad key ${key.primary}" },
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(FlipColors.KeyTop, FlipColors.KeyBottom)))
                .padding(top = 9.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = key.primary,
                color = FlipColors.KeyText,
                fontSize = 23.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            )
            key.secondary?.let {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = it,
                    color = FlipColors.KeySubtext,
                    fontSize = 8.sp,
                    letterSpacing = 1.4.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

/**
 * Holds the live system values required by the mockup's status screen.
 *
 * @property timeText Localized current time for the upper-left LCD readout.
 * @property batteryText Formatted battery percentage, or an unknown-state label.
 */
private data class LauncherStatus(val timeText: String, val batteryText: String)

/** Observes the local clock and battery broadcasts while the launcher composition is visible. */
@Composable
private fun rememberLauncherStatus(): LauncherStatus {
    val context = LocalContext.current
    var timeText by remember(context) { mutableStateOf(formattedTime(context)) }
    var batteryText by remember { mutableStateOf("BAT --%") }

    LaunchedEffect(context) {
        while (true) {
            timeText = formattedTime(context)
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
    return LauncherStatus(timeText, batteryText)
}

/** Formats the current time using the user's Android 12/24-hour preference. */
private fun formattedTime(context: Context): String = DateFormat.getTimeFormat(context).format(Date())

/** Returns the positive duration until the next minute boundary. */
private fun delayUntilNextMinute(currentTimeMillis: Long): Long =
    MillisPerMinute - currentTimeMillis % MillisPerMinute

/** Publishes a formatted charge value whenever Android emits its sticky battery broadcast. */
private class BatteryReceiver(private val onBatteryChanged: (String) -> Unit) : BroadcastReceiver() {
    /** Delivers the current battery percentage. The broadcast can be absent on unusual devices. */
    override fun onReceive(context: Context?, intent: Intent?) {
        onBatteryChanged(intent?.batteryText() ?: "BAT --%")
    }
}

/** Converts battery intent metadata into the display string used by the LCD screen. */
private fun Intent.batteryText(): String {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, UnknownBatteryLevel)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, UnknownBatteryLevel)
    if (level < 0 || scale <= 0) return "BAT --%"
    return "BAT ${level * 100 / scale}%"
}

/** Adds the fine scanline grid that gives the Compose display an LCD appearance. */
private fun Modifier.lcdGrid(): Modifier = drawBehind {
    val verticalSpacing = 8.dp.toPx()
    val horizontalSpacing = 8.dp.toPx()
    val lineColor = FlipColors.ScreenInk.copy(alpha = 0.14f)
    var y = 0f
    while (y < size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        y += verticalSpacing
    }
    var x = 0f
    while (x < size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        x += horizontalSpacing
    }
    drawRoundRect(FlipColors.ScreenInk.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))
}
