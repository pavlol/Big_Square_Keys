package com.bigsquarekeys.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigsquarekeys.settings.SettingsActivity
import com.bigsquarekeys.settings.UserPreferences
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OnboardingScreen(
                    onAccept = {
                        prefs.markOnboardingComplete()
                        startActivity(Intent(this@OnboardingActivity, SettingsActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⬛", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))

        Text(
            "Big Square Keys",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))

        Text(
            "10 mm × 10 mm keys. Built for large fingers.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(32.dp))

        // Privacy pledge — must be shown before first use
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacy Pledge", style = MaterialTheme.typography.titleMedium)
                Text(
                    "• This app has NO internet permission\n" +
                        "• Your keystrokes are NEVER sent anywhere\n" +
                        "• No analytics, no tracking, no cloud sync\n" +
                        "• Everything stays on your device only\n" +
                        "• Open source — you can verify this yourself",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("I Understand — Let's Go")
        }
    }
}
