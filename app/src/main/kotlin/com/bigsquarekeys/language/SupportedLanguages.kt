package com.bigsquarekeys.language

data class SupportedLanguage(
    val code: String,       // BCP-47 / locale language code
    val displayName: String,
    val nativeName: String,
)

val SUPPORTED_LANGUAGES = listOf(
    SupportedLanguage("en", "English",    "English"),
    SupportedLanguage("es", "Spanish",    "Español"),
    SupportedLanguage("fr", "French",     "Français"),
    SupportedLanguage("de", "German",     "Deutsch"),
    SupportedLanguage("it", "Italian",    "Italiano"),
    SupportedLanguage("pt", "Portuguese", "Português"),
    SupportedLanguage("pl", "Polish",     "Polski"),
    SupportedLanguage("nl", "Dutch",      "Nederlands"),
    SupportedLanguage("sv", "Swedish",    "Svenska"),
    SupportedLanguage("tr", "Turkish",    "Türkçe"),
    SupportedLanguage("ru", "Russian",    "Русский"),
    SupportedLanguage("uk", "Ukrainian",  "Українська"),
    SupportedLanguage("bg", "Bulgarian",  "Български"),
    SupportedLanguage("ar", "Arabic",     "العربية"),
    SupportedLanguage("he", "Hebrew",     "עברית"),
)

/** Language code used when nothing is stored yet. */
const val DEFAULT_LANGUAGE = "en"
