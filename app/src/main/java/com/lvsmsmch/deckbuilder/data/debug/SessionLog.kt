package com.lvsmsmch.deckbuilder.data.debug

import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionLog {
    private val lock = Any()
    private val lines = ArrayDeque<String>()
    private val startedAt = System.currentTimeMillis()

    init {
        add("Session", "started ${formatDate(startedAt)}")
    }

    fun add(tag: String, message: String) {
        val elapsed = SystemClock.elapsedRealtime()
        synchronized(lock) {
            lines.addLast("$elapsed [$tag] $message")
            while (lines.size > MAX_LINES) lines.removeFirst()
        }
    }

    fun dump(): String = synchronized(lock) {
        buildString {
            appendLine("Deck Builder debug log")
            appendLine("session=${formatDate(startedAt)}")
            lines.forEach(::appendLine)
        }
    }

    private fun formatDate(epochMs: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(epochMs))

    private companion object {
        const val MAX_LINES = 2_000
    }
}
