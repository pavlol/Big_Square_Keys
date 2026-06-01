# ⬛ Big Square Keys

A privacy-first Android custom keyboard with **physically 10 mm × 10 mm square keys** —
calculated from each device's real display density, not a dp approximation.
Built for adults with large fingers who are tired of hitting the wrong key.

---

## Features

| Feature | Detail |
|---|---|
| **Key size** | Every key is exactly 10 mm × 10 mm on the physical screen |
| **Full-width layout** | Keys expand to fill the screen width; 10 mm is the minimum, never the cap |
| **Shift visual feedback** | Letters show lowercase / UPPERCASE live; shift key turns blue when active |
| **Long-press backspace** | Hold ⌫ to delete continuously (~18 chars/s); tap to delete one character |
| **Numbers layer** | Tap `123` to open a phone-style numeric pad with arithmetic operators |
| **Symbols layer** | Tap `#+=` in the numbers layer to open 32 special characters (€, £, brackets, …) |
| **Language picker** | Tap `🌐` to open a popup listing all your enabled languages; tap one to switch instantly |
| **Globe key label** | Always shows the active language code (EN, DE, RU, UK, …) so you always know what's selected |
| **Privacy onboarding** | Privacy pledge screen is shown on first launch before the keyboard is used |
| **Zero permissions** | Only `VIBRATE` is declared — no internet, no contacts, no storage |

---

## Supported Languages

| Language | Script | Layout |
|---|---|---|
| English | Latin | Wrapped QWERTY (5 or 6 cols) |
| Spanish | Latin | Wrapped QWERTY |
| French | Latin | Wrapped QWERTY |
| German | Latin | Wrapped **QWERTZ** + ä ö ü ß |
| Italian | Latin | Wrapped QWERTY |
| Portuguese | Latin | Wrapped QWERTY |
| Polish | Latin | Wrapped QWERTY |
| Dutch | Latin | Wrapped QWERTY |
| Swedish | Latin | Wrapped QWERTY |
| Turkish | Latin | Wrapped QWERTY |
| Russian | Cyrillic | Wrapped ЙЦУКЕН |
| Ukrainian | Cyrillic | Wrapped ЙЦУКЕН (ї, і, є, ґ — distinct from Russian) |
| Bulgarian | Cyrillic | Wrapped ЙЦУКЕН |
| Arabic | Arabic | Latin placeholder (dedicated layout: v1.1) |
| Hebrew | Hebrew | Latin placeholder (dedicated layout: v1.1) |

The number of columns (5 or 6) is chosen automatically based on the physical screen width;
every column keeps keys at or above the 10 mm minimum.

---

## Privacy

> **Big Square Keys cannot access the internet. Your keystrokes never leave your device.**

- `INTERNET` permission is **not declared** in the manifest — the OS blocks all network access at the system level.
- No analytics SDK, no crash reporting, no Firebase, no cloud sync of any kind.
- Nothing is written to disk except your language preferences (SharedPreferences, device-local).
- The `InputConnection` is used only to commit text and send key events — surrounding text is never read.
- Open source — you can verify every claim above by reading the source code.

---

## Building

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Debug APK (for sideloading)
```bash
./gradlew :app:assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

### Install directly on a connected device
```bash
./gradlew :app:installDebug
```

### Run tests
```bash
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew check                    # lint + all tests
```

### Release APK (requires signing)
```bash
export BSK_KEYSTORE_PATH=/path/to/bigsquarekeys.jks
export BSK_KEYSTORE_PASSWORD=...
export BSK_KEY_ALIAS=bigsquarekeys
export BSK_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```

---

## Installing on Your Device (without Play Store)

1. Build the debug APK or download a release APK.
2. On your Android phone, go to **Settings → Security** and enable **Install unknown apps** for your file manager or browser.
3. Copy the `.apk` file to the device and open it.
4. After installation, open **Big Square Keys** and tap **Enable Big Square Keys in System Settings**.
5. In System Settings, enable the keyboard and set it as the default input method.

---

## Architecture Overview

```
com.bigsquarekeys/
├── ime/BigSquareKeyboardService.kt   InputMethodService, FrameLayout container,
│                                      language picker overlay
├── keyboard/
│   ├── KeyboardView.kt               Canvas rendering, long-press repeat delete
│   ├── KeyboardEngine.kt             Physical key-size calculation (real DPI)
│   ├── InputRouter.kt                KeyAction → commitText / sendKeyEvent
│   ├── model/                        Key, KeyAction (sealed), KeyboardLayout
│   └── layout/
│       ├── LayoutProvider.kt         Routes locale + mode → correct layout
│       ├── LatinLayout.kt            Wrapped QWERTY (EN, ES, FR, IT, …)
│       ├── GermanLayout.kt           Wrapped QWERTZ + ä ö ü ß
│       ├── CyrillicLayout.kt         Wrapped ЙЦУКЕН (RU, BG, …)
│       ├── UkrainianLayout.kt        Wrapped ЙЦУКЕН-UA (ї і є — distinct from RU)
│       ├── NumberLayout.kt           Phone-style numeric pad
│       └── SymbolLayout.kt           32 special characters (@, €, £, brackets, …)
├── language/SupportedLanguages.kt    15-language code + name registry
├── settings/                         Compose settings UI, SharedPreferences wrapper
└── onboarding/                       First-launch privacy pledge
```

---

## Roadmap

| Version | Planned features |
|---|---|
| **v1.0** *(current)* | Wrapped QWERTY/QWERTZ/ЙЦУКЕН, numbers, symbols, shift visual feedback, long-press delete, language picker overlay, 15 languages |
| **v1.1** | Long-press accented characters (à é ñ …), dedicated Arabic / Hebrew layouts |
| **v1.2** | Dark / light / high-contrast themes, key height multiplier (1.0×–1.5×) |
| **v2.0** | CJK / Indic dedicated layouts |

---

## License

MIT — see `LICENSE` file.
