package com.bigsquarekeys.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigsquarekeys.language.SUPPORTED_LANGUAGES
import com.bigsquarekeys.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.isFirstLaunch) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                SettingsScreen(
                    initialEnabledLanguages = prefs.enabledLanguages.toSet(),
                    onLanguagesChanged = { prefs.enabledLanguages = it.toList() },
                    onEnableKeyboard = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    initialEnabledLanguages: Set<String>,
    onLanguagesChanged: (Set<String>) -> Unit,
    onEnableKeyboard: () -> Unit,
) {
    var enabledLanguages by remember { mutableStateOf(initialEnabledLanguages) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Big Square Keys", style = MaterialTheme.typography.headlineMedium)

        // Privacy card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("🔒 Privacy Guarantee", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Big Square Keys cannot access the internet. " +
                        "Your keystrokes never leave your device. " +
                        "No analytics. No crash reporting. No data collection of any kind.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Key size card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("⬛ Key Size", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Every key is physically 10 mm × 10 mm — " +
                        "calculated from your device's actual display density.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Language picker card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("🌐 Languages", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tick the languages you want. " +
                        "Use the 🌐 key on the keyboard to cycle between them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                SUPPORTED_LANGUAGES.forEach { lang ->
                    val isChecked = lang.code in enabledLanguages
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val updated = if (checked) {
                                    enabledLanguages + lang.code
                                } else {
                                    // Always keep at least one language
                                    if (enabledLanguages.size > 1)
                                        enabledLanguages - lang.code
                                    else enabledLanguages
                                }
                                enabledLanguages = updated
                                onLanguagesChanged(updated)
                            },
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(lang.nativeName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                lang.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Enable keyboard button
        Button(
            onClick = onEnableKeyboard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Enable Big Square Keys in System Settings")
        }

        Spacer(Modifier.height(8.dp))
    }
}
