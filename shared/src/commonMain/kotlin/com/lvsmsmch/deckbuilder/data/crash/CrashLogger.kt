package com.lvsmsmch.deckbuilder.data.crash

/**
 * Crash-reporting seam. Android binds Firebase Crashlytics; platforms without
 * a crash SDK use [NoopCrashLogger].
 */
interface CrashLogger {
    fun log(message: String)
    fun recordException(t: Throwable)
}

object NoopCrashLogger : CrashLogger {
    override fun log(message: String) = Unit
    override fun recordException(t: Throwable) = Unit
}
