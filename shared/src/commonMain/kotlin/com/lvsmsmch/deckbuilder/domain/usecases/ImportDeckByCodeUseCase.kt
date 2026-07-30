package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository

class ImportDeckByCodeUseCase(
    private val decks: DeckRepository,
) {
    suspend operator fun invoke(code: String, locale: String? = null): Result<Deck> {
        val cleaned = extractDeckCode(code)
        if (!cleaned.matches(CODE_REGEX)) {
            return Result.Error(InvalidDeckCodeException(cleaned))
        }
        return decks.decodeByCode(cleaned, locale)
    }

    /**
     * Loose lower bound — Hearthstone share codes are URL-safe Base64 with `=` padding,
     * always longer than ~30 chars. Real validation comes from the API decoding.
     */
    private val CODE_REGEX = Regex("^[A-Za-z0-9+/=]{12,}$")

    private fun extractDeckCode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.matches(CODE_REGEX)) return trimmed
        return input
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .lastOrNull { it.matches(CODE_REGEX) }
            ?: trimmed
    }
}

class InvalidDeckCodeException(val code: String) :
    IllegalArgumentException("This deck code looks invalid.")
