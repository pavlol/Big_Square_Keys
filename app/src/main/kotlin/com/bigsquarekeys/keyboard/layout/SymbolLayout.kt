package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/**
 * Symbol layer — opened by the [#+=] key from the numbers layer.
 * 4-column grid, same column count as NumberLayout so the key size stays consistent.
 *
 * Layout:
 *   @  #  $  €
 *   £  ¥  %  ^
 *   &  *  (  )
 *   [  ]  {  }
 *   <  >  /  \
 *   +  =  _  |
 *   ~  `  '  "
 *   ;  :  !  ?
 *   123(×2)  ⌫  ↵
 */
object SymbolLayout {

    fun build(): KeyboardLayout {
        val rows = listOf(
            listOf(
                Key("@",  KeyAction.CommitChar('@')),
                Key("#",  KeyAction.CommitChar('#')),
                Key("$",  KeyAction.CommitChar('$')),
                Key("€",  KeyAction.CommitText("€")),
            ),
            listOf(
                Key("£",  KeyAction.CommitText("£")),
                Key("¥",  KeyAction.CommitText("¥")),
                Key("%",  KeyAction.CommitChar('%')),
                Key("^",  KeyAction.CommitChar('^')),
            ),
            listOf(
                Key("&",  KeyAction.CommitChar('&')),
                Key("*",  KeyAction.CommitChar('*')),
                Key("(",  KeyAction.CommitChar('(')),
                Key(")",  KeyAction.CommitChar(')')),
            ),
            listOf(
                Key("[",  KeyAction.CommitChar('[')),
                Key("]",  KeyAction.CommitChar(']')),
                Key("{",  KeyAction.CommitChar('{')),
                Key("}",  KeyAction.CommitChar('}')),
            ),
            listOf(
                Key("<",  KeyAction.CommitChar('<')),
                Key(">",  KeyAction.CommitChar('>')),
                Key("/",  KeyAction.CommitChar('/')),
                Key("\\", KeyAction.CommitChar('\\')),
            ),
            listOf(
                Key("+",  KeyAction.CommitChar('+')),
                Key("=",  KeyAction.CommitChar('=')),
                Key("_",  KeyAction.CommitChar('_')),
                Key("|",  KeyAction.CommitChar('|')),
            ),
            listOf(
                Key("~",  KeyAction.CommitChar('~')),
                Key("`",  KeyAction.CommitChar('`')),
                Key("'",  KeyAction.CommitChar('\'')),
                Key("\"", KeyAction.CommitChar('"')),
            ),
            listOf(
                Key(";",  KeyAction.CommitChar(';')),
                Key(":",  KeyAction.CommitChar(':')),
                Key("!",  KeyAction.CommitChar('!')),
                Key("?",  KeyAction.CommitChar('?')),
            ),
            listOf(
                Key("123", KeyAction.SwitchToNumbers, widthMultiplier = 2f),
                Key("⌫",  KeyAction.Delete),
                Key("↵",  KeyAction.Enter),
            ),
        )
        return KeyboardLayout(rows, columns = 4)
    }
}
