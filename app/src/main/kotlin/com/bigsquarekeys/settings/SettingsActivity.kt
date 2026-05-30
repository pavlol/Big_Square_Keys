package com.bigsquarekeys.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bigsquarekeys.R
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
                    onEnableKeyboard = ::openInputMethodSettings,
                )
            }
        }
    }

    private fun openInputMethodSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }
}

@Composable
fun SettingsScreen(onEnableKeyboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Big Square Keys",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Privacy card — always visible
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

        // Key size info card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("⬛ Key Size", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Every key is physically 10 mm × 10 mm on your screen — " +
                        "calculated from your device's actual display density.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Enable keyboard button
        Button(
            onClick = onEnableKeyboard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Enable Big Square Keys in System Settings")
        }
    }
}
