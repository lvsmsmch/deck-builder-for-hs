package com.lvsmsmch.deckbuilder.presentation

import com.lvsmsmch.deckbuilder.domain.entities.Card
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Hand-off from the card screen back to the deck editor. The editor leaves the
 * composition while the card screen sits on top of it, so a picked card waits
 * in the buffer until the editor resumes and collects it.
 */
class PendingDeckAdditions {

    private val _requests = Channel<Card>(Channel.BUFFERED)
    val requests: Flow<Card> = _requests.receiveAsFlow()

    fun add(card: Card) {
        _requests.trySend(card)
    }
}
