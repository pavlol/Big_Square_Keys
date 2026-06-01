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
| Key shape | Square. All keys same width and height except Space (may span 2 columns). |
| Layout | Wrapped keyboard order — rows wrap at 5 or 6 columns depending on screen width |
| Language support | In-app language list stored in SharedPreferences; globe key cycles/picks |

### Physical key size formula

```kotlin
// Use actual hardware DPI, not the density bucket
val keyPx = (10f / 25.4f) * displayMetrics.xdpi   // width
val keyPy = (10f / 25.4f) * displayMetrics.ydpi   // height
```

Do **not** use `dp` or `resources.getDimensionPixelSize` for key dimensions —
those are density-bucket approximations and will produce wrong physical sizes.

Keys expand to fill the full screen width: `baseKeyWidth = (screenWidth - (cols-1)*gap) / cols`.
10 mm is the **minimum**, never a fixed cap.

---

## Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Language | **Kotlin** | First-class Android support, null-safety |
| IME framework | **Android `InputMethodService`** | Only API for system keyboard replacement |
| Keyboard rendering | **Custom `View` + `Canvas`** | Pixel-perfect control of physical sizes; Compose IME support is experimental/limited |
| Settings UI | **Jetpack Compose** | Modern declarative UI for the settings Activity |
| DI | **Hilt** | Standard Android DI, minimal boilerplate |
| Preferences | **`SharedPreferences`** | Synchronous reads required during IME session; DataStore is async-only |
| Testing | **JUnit 5 + Robolectric** (unit), **Espresso** (UI) | Standard Android test stack |
| CI | **GitHub Actions** | Free for open source; runs lint + tests on every PR |
| Build | **Gradle 8.x Kotlin DSL** | Type-safe, IDE-friendly |

### Explicitly excluded
- No `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_CONTACTS`, or any other sensitive permission
- No Firebase, Crashlytics, Sentry, or any analytics SDK
- No third-party keyboard SDK
- No Jetpack Compose for the keyboard view itself (IME window is not a Compose host reliably until API 35+)
- No DataStore — IME service reads preferences synchronously on every `onStartInputView`; DataStore's coroutine API creates lifecycle complexity with no benefit here

---

## Architecture

```
com.bigsquarekeys/
├── BigSquareKeysApp.kt               Application class (Hilt entry point)
│
├── ime/
│   └── BigSquareKeyboardService.kt   InputMethodService — lifecycle owner.
│                                      Owns a FrameLayout container that holds
│                                      KeyboardView + language-picker overlay.
│                                      Delegates rendering to KeyboardView,
│                                      input to InputRouter.
│
├── keyboard/
│   ├── KeyboardView.kt               Custom View: draws keys on Canvas, handles
│   │                                  touch. Long-press ⌫ triggers repeat-delete
│   │                                  via Handler on the main looper.
│   ├── KeyboardEngine.kt             Computes key rectangles from screen width + DPI
│   ├── InputRouter.kt                Translates KeyAction → commitText / sendKeyEvent
│   ├── model/
│   │   ├── Key.kt                    Data: label, secondaryLabel, action, RectF bounds
│   │   ├── KeyAction.kt              Sealed class: CommitChar, CommitText, Delete,
│   │   │                              DeleteWord, Enter, Space, ShiftToggle,
│   │   │                              SwitchToNumbers, SwitchToSymbols, SwitchToAlpha,
│   │   │                              SwitchLanguage, OpenSettings
│   │   └── KeyboardLayout.kt         Ordered list of rows, each row = list of Keys
│   └── layout/
│       ├── LayoutProvider.kt         Factory: routes locale + mode → correct layout
│       ├── LatinLayout.kt            Wrapped QWERTY — EN, ES, FR, IT, PT, PL, NL, SV, TR
│       ├── GermanLayout.kt           Wrapped QWERTZ — DE (includes ä ö ü ß)
│       ├── CyrillicLayout.kt         Wrapped ЙЦУКЕН — RU, BG, SR, MK, BE
│       ├── UkrainianLayout.kt        Wrapped ЙЦУКЕН-UA — UK (ї і є ґ, no ъ ы ё э)
│       ├── NumberLayout.kt           Phone-style 4-col numeric pad + operators
│       └── SymbolLayout.kt           4-col symbol layer: @#$€ brackets quotes …
│
├── language/
│   └── SupportedLanguages.kt         15-language registry (code, displayName, nativeName)
│
├── settings/
│   ├── SettingsActivity.kt           Hosts Compose UI; registered in manifest as launcher
│   └── UserPreferences.kt            SharedPreferences wrapper (onboarding flag, language list)
│
└── onboarding/
    └── OnboardingActivity.kt         Full-screen privacy pledge; shown on first launch only
```

### Data flow

```
Touch event (MotionEvent)
  → KeyboardView.onTouchEvent()
      → findKey(x, y) → Key          (uses bounds assigned by KeyboardEngine)
          → BigSquareKeyboardService.handleKey(key)
              → InputRouter.route(action, inputConnection, isShifted)
                  → commitText() / sendKeyEvent() / …
```

No data is persisted from the touch → commit path.
`UserPreferences` is read-only during a keyboard session (settings applied on next show).

---

## Keyboard Layers

### Layer 1 — Alpha (ALPHA mode)

One layout per language family. Key labels are **lowercase by default**.
`KeyboardView` uppercases single-character labels at draw time when `isShifted = true`.
The ⇧ key gets a **blue background** (`#0A84FF`) when shift is active.

**Latin 5-column (narrow phones):**
```
q  w  e  r  t
y  u  i  o  p
a  s  d  f  g
h  j  k  l  ⌫
⇧  z  x  c  v
b  n  m  .  ↵
123  ,  [space]  !  EN
```

**Latin 6-column (wide phones):**
```
q  w  e  r  t  y
u  i  o  p  a  s
d  f  g  h  j  k
l  z  x  c  v  b
n  m  ,  .  ⌫  ↵
123  ⇧  [──space──]  !  EN
```

German uses the same column counts but QWERTZ order with ä ö ü ß on dedicated keys.
Cyrillic (RU/BG) and Ukrainian each have their own wrapped layouts at 5 or 6 columns.

### Layer 2 — Numbers (NUMBERS mode, `123` key)

Phone-style 4-column pad:
```
1  2  3  +
4  5  6  -
7  8  9  *
#+=  0  ⌫  ↵
ABC(×2)  .  ,
```

### Layer 3 — Symbols (SYMBOLS mode, `#+=` key)

4-column grid of 32 special characters:
```
@  #  $  €
£  ¥  %  ^
&  *  (  )
[  ]  {  }
<  >  /  \
+  =  _  |
~  `  '  "
;  :  !  ?
123(×2)  ⌫  ↵
```

### Globe key / language picker

The globe key label always shows the current language code (e.g. `EN`, `DE`, `RU`, `UK`).
Tapping it opens a `LinearLayout` overlay stacked in the `FrameLayout` container. The overlay
lists every language the user has enabled in Settings. The active language is shown in yellow
(`#FFD60A`) with a darker row background. Tapping a row switches immediately and closes the
overlay. Tapping any non-globe key also closes it.

If only one language is enabled, the globe key hands off to the next installed IME instead
(`switchToNextInputMethod(false)`).

### Row height vs key height

All rows have the same height = key height. The keyboard panel height is
`rows × keyHeightPx + (rows − 1) × GAP_PX`. There is no fixed percentage of screen height.

---

## Long-press Backspace

`KeyboardView` uses a `Handler(Looper.getMainLooper())` for repeat-delete:

```
ACTION_DOWN on ⌫
  → postDelayed(400 ms) {
        isRepeatActive = true
        repeatDeleteRunnable.run()   // fires delete, reschedules every 55 ms
    }

ACTION_UP
  → removeCallbacksAndMessages(null)   // cancels any pending repeat
  → if (!isRepeatActive) fire single delete   // normal short tap
  → isRepeatActive = false
```

- **400 ms** initial delay before repeat begins — long enough not to trigger accidentally.
- **55 ms** repeat interval ≈ 18 deletions/second — fast enough to clear a word quickly,
  slow enough that the user can react and release.
- On `ACTION_UP` after a long press, **no extra single delete is fired** — avoids deleting
  one character too many on release.

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
  surrounding text (auto-correct is a future opt-in feature, default OFF).
- Nothing is written to disk except `UserPreferences` (SharedPreferences, device-local).
- No clipboard reading.

### User-facing disclosures
1. **Play Store listing** — first line of description: *"All keys are 10 mm × 10 mm squares.
   Zero data collection — no internet permission, ever."*
2. **Onboarding** — Privacy pledge screen must be shown before first use; user taps to acknowledge.
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

## Language Support

### Layout routing (`LayoutProvider`)

```
locale.language  →  layout
───────────────────────────────────────────
"de"             →  GermanLayout   (QWERTZ + ä ö ü ß)
"ru","bg","sr","mk","be"  →  CyrillicLayout  (ЙЦУКЕН)
"uk"             →  UkrainianLayout (ЙЦУКЕН-UA: ї і є, no ъ ы)
"ar","he","fa","ur"  →  LatinLayout   (placeholder until v1.1)
everything else  →  LatinLayout   (QWERTY)
```

### Language selection flow

1. User opens **Settings** and ticks languages via checkboxes. The list is stored as a
   comma-separated string in `UserPreferences.enabledLanguages` (SharedPreferences, synchronous).
2. On keyboard show (`onStartInputView`), the service reads `enabledLanguages`. If
   `currentLanguageIndex` is out of range it resets to 0.
3. Globe key label = `currentLanguageCode().uppercase()` (e.g. `"EN"`, `"DE"`).
4. Tapping globe opens the picker overlay; tapping a row sets `currentLanguageIndex`,
   calls `refreshLayout()`, closes the overlay.

### Adding a new language layout

1. Create `XxxLayout.kt` in `keyboard/layout/`.
2. Add the locale code(s) to a new `private val XXX_LOCALES = setOf(...)` in `LayoutProvider`.
3. Add a `when` branch in `LayoutProvider.alphaLayout()`.
4. Add the locale to `res/xml/method.xml` (new `<subtype>` entry).
5. Add a string to `res/values/strings.xml`.
6. Add the language to `SupportedLanguages.kt` if it should appear in the Settings picker.

---

## Testing Strategy

| Test type | Location | What to cover |
|---|---|---|
| Unit | `src/test/` | `KeyboardEngine` (key rect calculation), `LayoutProvider` (correct layout returned), `InputRouter` (action → commit mapping) |
| Robolectric | `src/test/` | `BigSquareKeyboardService` lifecycle, `UserPreferences` SharedPreferences reads/writes |
| Instrumented | `src/androidTest/` | KeyboardView renders correct number of keys, touch → commit end-to-end |

---

## Common Gotchas

1. **`InputMethodService.onCreateInputView()`** — return the root `FrameLayout` (container) here,
   not in `onCreate`. The language picker overlay is added/removed from this container.
2. **`getCurrentInputConnection()`** can return `null` — always null-check before calling `commitText`.
3. **Key size must be computed from `displayMetrics.xdpi` / `ydpi`** — do not use `density` or `densityDpi`.
4. **The IME window does not host a normal Activity** — do not use `startActivity` from inside the
   service without `FLAG_ACTIVITY_NEW_TASK`.
5. **SharedPreferences, not DataStore** — the IME service reads preferences synchronously in
   `onStartInputView`. DataStore is coroutine-based and requires a `lifecycleScope`; SharedPreferences
   is simpler and perfectly adequate for the small amount of data stored here.
6. **Long-press repeat uses `Handler(Looper.getMainLooper())`** — post the initial delay runnable on
   `ACTION_DOWN`; call `removeCallbacksAndMessages(null)` on `ACTION_UP` / `ACTION_CANCEL`.
   Do not use `Thread.sleep` or `postDelayed` loops elsewhere.
7. **Shift state** — `isShifted` is a `Boolean` in the service. `KeyboardView` uppercases
   single-character labels at draw time; the ⇧ key gets a blue background. Auto-reset to `false`
   after the first `CommitChar` (one-shot shift).
8. **Landscape mode** — keyboard panel height must be reduced in landscape. Implement
   `onComputeInsets()` (planned v1.1).
9. **`FLAG_SECURE` windows** — some apps (banking) set this flag; the keyboard still works but
   screenshot-based tests will fail on those windows.
10. **`€` / `£` are multi-byte** — use `KeyAction.CommitText("€")` not `CommitChar`; the Char
    type can represent them but `CommitText` is clearer and future-safe.

---

## File Naming Conventions

- Kotlin files: `PascalCase.kt`
- Layout XMLs: `snake_case.xml`
- Resource values: `snake_case`
- String keys: `snake_case`
- Constants: `SCREAMING_SNAKE_CASE` in companion objects or at file level

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

## Versioning

- **v1.0** *(current)*: Wrapped QWERTY / QWERTZ / ЙЦУКЕН / ЙЦУКЕН-UA, numbers layer,
  symbols layer (32 chars), shift visual feedback (lowercase labels + blue ⇧),
  long-press delete with 400 ms delay / 55 ms repeat, language picker overlay on globe key,
  globe key shows active language code, 15 languages in settings picker,
  privacy onboarding, SharedPreferences language storage
- **v1.1**: Long-press accented characters (à é ñ ü…), dedicated Arabic / Hebrew layouts,
  landscape compact mode (`onComputeInsets`)
- **v1.2**: Theme support (dark / light / high-contrast), key height multiplier setting (1.0×–1.5×)
- **v2.0**: CJK / Indic dedicated layouts
