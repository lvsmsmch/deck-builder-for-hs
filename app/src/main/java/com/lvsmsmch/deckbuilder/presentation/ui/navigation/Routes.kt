package com.lvsmsmch.deckbuilder.presentation.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe Compose Navigation routes (Navigation 2.8+). */
sealed interface Route

@Serializable
data class Library(
    val initialKeyword: String? = null,
    val initialSetSlug: String? = null,
) : Route

@Serializable data object Builder : Route
@Serializable data object Saved : Route
@Serializable data object More : Route

@Serializable data class CardDetail(val idOrSlug: String) : Route
@Serializable data class DeckView(val code: String) : Route

@Serializable data object Settings : Route
@Serializable data object CardData : Route
