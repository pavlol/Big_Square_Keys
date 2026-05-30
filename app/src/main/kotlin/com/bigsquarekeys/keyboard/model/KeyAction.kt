package com.bigsquarekeys.keyboard.model

sealed class KeyAction {
    data class CommitChar(val char: Char) : KeyAction()
    data class CommitText(val text: String) : KeyAction()
    object Delete : KeyAction()
    object DeleteWord : KeyAction()
    object Enter : KeyAction()
    object Space : KeyAction()
    object ShiftToggle : KeyAction()
    object SwitchToNumbers : KeyAction()
    object SwitchToSymbols : KeyAction()
    object SwitchToAlpha : KeyAction()
    object SwitchLanguage : KeyAction()
    object OpenSettings : KeyAction()
}
