package com.lvsmsmch.deckbuilder.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lvsmsmch.deckbuilder.data.crash.CrashLogger
import com.lvsmsmch.deckbuilder.data.crash.NoopCrashLogger
import com.lvsmsmch.deckbuilder.data.db.createAppDatabase
import com.lvsmsmch.deckbuilder.presentation.platform.AppInfo
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkMonitor
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkType
import com.lvsmsmch.deckbuilder.presentation.platform.Toaster
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(documents?.path) { "Cannot resolve documents directory" }
}

val iosPlatformModule = module {

    single {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${documentsPath()}/user_prefs.preferences_pb".toPath() },
        )
    }

    single { createAppDatabase() }

    single<CrashLogger> { NoopCrashLogger }

    single<Toaster> {
        object : Toaster {
            override fun show(message: String) {
                println("Toast: $message")
            }
        }
    }

    // TODO: wire NWPathMonitor for real reachability; Wifi keeps downloads allowed.
    single<NetworkMonitor> {
        object : NetworkMonitor {
            override fun currentNetworkType(): NetworkType = NetworkType.Wifi
        }
    }

    single {
        AppInfo(
            versionName = NSBundle.mainBundle
                .objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0",
            applicationId = NSBundle.mainBundle.bundleIdentifier ?: "com.lvsmsmch.deckbuilder",
            isDebug = false,
        )
    }
}
