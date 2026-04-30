package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.domain.usecases.AcknowledgeNewSetUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.AssembleDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.DeleteSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.GetCardDetailsUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.GetDeckByCodeUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.ImportDeckByCodeUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.IsDeckSavedUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.ObservePreferencesUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.ObserveSavedDecksUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.RenameSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SaveDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCardLocaleUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCrashReportingEnabledUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetThemeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetCardDetailsUseCase)
    factoryOf(::SearchCardsUseCase)
    factoryOf(::ImportDeckByCodeUseCase)
    factoryOf(::GetDeckByCodeUseCase)
    factoryOf(::AssembleDeckUseCase)
    factoryOf(::ObserveSavedDecksUseCase)
    factoryOf(::SaveDeckUseCase)
    factoryOf(::DeleteSavedDeckUseCase)
    factoryOf(::RenameSavedDeckUseCase)
    factoryOf(::IsDeckSavedUseCase)

    factoryOf(::ObservePreferencesUseCase)
    factoryOf(::SetThemeUseCase)
    factoryOf(::SetCardLocaleUseCase)
    factoryOf(::SetCrashReportingEnabledUseCase)
    factoryOf(::AcknowledgeNewSetUseCase)
}
