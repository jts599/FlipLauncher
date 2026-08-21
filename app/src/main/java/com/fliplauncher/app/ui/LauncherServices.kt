/**
 * Adapts Android package, activity, and persistence APIs for the launcher UI.
 *
 * The interfaces keep platform effects outside the reducer so state transitions remain testable.
 */
package com.fliplauncher.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

private const val FavoriteStoreName = "quick_launch_favorites"
private const val FavoriteFieldSeparator = "\u001F"
private const val FavoriteSlotPrefix = "favorite_slot_"

private val Context.favoriteDataStore by preferencesDataStore(name = FavoriteStoreName)

/** Loads and saves the configurable Quick Launch slots. */
internal interface FavoriteRepository {
    /** Returns six validated saved favorites, or the supplied defaults when no data exists. */
    suspend fun load(): List<QuickLaunchFavorite>

    /** Persists every favorite slot atomically. */
    suspend fun save(favorites: List<QuickLaunchFavorite>)
}

/** Persists Quick Launch values in app-private Android DataStore preferences. */
internal class DataStoreFavoriteRepository(private val context: Context) : FavoriteRepository {
    /** Reads all slots, falling back safely after corrupt or unavailable storage. */
    override suspend fun load(): List<QuickLaunchFavorite> {
        val preferences = context.favoriteDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        val saved = FavoriteTargets.all.indices.mapNotNull { index ->
            preferences[favoriteSlotKey(index)]?.toFavoriteOrNull()
        }
        return if (saved.size == FavoriteTargets.all.size) saved else FavoriteTargets.defaults()
    }

    /** Replaces the persisted slots after validating the required fixed-size configuration. */
    override suspend fun save(favorites: List<QuickLaunchFavorite>) {
        require(favorites.size == FavoriteTargets.all.size) { "Quick Launch requires six favorites." }
        context.favoriteDataStore.edit { preferences ->
            favorites.forEachIndexed { index, favorite -> preferences[favoriteSlotKey(index)] = favorite.serialize() }
        }
    }
}

/** Supplies installed activities visible to FlipLauncher through the launcher intent contract. */
internal interface LaunchableAppCatalog {
    /** Returns alphabetically sorted launchable activities, excluding FlipLauncher itself. */
    fun load(): List<LaunchableApp>
}

/** Queries Android PackageManager for launcher activities exposed to this application. */
internal class AndroidLaunchableAppCatalog(private val context: Context) : LaunchableAppCatalog {
    /** Reads and transforms launcher activities without starting any external component. */
    @Suppress("DEPRECATION")
    override fun load(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return context.packageManager.queryIntentActivities(intent, 0)
            .asSequence()
            .map { resolveInfo ->
                LaunchableApp(
                    label = resolveInfo.loadLabel(context.packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    className = resolveInfo.activityInfo.name,
                )
            }
            .filterNot { app -> app.packageName == context.packageName }
            .distinctBy { app -> app.packageName to app.className }
            .sortedBy { app -> app.label.lowercase() }
            .toList()
    }
}

/** Reports whether a requested external handoff was started or could not be resolved. */
internal sealed interface HandoffResult {
    data object Started : HandoffResult
    data object Unavailable : HandoffResult
}

/** Starts selected apps and the system dialer while translating platform failures into UI outcomes. */
internal interface ExternalNavigator {
    /** Launches a concrete searchable app activity. */
    fun launchApp(app: LaunchableApp): HandoffResult

    /** Launches a configured mock favorite by its package name. */
    fun launchFavorite(target: FavoriteTarget): HandoffResult

    /** Opens Android's dialer populated with a valid user-entered phone number. */
    fun openDialer(number: String): HandoffResult
}

/** Uses explicit launcher intents and ACTION_DIAL so no call permission is needed. */
internal class AndroidExternalNavigator(private val context: Context) : ExternalNavigator {
    /** Starts a specific activity selected from Search. */
    override fun launchApp(app: LaunchableApp): HandoffResult = start(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .apply { component = ComponentName(app.packageName, app.className) },
    )

    /** Resolves a mock favorite's current package entry point before launching it. */
    override fun launchFavorite(target: FavoriteTarget): HandoffResult {
        val intent = context.packageManager.getLaunchIntentForPackage(target.packageName) ?: return HandoffResult.Unavailable
        return start(intent)
    }

    /** Hands a number to any installed dialer without requesting CALL_PHONE permission. */
    override fun openDialer(number: String): HandoffResult = start(
        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)),
    )

    /** Starts an external intent and converts missing targets into local UI feedback. */
    private fun start(intent: Intent): HandoffResult = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        HandoffResult.Started
    } catch (_: Exception) {
        HandoffResult.Unavailable
    }
}

/** Converts a visible app label into the digits used by a conventional T9 keypad. */
internal fun String.toT9Digits(): String = buildString {
    this@toT9Digits.lowercase().forEach { character ->
        val digit = when (character) {
            in 'a'..'c' -> '2'
            in 'd'..'f' -> '3'
            in 'g'..'i' -> '4'
            in 'j'..'l' -> '5'
            in 'm'..'o' -> '6'
            in 'p'..'s' -> '7'
            in 't'..'v' -> '8'
            in 'w'..'z' -> '9'
            else -> null
        }
        if (digit != null) append(digit)
    }
}

/** Filters a catalog by a partial T9 pattern while retaining alphabetical catalog order. */
internal fun filterAppsByT9(apps: List<LaunchableApp>, digits: String): List<LaunchableApp> =
    if (digits.isEmpty()) apps else apps.filter { app -> app.label.toT9Digits().contains(digits) }

/** Creates a stable DataStore key for one zero-based Quick Launch slot. */
private fun favoriteSlotKey(index: Int) = stringPreferencesKey(FavoriteSlotPrefix + index)

/** Serializes the simple favorite record without exposing platform objects to persistence. */
private fun QuickLaunchFavorite.serialize(): String = listOf(targetId, label, icon.name).joinToString(FavoriteFieldSeparator)

/** Parses and validates a stored favorite record, returning null for malformed values. */
private fun String.toFavoriteOrNull(): QuickLaunchFavorite? {
    val parts = split(FavoriteFieldSeparator)
    val target = parts.getOrNull(0)?.let(FavoriteTargets::find) ?: return null
    val label = parts.getOrNull(1)?.takeIf { it in target.labels } ?: return null
    val icon = parts.getOrNull(2)?.let { name -> FavoriteIcon.entries.firstOrNull { it.name == name } } ?: return null
    return QuickLaunchFavorite(target.id, label, icon)
}
