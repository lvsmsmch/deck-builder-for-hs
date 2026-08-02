package com.lvsmsmch.deckbuilder.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.data.crash.CrashLogger
import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.data.db.createAppDatabase
import com.lvsmsmch.deckbuilder.data.prefs.userPrefsStore
import com.lvsmsmch.deckbuilder.presentation.platform.AppInfo
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkMonitor
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android implementations of the platform seams the shared code depends on. */
val platformModule = module {

    single { androidContext().userPrefsStore }
    single { createAppDatabase(androidContext()) }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { CrashReporter(prefs = get(), scope = get()) }
    single<CrashLogger> { get<CrashReporter>() }

    single<NetworkMonitor> {
        val context = androidContext()
        object : NetworkMonitor {
            override fun currentNetworkType(): NetworkType {
                val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as? ConnectivityManager ?: return NetworkType.None
                val network = manager.activeNetwork ?: return NetworkType.None
                val caps = manager.getNetworkCapabilities(network) ?: return NetworkType.None
                return when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.Wifi
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.Mobile
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkType.Wifi
                    else -> NetworkType.None
                }
            }
        }
    }

    single {
        AppInfo(
            versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID,
            isDebug = BuildConfig.DEBUG,
        )
    }
}
