package com.lvsmsmch.deckbuilder

import android.app.Application
import android.util.Log
import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.di.dataModule
import com.lvsmsmch.deckbuilder.di.domainModule
import com.lvsmsmch.deckbuilder.di.networkModule
import com.lvsmsmch.deckbuilder.di.presentationModule
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.usecases.RefreshMetadataUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DeckBuilderApp : Application() {

    private val refreshMetadata: RefreshMetadataUseCase by inject()
    private val crashReporter: CrashReporter by inject()

    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, t ->
            Log.w(TAG, "Background work failed: ${t.message}", t)
            crashReporter.recordException(t)
        },
    )

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@DeckBuilderApp)
            modules(
                networkModule,
                dataModule,
                domainModule,
                presentationModule,
            )
        }
        crashReporter.bindToPreferences()
        kickOffMetadataRefresh()
    }

    /** Plan §10.11: hydrate from Room first, then refresh from network in the background. */
    private fun kickOffMetadataRefresh() {
        appScope.launch {
            when (val r = refreshMetadata()) {
                is Result.Success -> Log.i(TAG, "Metadata refreshed (${r.data.refreshedAtMs})")
                is Result.Error -> {
                    Log.w(TAG, "Metadata refresh failed", r.throwable)
                    crashReporter.log("Metadata refresh failed: ${r.throwable.message}")
                }
            }
        }
    }

    private companion object {
        const val TAG = "DeckBuilderApp"
    }
}
