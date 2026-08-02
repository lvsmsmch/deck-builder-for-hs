package com.lvsmsmch.deckbuilder.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConcurrentCacheTest {

    @Test
    fun unbounded_cache_keeps_everything() {
        val cache = ConcurrentCache<Int, String>()
        repeat(100) { cache[it] = "v$it" }
        assertEquals("v0", cache[0])
        assertEquals("v99", cache[99])
    }

    @Test
    fun bounded_cache_evicts_the_least_recently_used_entry() {
        val cache = ConcurrentCache<String, String>(maxSize = 2)
        cache["a"] = "1"
        cache["b"] = "2"
        cache["a"]                 // 'a' becomes the most recent
        cache["c"] = "3"           // evicts 'b'

        assertEquals("1", cache["a"])
        assertNull(cache["b"])
        assertEquals("3", cache["c"])
    }

    @Test
    fun getOrPut_computes_once_and_then_serves_the_cached_value() {
        val cache = ConcurrentCache<String, Int>(maxSize = 4)
        var calls = 0
        val compute = { calls++; 42 }

        assertEquals(42, cache.getOrPut("k", compute))
        assertEquals(42, cache.getOrPut("k", compute))
        assertEquals(1, calls)
    }

    @Test
    fun clear_empties_the_cache() {
        val cache = ConcurrentCache<String, String>(maxSize = 4)
        cache["a"] = "1"
        cache.clear()
        assertNull(cache["a"])
    }
}
