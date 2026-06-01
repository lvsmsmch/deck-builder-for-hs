package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.presentation.ui.screen.builder.DeckBuilderViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview.DeckViewViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.detail.CardDetailViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.CardLibraryViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.SavedDecksViewModel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.settings.SettingsViewModel
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
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
    viewModel { (initialPreferences: AppPreferences) ->
        SettingsViewModel(
            observePrefs = get(),
            setThemeUseCase = get(),
            setCardLocale = get(),
            setCrashReporting = get(),
            hsJson = get(),
            updateRunner = get(),
            initialPreferences = initialPreferences,
        )
    }

    viewModel { (idOrSlug: String) ->
        CardDetailViewModel(
            idOrSlug = idOrSlug,
            getCardDetails = get(),
            prefs = get(),
        )
    }

    viewModel { (code: String, initialSavedName: String) ->
        DeckViewViewModel(
            code = code,
            initialSavedName = initialSavedName,
            getDeck = get(),
            isSaved = get(),
            saveDeck = get(),
            deleteDeck = get(),
            renameDeck = get(),
            savedRepo = get(),
            prefs = get(),
        )
    }
}
