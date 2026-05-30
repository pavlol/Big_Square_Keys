# Big Square Keys — CLAUDE.md

## Project Overview

**Big Square Keys** is a privacy-first Android custom keyboard (Input Method Editor, IME) designed
for adults with large fingers. Every key is physically 10 mm × 10 mm regardless of device density,
eliminating accidental neighbour-key presses. The app replaces the system default keyboard
(Samsung, Gboard, SwiftKey, etc.).

- **Package name:** `com.bigsquarekeys`
- **Min SDK:** 23 (Android 6.0 — covers 99%+ of active devices)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin (no Java)
- **Build system:** Gradle 8.x with Kotlin DSL (`build.gradle.kts`)

---

## Core Design Constraints (never violate these)

| Constraint | Value |
|---|---|
| Physical key size | **10 mm × 10 mm** (calculated from real device DPI, not dp) |
| Privacy | **Zero data collection.** `INTERNET` permission is forbidden in AndroidManifest. |
| Key shape | Square. All keys same width and height except Space and Enter (which may span 2 columns). |
| Layout | "Wrapped QWERTY" — QWERTY order preserved, rows wrap at 5 or 6 columns depending on screen width |
| Language support | All languages via Android `InputMethodSubtype` + dynamic layout switching |

### Physical key size formula

```kotlin
// Use actual hardware DPI, not the density bucket
val keyPx = (10f / 25.4f) * displayMetrics.xdpi   // width
val keyPy = (10f / 25.4f) * displayMetrics.ydpi   // height
```

Do **not** use `dp` or `resources.getDimensionPixelSize` for key dimensions —
those are density-bucket approximations and will produce wrong physical sizes.

---

## Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Language | **Kotlin** | First-class Android support, null-safety |
| IME framework | **Android `InputMethodService`** | Only API for system keyboard replacement |
| Keyboard rendering | **Custom `View` + `Canvas`** | Pixel-perfect control of physical sizes; Compose IME support is experimental/limited |
| Settings UI | **Jetpack Compose** | Modern declarative UI for the settings Activity |
| DI | **Hilt** | Standard Android DI, minimal boilerplate |
| Preferences | **`DataStore` (Proto)** | Type-safe, coroutine-native; no SharedPreferences |
| Testing | **JUnit 5 + Robolectric** (unit), **Espresso** (UI) | Standard Android test stack |
| CI | **GitHub Actions** | Free for open source; runs lint + tests on every PR |
| Build | **Gradle 8.x Kotlin DSL** | Type-safe, IDE-friendly |

### Explicitly excluded
- No `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_CONTACTS`, or any other sensitive permission
- No Firebase, Crashlytics, Sentry, or any analytics SDK
- No third-party keyboard SDK
- No Jetpack Compose for the keyboard view itself (IME window is not a Compose host reliably until API 35+)

---

## Architecture

```
com.bigsquarekeys/
├── BigSquareKeysApp.kt            Application class (Hilt entry point)
│
├── ime/
│   └── BigSquareKeyboardService.kt  InputMethodService — lifecycle owner,
│                                     delegates rendering to KeyboardView,
│                                     delegates input to InputRouter
│
├── keyboard/
│   ├── KeyboardView.kt            Custom View: draws keys on Canvas, handles touch
│   ├── KeyboardEngine.kt          Computes key rectangles from screen width + DPI
│   ├── InputRouter.kt             Translates KeyAction → commitText / sendKeyEvent
│   ├── model/
│   │   ├── Key.kt                 Data: label, secondaryLabel, action, RectF bounds
│   │   ├── KeyAction.kt           Sealed class: Char, Delete, Shift, Space, Enter,
│   │   │                            SwitchToNumbers, SwitchToSymbols, SwitchLanguage
│   │   └── KeyboardLayout.kt      Ordered list of rows, each row = list of Keys
│   └── layout/
│       ├── LayoutProvider.kt      Factory: returns correct layout for current locale + mode
│       ├── LatinLayout.kt         Wrapped-QWERTY for Latin-script languages
│       ├── NumberLayout.kt        Numeric pad (3×4 phone style + operators)
│       ├── SymbolLayout.kt        Punctuation + special chars
│       └── RtlLatinLayout.kt      Mirror of LatinLayout for RTL contexts
│
├── language/
│   ├── LanguageManager.kt         Tracks active subtype; broadcasts changes
│   └── SubtypeHelper.kt           Builds InputMethodSubtype list from locale list
│
├── settings/
│   ├── SettingsActivity.kt        Hosts Compose UI; registered in manifest
│   ├── SettingsScreen.kt          Compose screen: vibration, key height multiplier (1.0–1.5×), theme
│   └── UserPreferences.kt         DataStore wrapper; all fields have defaults
│
└── onboarding/
    ├── OnboardingActivity.kt      Shown on first launch only
    ├── PrivacyPledgeScreen.kt     Full-screen privacy declaration (Compose)
    └── SetupGuideScreen.kt        Step-by-step instructions to enable IME in Settings
```

### Data flow

```
Touch event (MotionEvent)
  → KeyboardView.onTouchEvent()
      → KeyboardEngine.keyAt(x, y) → Key
          → BigSquareKeyboardService.onKeyPressed(key)
              → InputRouter.route(action, inputConnection)
                  → commitText() / sendKeyEvent() / …
```

No data is persisted from the touch → commit path. `UserPreferences` is read-only during keyboard
session (settings are applied on next keyboard show).

---

## Keyboard Layout Detail

### "Wrapped QWERTY" Layout

Keys follow QWERTY order but wrap into 5 columns (narrow phones, < 380 dp) or 6 columns
(wide phones, ≥ 380 dp). The column count is computed once at `onStartInputView` and cached.

**5-column layout (narrow):**
```
Q  W  E  R  T
Y  U  I  O  P
A  S  D  F  G
H  J  K  L  ⌫
⇧  Z  X  C  V
B  N  M  .  ↵
123  ,  [─────]  !  🌐
              ↑ Space spans 1 col; row has 6 logical slots, space=1
```

**6-column layout (wide):**
```
Q  W  E  R  T  Y
U  I  O  P  A  S
D  F  G  H  J  K
L  Z  X  C  V  B
N  M  ,  .  ⌫  ↵
123  ⇧  [───────]  !  🌐
```

Secondary characters (!, @, #, …) are accessed via the `123` / symbols layer, not via long-press
(long-press on a letter = uppercase lock for that letter only, planned v1.1).

### Row height vs key height
All rows have the same height = key height. The keyboard panel height is
`rows * keyHeightPx + padding`. There is no fixed percentage of screen height.

---

## Privacy Architecture

### Manifest (enforced, never add these)
```xml
<!-- These permissions are intentionally absent: -->
<!-- INTERNET, ACCESS_NETWORK_STATE, READ_CONTACTS, READ_CALL_LOG,
     RECEIVE_BOOT_COMPLETED, READ_EXTERNAL_STORAGE -->
```

### At runtime
- `InputConnection` is used **only** to commit text and send key events — never to read
  surrounding text except for auto-correct cursor context (feature flag, default OFF in v1.0).
- Nothing is written to disk except `UserPreferences` (DataStore, device-local).
- No clipboard reading.

### User-facing disclosures
1. **Play Store listing** — first line of description: *"All keys are 10 mm × 10 mm squares.
   Zero data collection — no internet permission, ever."*
2. **Onboarding** — `PrivacyPledgeScreen` must be shown before first use; user acknowledges.
3. **Settings screen** — permanent "Privacy" card: *"Big Square Keys cannot access the internet.
   Your keystrokes never leave your device."*

---

## Build Commands

```bash
# Assemble debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run lint
./gradlew :app:lintDebug

# Run all checks (lint + tests)
./gradlew check

# Install on connected device
./gradlew :app:installDebug

# Generate signed release APK (requires keystore env vars — see CI section)
./gradlew :app:assembleRelease
```

### Keystore environment variables (CI / local release)
```
BSK_KEYSTORE_PATH=/path/to/bigsquarekeys.jks
BSK_KEYSTORE_PASSWORD=...
BSK_KEY_ALIAS=bigsquarekeys
BSK_KEY_PASSWORD=...
```

---

## Android Manifest Key Points

```xml
<!-- The IME service declaration — the most important piece -->
<service
    android:name=".ime.BigSquareKeyboardService"
    android:label="@string/ime_name"
    android:permission="android.permission.BIND_INPUT_METHOD"
    android:exported="true">
    <intent-filter>
        <action android:name="android.view.InputMethod" />
    </intent-filter>
    <meta-data
        android:name="android.view.im"
        android:resource="@xml/method" />
</service>
```

`res/xml/method.xml` declares `supportsSwitchingToNextInputMethod="true"` and lists all
`InputMethodSubtype` entries (one per supported locale).

---

## Language Support Strategy

### Latin-script languages (English, French, German, Spanish, Polish, …)
- Share `LatinLayout`; only key labels change per locale (accented characters on long-press in v1.1).
- One `InputMethodSubtype` per locale; user adds subtypes in Settings.

### RTL languages (Arabic, Hebrew, Persian, Urdu)
- `RtlLatinLayout` mirrors the grid horizontally.
- For Arabic/Hebrew character sets: dedicated `ArabicLayout` / `HebrewLayout` (v1.1).

### CJK and Indic scripts (Chinese, Japanese, Korean, Hindi, …)
- v1.0: Route through romanisation / transliteration input (Pinyin for Chinese, Romaji for Japanese).
  The OS candidate bar above the keyboard handles character selection.
- v1.1: Dedicated layout files per script where feasible.

### Implementation
- `LanguageManager` subscribes to `InputMethodManager.getCurrentInputMethodSubtype()`.
- `LayoutProvider.getLayout(locale, mode)` returns the correct `KeyboardLayout`.
- Switching language: globe key calls `switchToNextInputMethod()`.

---

## Testing Strategy

| Test type | Location | What to cover |
|---|---|---|
| Unit | `src/test/` | `KeyboardEngine` (key rect calculation), `LayoutProvider` (correct layout returned), `InputRouter` (action → commit mapping) |
| Robolectric | `src/test/` | `BigSquareKeyboardService` lifecycle, `UserPreferences` DataStore reads/writes |
| Instrumented | `src/androidTest/` | KeyboardView renders correct number of keys, touch → commit end-to-end |

---

## Play Store Metadata (draft)

**Title:** Big Square Keys — Large Key Keyboard  
**Short description:** 10 mm × 10 mm square keys. Built for large fingers. Zero data collection.

**Description (first 167 chars are "above the fold"):**
```
⬛ 10 mm × 10 mm square keys — no more fat-finger mistakes.
🔒 Zero data collection. No internet permission. Ever.
Built for adults with large fingers who are tired of hitting neighbouring keys.
```

**Content rating:** Everyone  
**Category:** Tools  
**Privacy policy URL:** required — host a simple static page declaring no data collection.

---

## File Naming Conventions

- Kotlin files: `PascalCase.kt`
- Layout XMLs: `snake_case.xml`
- Resource values: `snake_case`
- String keys: `snake_case`
- Constants: `SCREAMING_SNAKE_CASE` in companion objects

---

## Common Gotchas

1. **`InputMethodService.onCreateInputView()`** — return the `KeyboardView` here, not in `onCreate`.
2. **`getCurrentInputConnection()`** can return `null` — always null-check before calling `commitText`.
3. **Key size must be computed from `displayMetrics.xdpi` / `ydpi`** — do not use `density` or `densityDpi`.
4. **The IME window does not host a normal Activity** — do not use `startActivity` from inside the service without `FLAG_ACTIVITY_NEW_TASK`.
5. **`DataStore` is coroutine-based** — collect preferences in a `lifecycleScope` tied to the service, not a plain thread.
6. **Shift state** — maintain a local `ShiftState` enum (`OFF`, `ONE_SHOT`, `LOCKED`) and redraw the keyboard when it changes.
7. **Landscape mode** — the keyboard panel height must be reduced in landscape (fewer rows visible, or a compact mode). Implement `onComputeInsets()`.
8. **`FLAG_SECURE` windows** — some apps (banking) set this flag; the keyboard still works but screenshot-based tests will fail on those windows.

---

## Versioning

- v1.0: Latin wrapped-QWERTY, numbers, symbols, privacy onboarding, EN/ES/FR/DE/PL subtypes
- v1.1: Long-press accented characters, Arabic layout, per-layout key labels
- v1.2: Theme support (dark/light/high-contrast), key height multiplier setting (1.0×–1.5×)
- v2.0: CJK / Indic dedicated layouts
