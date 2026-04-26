package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Tiny renderer for the subset of HTML Blizzard ships in card text:
 * `<b>`, `<i>` and literal `\n`. Strips `$`/`#` markers (Spell Damage / Hero Power
 * scaling) — for static display we just show the digits.
 *
 * Not a real HTML parser. Doesn't handle nesting beyond the two tags or attributes,
 * because card text never uses any of that. If Blizzard ever adds new tags, fall
 * back to plain text by stripping unknown tags rather than crashing.
 */
fun cardTextToAnnotated(raw: String): AnnotatedString {
    val cleaned = raw.replace("$", "").replace("#", "")
    return buildAnnotatedString {
        var i = 0
        val len = cleaned.length
        while (i < len) {
            val c = cleaned[i]
            if (c == '<') {
                val close = cleaned.indexOf('>', startIndex = i)
                if (close < 0) {
                    append(c); i++; continue
                }
                val tag = cleaned.substring(i + 1, close).trim().lowercase()
                when (tag) {
                    "b" -> {
                        val endTag = cleaned.indexOf("</b>", startIndex = close + 1, ignoreCase = true)
                        if (endTag < 0) {
                            append(c); i++; continue
                        }
                        val inner = cleaned.substring(close + 1, endTag)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(inner) }
                        i = endTag + "</b>".length
                    }
                    "i" -> {
                        val endTag = cleaned.indexOf("</i>", startIndex = close + 1, ignoreCase = true)
                        if (endTag < 0) {
                            append(c); i++; continue
                        }
                        val inner = cleaned.substring(close + 1, endTag)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inner) }
                        i = endTag + "</i>".length
                    }
                    else -> {
                        // Unknown tag — drop it, continue with the inner content as-is.
                        i = close + 1
                    }
                }
            } else {
                append(c)
                i++
            }
        }
    }
}
