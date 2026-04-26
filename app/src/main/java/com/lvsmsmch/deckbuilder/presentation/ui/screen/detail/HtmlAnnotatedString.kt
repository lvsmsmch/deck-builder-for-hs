package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Tiny renderer for the subset of HTML Blizzard ships in card text:
 * `<b>` and `<i>`. Strips `$`/`#` markers (Spell Damage / Hero Power
 * scaling) — for static display we just show the digits.
 *
 * Three things made the previous implementation leak raw `<b>` into the UI:
 *   1) tags other than b/i (e.g. `<br>`, `<image src>`) sometimes appeared inside
 *      a `<b>` block and the inner text was appended verbatim;
 *   2) nested tags (`<b>foo <i>bar</i></b>`) were not parsed recursively, so
 *      the inner `<i>` survived as plain text;
 *   3) when an opening `<b>` had no matching `</b>`, the `<` was emitted as a
 *      literal character.
 *
 * The fix: drop unknown tags up front via regex (so the parser only ever sees
 * `<b>`, `</b>`, `<i>`, `</i>`), then parse recursively, falling back to the
 * raw character if the closing tag is genuinely missing.
 */
private val UNKNOWN_TAG = Regex("<(?!\\s*/?\\s*[bi]\\s*>)[^>]*>", RegexOption.IGNORE_CASE)
private val BI_TAG = Regex("<\\s*(/?)\\s*([bi])\\s*>", RegexOption.IGNORE_CASE)

fun cardTextToAnnotated(raw: String): AnnotatedString {
    val cleaned = raw
        .replace("$", "")
        .replace("#", "")
        .replace(UNKNOWN_TAG, "")
        // Canonicalise b/i tags so the parser only ever sees `<b>` / `</b>` / `<i>` / `</i>`.
        .replace(BI_TAG) { m -> "<${m.groupValues[1]}${m.groupValues[2].lowercase()}>" }

    return buildAnnotatedString { appendWithTags(cleaned) }
}

private fun AnnotatedString.Builder.appendWithTags(text: String) {
    var i = 0
    val len = text.length
    while (i < len) {
        val c = text[i]
        if (c != '<') {
            append(c)
            i++
            continue
        }
        val close = text.indexOf('>', i)
        if (close < 0) {
            append(c)
            i++
            continue
        }
        val tag = text.substring(i + 1, close).trim().lowercase()
        when (tag) {
            "b" -> {
                val end = text.indexOf("</b>", startIndex = close + 1, ignoreCase = true)
                if (end < 0) {
                    // Unmatched opening tag — drop it rather than echo `<b>` to the screen.
                    i = close + 1
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendWithTags(text.substring(close + 1, end))
                    }
                    i = end + "</b>".length
                }
            }
            "i" -> {
                val end = text.indexOf("</i>", startIndex = close + 1, ignoreCase = true)
                if (end < 0) {
                    i = close + 1
                } else {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        appendWithTags(text.substring(close + 1, end))
                    }
                    i = end + "</i>".length
                }
            }
            // The pre-clean above strips everything else, but if a stray closing
            // tag survives (rare malformed input), drop it rather than echo it.
            else -> i = close + 1
        }
    }
}
