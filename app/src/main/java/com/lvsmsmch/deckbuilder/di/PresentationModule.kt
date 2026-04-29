package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.presentation.ui.screen.battlegrounds.BattlegroundsViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.builder.DeckBuilderViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.cardbacks.CardBacksViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview.DeckViewViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.detail.CardDetailViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.glossary.GlossaryViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.CardLibraryViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.SavedDecksViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModel { (initialKeyword: String?, initialSetSlug: String?) ->
        CardLibraryViewModel(
            searchCards = get(),
            prefs = get(),
            initialKeyword = initialKeyword,
            initialSetSlug = initialSetSlug,
        )
    }
    viewModelOf(::SavedDecksViewModel)
    viewModelOf(::DeckBuilderViewModel)
    viewModelOf(::BattlegroundsViewModel)
    viewModelOf(::GlossaryViewModel)
    viewModelOf(::CardBacksViewModel)
    viewModelOf(::SettingsViewModel)

    viewModel { (idOrSlug: String) ->
        CardDetailViewModel(
            idOrSlug = idOrSlug,
            getCardDetails = get(),
            prefs = get(),
        )
    }

    viewModel { (code: String) ->
        DeckViewViewModel(
            code = code,
            getDeck = get(),
            isSaved = get(),
            saveDeck = get(),
            deleteDeck = get(),
            prefs = get(),
        )
    }
}
