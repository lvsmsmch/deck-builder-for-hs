package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HtmlAnnotatedStringTest {

    @Test
    fun `plain text passes through unchanged`() {
        val result = cardTextToAnnotated("Deal 3 damage.")
        assertEquals("Deal 3 damage.", result.text)
    }

    @Test
    fun `dollar and hash markers are stripped`() {
        val result = cardTextToAnnotated("Deal \$3 damage. Heal #2 health.")
        assertEquals("Deal 3 damage. Heal 2 health.", result.text)
    }

    @Test
    fun `bold tag content is preserved without tags`() {
        val result = cardTextToAnnotated("<b>Battlecry:</b> Draw a card.")
        assertEquals("Battlecry: Draw a card.", result.text)
    }

    @Test
    fun `italic tag content is preserved without tags`() {
        val result = cardTextToAnnotated("<i>(May only have one Quest per deck.)</i>")
        assertEquals("(May only have one Quest per deck.)", result.text)
    }

    @Test
    fun `nested italic inside bold no longer leaks the inner tag`() {
        val result = cardTextToAnnotated("<b>Hello <i>world</i></b>")
        // Before the recursive rewrite the rendered text was 'Hello <i>world</i>'.
        assertEquals("Hello world", result.text)
        assertFalse("inner tag must not survive", "<i>" in result.text)
        assertFalse("inner tag must not survive", "</i>" in result.text)
    }

    @Test
    fun `unknown tags are dropped, surrounding text kept`() {
        val result = cardTextToAnnotated("Line one.<br>Line two.<br/>Line three.")
        assertEquals("Line one.Line two.Line three.", result.text)
    }

    @Test
    fun `image-style self-closing tags are dropped`() {
        val result = cardTextToAnnotated("Cost <image src='gem.png'/> to play.")
        assertEquals("Cost  to play.", result.text)
    }

    @Test
    fun `unmatched opening bold emits the literal character instead of swallowing it`() {
        val result = cardTextToAnnotated("Stat: <b>broken")
        // Without a closing </b> we can't apply the style, but we also must not
        // render '<b>' to the screen.
        assertFalse("<b>" in result.text)
    }

    @Test
    fun `tags with surrounding whitespace are still recognised`() {
        val result = cardTextToAnnotated("< b >Bold</ b >")
        assertEquals("Bold", result.text)
    }

    @Test
    fun `case insensitive tags`() {
        val result = cardTextToAnnotated("<B>Yes</B>")
        assertEquals("Yes", result.text)
    }
}
