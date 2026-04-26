package com.lvsmsmch.deckbuilder.presentation.ui.screen.glossary

import com.lvsmsmch.deckbuilder.domain.entities.Keyword

data class GlossaryState(
    val query: String = "",
    /** Group letter (uppercase) → matching keywords sorted alphabetically. */
    val groups: List<Group> = emptyList(),
    val isMetadataReady: Boolean = false,
)

data class Group(
    val letter: String,
    val items: List<Keyword>,
)
