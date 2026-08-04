package com.lvsmsmch.deckbuilder.presentation.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe Compose Navigation routes (Navigation 2.8+). */
sealed interface Route

@Serializable data object Home : Route

@Serializable
data class Library(
    val initialKeyword: String? = null,
    val initialSetSlug: String? = null,
) : Route

@Serializable
data class Builder(
    val editCode: String? = null,
    val savedName: String? = null,
) : Route
@Serializable data object Saved : Route

/** Card library as a bottom-nav tab (the old push-from-More entry point). */
@Serializable data object Cards : Route
@Serializable data object More : Route

/** [fromBuilder] marks the trip that started in the deck editor, which is the
 *  only place where the card screen can offer to add the card to a deck. */
@Serializable data class CardDetail(val idOrSlug: String, val fromBuilder: Boolean = false) : Route
@Serializable data class DeckView(val code: String, val savedName: String? = null) : Route

@Serializable data object Settings : Route
@Serializable data object CardData : Route
