package com.lvsmsmch.deckbuilder.util

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Minimal thread-safe map for repository memory caches — the multiplatform
 * stand-in for the JVM's ConcurrentHashMap, covering only the operations the
 * caches actually use.
 */
class ConcurrentCache<K, V> {
    private val lock = SynchronizedObject()
    private val map = HashMap<K, V>()

    operator fun get(key: K): V? = synchronized(lock) { map[key] }

    operator fun set(key: K, value: V) {
        synchronized(lock) { map[key] = value }
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        synchronized(lock) { map[key] }?.let { return it }
        val computed = defaultValue()
        return synchronized(lock) { map.getOrPut(key) { computed } }
    }

    fun clear() {
        synchronized(lock) { map.clear() }
    }
}
