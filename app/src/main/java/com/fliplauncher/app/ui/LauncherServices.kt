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
    /** Returns validated saved favorites, appending defaults for newly introduced slots. */
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
        val saved = preferences.asMap().entries
            .mapNotNull { entry -> entry.key.name.favoriteIndexOrNull()?.let { index -> index to (entry.value as? String)?.toFavoriteOrNull() } }
            .sortedBy { (index, _) -> index }
            .mapNotNull { (_, favorite) -> favorite }
        return saved.withSeedFavorites()
    }

    /** Replaces the persisted slots after validating the configured slot count. */
    override suspend fun save(favorites: List<QuickLaunchFavorite>) {
        require(favorites.isNotEmpty()) { "Quick Launch requires at least one favorite." }
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

    /** Launches a configured favorite through its selected activity or package fallback. */
    fun launchFavorite(target: FavoriteLaunchTarget): HandoffResult

    /** Opens Android's dialer populated with a valid user-entered phone number. */
    fun openDialer(number: String): HandoffResult

    /** Opens Android's messaging flow addressed to a valid user-entered phone number. */
    fun openTextMessage(number: String): HandoffResult
}

/** Uses explicit launcher intents and ACTION_DIAL so no call permission is needed. */
internal class AndroidExternalNavigator(private val context: Context) : ExternalNavigator {
    /** Starts a specific activity selected from Search. */
    override fun launchApp(app: LaunchableApp): HandoffResult = start(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .apply { component = ComponentName(app.packageName, app.className) },
    )

    /** Resolves a selected component before falling back to the app package entry point. */
    override fun launchFavorite(target: FavoriteLaunchTarget): HandoffResult {
        if (target.className != null) {
            return start(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).apply {
                component = ComponentName(target.packageName, target.className)
            })
        }
        val intent = context.packageManager.getLaunchIntentForPackage(target.packageName) ?: return HandoffResult.Unavailable
        return start(intent)
    }

    /** Hands a number to any installed dialer without requesting CALL_PHONE permission. */
    override fun openDialer(number: String): HandoffResult = start(
        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)),
    )

    /** Hands a number to the default SMS-capable application without requesting SMS permission. */
    override fun openTextMessage(number: String): HandoffResult = start(
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)),
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

/** Extracts a non-negative favorite index from a DataStore preference key name. */
private fun String.favoriteIndexOrNull(): Int? = takeIf { startsWith(FavoriteSlotPrefix) }
    ?.removePrefix(FavoriteSlotPrefix)
    ?.toIntOrNull()

/** Appends new bundled examples so existing installs can exercise newly added grid pages. */
private fun List<QuickLaunchFavorite>.withSeedFavorites(): List<QuickLaunchFavorite> {
    if (isEmpty()) return FavoriteTargets.defaults()
    return this + FavoriteTargets.defaults().drop(size)
}

/** Serializes the simple favorite record without exposing platform objects to persistence. */
private fun QuickLaunchFavorite.serialize(): String = listOf(
    target.appName,
    target.packageName,
    target.className.orEmpty(),
    label,
    icon.name,
).joinToString(FavoriteFieldSeparator)

/** Parses and validates a stored favorite record, returning null for malformed values. */
private fun String.toFavoriteOrNull(): QuickLaunchFavorite? {
    val parts = split(FavoriteFieldSeparator)
    return if (parts.size == 3) legacyFavorite(parts) else currentFavorite(parts)
}

/** Converts the previous target-id record format into a package-launch fallback favorite. */
private fun legacyFavorite(parts: List<String>): QuickLaunchFavorite? {
    val legacyTarget = parts.getOrNull(0)?.let(FavoriteTargets::find) ?: return null
    val label = parts.getOrNull(1)?.takeIf { it in legacyTarget.labels } ?: return null
    val icon = parts.getOrNull(2).toFavoriteIconOrNull() ?: return null
    return QuickLaunchFavorite(FavoriteLaunchTarget(legacyTarget.appName, legacyTarget.packageName), label, icon)
}

/** Parses a current app-activity favorite record while rejecting incomplete values. */
private fun currentFavorite(parts: List<String>): QuickLaunchFavorite? {
    val appName = parts.getOrNull(0)?.takeIf(String::isNotBlank) ?: return null
    val packageName = parts.getOrNull(1)?.takeIf(String::isNotBlank) ?: return null
    val label = parts.getOrNull(3)?.takeIf(String::isNotBlank) ?: return null
    val icon = parts.getOrNull(4).toFavoriteIconOrNull() ?: return null
    return QuickLaunchFavorite(FavoriteLaunchTarget(appName, packageName, parts.getOrNull(2).orEmpty().ifBlank { null }), label, icon)
}

/** Converts a persisted enum name into a valid favorite icon. */
private fun String?.toFavoriteIconOrNull(): FavoriteIcon? = FavoriteIcon.values().firstOrNull { it.name == this }
