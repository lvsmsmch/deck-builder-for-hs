package com.lvsmsmch.deckbuilder.data.debug

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.datetime.Clock
import kotlin.time.TimeSource

class SessionLog {
    private val lock = SynchronizedObject()
    private val lines = ArrayDeque<String>()
    private val startedAt = Clock.System.now()
    private val timer = TimeSource.Monotonic.markNow()

    init {
        add("Session", "started $startedAt")
    }

    fun add(tag: String, message: String) {
        val elapsed = timer.elapsedNow().inWholeMilliseconds
        synchronized(lock) {
            lines.addLast("$elapsed [$tag] $message")
            while (lines.size > MAX_LINES) lines.removeFirst()
        }
    }

    fun dump(): String = synchronized(lock) {
        buildString {
            appendLine("Deck Builder debug log")
            appendLine("session=$startedAt")
            lines.forEach(::appendLine)
        }
    }

    private companion object {
        const val MAX_LINES = 2_000
    }
}
