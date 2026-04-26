package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.util.fakeMetadata
import com.lvsmsmch.deckbuilder.util.fakeSearchResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CardRepositoryImplTest {

    private val api: HearthstoneApi = mockk()
    private val metadata: MetadataRepository = mockk()
    private val locales: CurrentLocaleProvider = mockk()
    private val current = MutableStateFlow<Metadata?>(fakeMetadata())
    private lateinit var repo: CardRepositoryImpl

    @Before
    fun setUp() {
        coEvery { metadata.current } returns current
        coEvery { metadata.loadFromCache(any()) } returns null
        coEvery { locales.resolve(any()) } answers { firstArg<String?>() ?: "en_US" }
        repo = CardRepositoryImpl(api, metadata, locales)
    }

    @Test
    fun `searchCards builds expected base params`() = runTest {
        val params = slot<Map<String, String>>()
        coEvery { api.searchCards(capture(params)) } returns fakeSearchResponse()

        val r = repo.searchCards(filters = CardFilters(), page = 1, pageSize = 60, locale = null)
        assertTrue(r is Result.Success)

        val p = params.captured
        assertEquals("en_US", p["locale"])
        assertEquals("1", p["page"])
        assertEquals("60", p["pageSize"])
        assertEquals("constructed", p["gameMode"])
        assertEquals("manaCost:asc", p["sort"])
        assertEquals("1", p["collectible"])
    }

    @Test
    fun `searchCards passes class and rarity filters as csv`() = runTest {
        val params = slot<Map<String, String>>()
        coEvery { api.searchCards(capture(params)) } returns fakeSearchResponse()

        repo.searchCards(
            filters = CardFilters(
                classes = setOf("mage", "druid"),
                rarities = setOf("legendary"),
            ),
            page = 1,
            pageSize = 60,
        )
        val cls = params.captured["class"]?.split(",")?.toSet()
        assertEquals(setOf("mage", "druid"), cls)
        assertEquals("legendary", params.captured["rarity"])
    }

    @Test
    fun `searchCards expands manaCost 7 to 7-10`() = runTest {
        val params = slot<Map<String, String>>()
        coEvery { api.searchCards(capture(params)) } returns fakeSearchResponse()

        repo.searchCards(
            filters = CardFilters(manaCosts = setOf(2, 7)),
            page = 1, pageSize = 60,
        )
        assertEquals("2,7,8,9,10", params.captured["manaCost"])
    }

    @Test
    fun `searchCards collectibleOnly false sends 0,1`() = runTest {
        val params = slot<Map<String, String>>()
        coEvery { api.searchCards(capture(params)) } returns fakeSearchResponse()
        repo.searchCards(
            filters = CardFilters(collectibleOnly = false),
            page = 1, pageSize = 60,
        )
        assertEquals("0,1", params.captured["collectible"])
    }

    @Test
    fun `searchCards uses explicit locale over prefs`() = runTest {
        val params = slot<Map<String, String>>()
        coEvery { api.searchCards(capture(params)) } returns fakeSearchResponse()
        repo.searchCards(filters = CardFilters(), page = 1, pageSize = 60, locale = "ru_RU")
        assertEquals("ru_RU", params.captured["locale"])
        coVerify { locales.resolve("ru_RU") }
    }

    @Test
    fun `searchCards surfaces network errors as Result Error`() = runTest {
        coEvery { api.searchCards(any()) } throws IOException("boom")
        val r = repo.searchCards(filters = CardFilters(), page = 1, pageSize = 60)
        assertTrue(r is Result.Error)
    }

    @Test
    fun `searchCards uses Metadata Empty fallback when no cache or current`() = runTest {
        current.value = null
        coEvery { metadata.loadFromCache(any()) } returns null
        coEvery { api.searchCards(any()) } returns fakeSearchResponse()
        val r = repo.searchCards(filters = CardFilters(), page = 1, pageSize = 60)
        assertTrue(r is Result.Success)
        // No crash means the Metadata.Empty fallback path holds.
    }
}
