package com.lvsmsmch.deckbuilder.util

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Small thread-safe map for repository caches — the multiplatform stand-in for
 * ConcurrentHashMap, covering only what the caches need.
 *
 * With [maxSize] set it behaves as an LRU: reads refresh recency and the
 * least-recently-used entry is evicted once the cache is full. Unbounded
 * caches of card objects grew for the whole session, so anything holding
 * per-card or per-query values should pass a limit.
 */
class ConcurrentCache<K, V>(private val maxSize: Int = UNBOUNDED) {
    private val lock = SynchronizedObject()
    private val map = LinkedHashMap<K, V>()

    operator fun get(key: K): V? = synchronized(lock) { touch(key) }

    operator fun set(key: K, value: V) {
        synchronized(lock) {
            map.remove(key)
            map[key] = value
            evictIfNeeded()
        }
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        synchronized(lock) { touch(key) }?.let { return it }
        val computed = defaultValue()
        return synchronized(lock) {
            val existing = map[key]
            if (existing != null) {
                existing
            } else {
                map[key] = computed
                evictIfNeeded()
                computed
            }
        }
    }

    fun clear() {
        synchronized(lock) { map.clear() }
    }

    /** Re-inserts the entry so LinkedHashMap order reflects recency. */
    private fun touch(key: K): V? {
        val value = map.remove(key) ?: return null
        map[key] = value
        return value
    }

    private fun evictIfNeeded() {
        if (maxSize == UNBOUNDED) return
        while (map.size > maxSize) {
            val oldest = map.keys.firstOrNull() ?: return
            map.remove(oldest)
        }
    }

    private companion object {
        const val UNBOUNDED = -1
    }
}
