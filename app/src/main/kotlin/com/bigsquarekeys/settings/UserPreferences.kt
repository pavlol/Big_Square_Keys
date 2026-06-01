package com.bigsquarekeys.settings

import android.content.Context
import android.content.SharedPreferences
import com.bigsquarekeys.language.DEFAULT_LANGUAGE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME   = "bsk_prefs"
private const val KEY_ONBOARDED    = "onboarded"
private const val KEY_LANGUAGES    = "enabled_languages"

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Onboarding ──────────────────────────────────────────────────────────

    val isFirstLaunch: Boolean
        get() = !prefs.getBoolean(KEY_ONBOARDED, false)

    fun markOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    // ── Language list ────────────────────────────────────────────────────────

    /**
     * Ordered list of language codes the user has enabled (e.g. ["en", "ru"]).
     * The globe key cycles through this list.
     * Defaults to just English if nothing is stored.
     */
    var enabledLanguages: List<String>
        get() {
            val stored = prefs.getString(KEY_LANGUAGES, null)
            return if (stored.isNullOrBlank()) listOf(DEFAULT_LANGUAGE)
                   else stored.split(",").filter { it.isNotBlank() }
        }
        set(value) {
            prefs.edit().putString(KEY_LANGUAGES, value.joinToString(",")).apply()
        }
}
