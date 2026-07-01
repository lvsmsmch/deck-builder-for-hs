package com.lvsmsmch.deckbuilder

import android.app.Application
import android.util.Log
import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.data.update.UpdateScheduler
import com.lvsmsmch.deckbuilder.di.dataModule
import com.lvsmsmch.deckbuilder.di.domainModule
import com.lvsmsmch.deckbuilder.di.networkModule
import com.lvsmsmch.deckbuilder.di.presentationModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DeckBuilderApp : Application() {

    private val crashReporter: CrashReporter by inject()

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

        UpdateScheduler.scheduleDaily(this)
    }

    private companion object {
        const val TAG = "DB.App"
    }
}
