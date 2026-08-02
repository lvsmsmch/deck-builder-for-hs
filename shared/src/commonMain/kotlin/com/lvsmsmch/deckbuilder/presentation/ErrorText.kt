package com.lvsmsmch.deckbuilder.presentation

import com.lvsmsmch.deckbuilder.domain.usecases.InvalidDeckCodeException
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.error_card_data_missing
import com.lvsmsmch.deckbuilder.resources.error_generic
import com.lvsmsmch.deckbuilder.resources.error_no_network
import com.lvsmsmch.deckbuilder.resources.error_server
import com.lvsmsmch.deckbuilder.resources.error_timeout
import com.lvsmsmch.deckbuilder.resources.import_error_invalid_code
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException

/**
 * Turns a failure into something a player can act on. Technical text
 * ("Truncated varint in deckstring stream", "HTTP 503") never reaches the UI;
 * it stays in logs and crash reports.
 */
fun Throwable.toUiText(): UiText = when {
    this is InvalidDeckCodeException -> UiText.of(Res.string.import_error_invalid_code)
    this is HttpRequestTimeoutException -> UiText.of(Res.string.error_timeout)
    this is ResponseException -> UiText.of(Res.string.error_server)
    this is IOException -> UiText.of(Res.string.error_no_network)
    isMissingCardData() -> UiText.of(Res.string.error_card_data_missing)
    else -> UiText.of(Res.string.error_generic)
}

/**
 * The repositories signal "no cards for this locale yet" through plain
 * IllegalStateException from `error(...)`; recognise those so the user is told
 * to load card data rather than shown a generic failure.
 */
private fun Throwable.isMissingCardData(): Boolean {
    val text = message ?: return false
    return text.contains("cannot resolve latest build", ignoreCase = true) ||
        text.contains("not found in HsJson pool", ignoreCase = true)
}
