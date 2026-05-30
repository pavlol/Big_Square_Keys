package com.bigsquarekeys.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("bsk_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")

    val isFirstLaunch: Boolean
        get() = runBlocking {
            context.dataStore.data.first()[FIRST_LAUNCH] ?: true
        }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { it[FIRST_LAUNCH] = false }
    }
}
