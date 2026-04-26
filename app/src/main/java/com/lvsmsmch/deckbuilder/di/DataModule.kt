package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.data.prefs.PreferencesRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.lvsmsmch.deckbuilder.data.repository.CardBackRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.CardRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.DeckRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.MetadataRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.SavedDeckRepositoryImpl
import com.lvsmsmch.deckbuilder.domain.repositories.CardBackRepository
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    // Preferences first — other repositories depend on the locale provider.
    singleOf(::PreferencesRepositoryImpl) { bind<PreferencesRepository>() }
    singleOf(::CurrentLocaleProvider)

    singleOf(::MetadataRepositoryImpl) { bind<MetadataRepository>() }
    singleOf(::CardRepositoryImpl) { bind<CardRepository>() }
    singleOf(::DeckRepositoryImpl) { bind<DeckRepository>() }
    singleOf(::SavedDeckRepositoryImpl) { bind<SavedDeckRepository>() }
    singleOf(::CardBackRepositoryImpl) { bind<CardBackRepository>() }

    // Crash reporting — safe no-op when google-services.json is missing.
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { CrashReporter(prefs = get(), scope = get()) }
}
