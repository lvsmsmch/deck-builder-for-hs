package com.lvsmsmch.deckbuilder.data.crash

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin

/**
 * Bridges to Firebase Crashlytics via CrashKiOS. The Firebase SDK itself is
 * linked and configured on the Swift side (FirebaseApp.configure()).
 */
class IosCrashLogger : CrashLogger {
    override fun log(message: String) {
        CrashlyticsKotlin.logMessage(message)
    }

    override fun recordException(t: Throwable) {
        CrashlyticsKotlin.sendHandledException(t)
    }
}
