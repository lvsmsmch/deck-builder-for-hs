package com.lvsmsmch.deckbuilder

import android.app.Application
import android.util.Log
import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.di.dataModule
import com.lvsmsmch.deckbuilder.di.domainModule
import com.lvsmsmch.deckbuilder.di.networkModule
import com.lvsmsmch.deckbuilder.di.presentationModule
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
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

    private val crashReporter: CrashReporter by inject()
    private val hsJsonRepository: HsJsonRepository by inject()
    private val rotationRepository: RotationRepository by inject()
    private val prefs: PreferencesRepository by inject()

    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, t ->
            Log.w(TAG, "Background work failed: ${t.message}", t)
            crashReporter.recordException(t)
        },
    )

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: starting Koin (debug=${BuildConfig.DEBUG})")
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
        kickOffHsJsonLoad()
        kickOffRotationRefresh()
    }

    private fun kickOffRotationRefresh() {
        Log.i(TAG, "kickOffRotationRefresh: launching")
        appScope.launch {
            runCatching {
                val cached = rotationRepository.cached()
                if (cached == null) {
                    val fresh = rotationRepository.ensureLoaded()
                    Log.i(
                        TAG,
                        "kickOffRotationRefresh: first load standard=${fresh.standardSets.size} known=${fresh.knownSets.size}",
                    )
                } else {
                    val updated = rotationRepository.refresh()
                    if (updated != null && updated.sourceSha != cached.sourceSha) {
                        Log.i(TAG, "kickOffRotationRefresh: updated to sha=${updated.sourceSha?.take(8)}")
                    }
                }
            }.onFailure {
                Log.w(TAG, "kickOffRotationRefresh failed: ${it.message}", it)
                crashReporter.log("Rotation refresh failed: ${it.message}")
            }
        }
    }

    private fun kickOffHsJsonLoad() {
        Log.i(TAG, "kickOffHsJsonLoad: launching")
        appScope.launch {
            val locale = runCatching { prefs.current().cardLocale }.getOrDefault("en_US")
            runCatching {
                val snap = hsJsonRepository.ensureLoaded(locale)
                Log.i(TAG, "kickOffHsJsonLoad: ensured locale=${snap.locale} build=${snap.build} cards=${snap.cards.size}")
            }.onFailure {
                Log.w(TAG, "kickOffHsJsonLoad: ensureLoaded failed — ${it.message}", it)
                crashReporter.log("HsJson ensureLoaded failed: ${it.message}")
            }
            runCatching {
                val applied = hsJsonRepository.checkForUpdate(locale)
                if (applied != null) Log.i(TAG, "kickOffHsJsonLoad: updated to build=$applied")
            }.onFailure { Log.w(TAG, "kickOffHsJsonLoad: checkForUpdate failed — ${it.message}") }
        }
    }

    private companion object {
        const val TAG = "DB.App"
    }
}
