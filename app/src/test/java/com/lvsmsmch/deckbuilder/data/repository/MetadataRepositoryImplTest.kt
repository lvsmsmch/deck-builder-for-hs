package com.lvsmsmch.deckbuilder.data.repository

import app.cash.turbine.test
import com.lvsmsmch.deckbuilder.data.db.dao.MetadataDao
import com.lvsmsmch.deckbuilder.data.db.entity.MetadataBlobEntity
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.util.fakeMetadataDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MetadataRepositoryImplTest {

    private val api: HearthstoneApi = mockk()
    private val dao: MetadataDao = mockk(relaxed = true)
    private val locales: CurrentLocaleProvider = mockk()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var clock: Long = 1_000_000L

    private fun newRepo(freshnessMs: Long = 60_000L) = MetadataRepositoryImpl(
        api = api,
        dao = dao,
        json = json,
        locales = locales,
        freshnessWindowMs = freshnessMs,
        nowMs = { clock },
    )

    @Before
    fun setUp() {
        coEvery { locales.resolve(any()) } answers {
            firstArg<String?>() ?: "en_US"
        }
    }

    @Test
    fun `loadFromCache returns null and leaves current null when no row`() = runTest {
        coEvery { dao.getBlob("en_US") } returns null
        val repo = newRepo()
        assertNull(repo.loadFromCache(null))
        assertNull(repo.current.value)
    }

    @Test
    fun `loadFromCache hydrates current from row`() = runTest {
        val payload = json.encodeToString(MetadataAllDto.serializer(), fakeMetadataDto())
        coEvery { dao.getBlob("en_US") } returns
            MetadataBlobEntity(locale = "en_US", payloadJson = payload, refreshedAtMs = 500L)
        val repo = newRepo()

        val meta = repo.loadFromCache(null)
        assertNotNull(meta)
        assertEquals("en_US", meta!!.locale)
        assertEquals(3, meta.classes.size)
        assertEquals(meta, repo.current.value)
    }

    @Test
    fun `refresh hits API, persists row, updates current`() = runTest {
        coEvery { api.metadata("en_US") } returns fakeMetadataDto()
        val saved = slot<MetadataBlobEntity>()
        coEvery { dao.upsert(capture(saved)) } just Runs
        val repo = newRepo()

        repo.current.test {
            assertNull(awaitItem())
            val r = repo.refresh()
            assertTrue(r is Result.Success)
            val meta = (r as Result.Success).data
            assertEquals(3, meta.classes.size)
            assertEquals("en_US", meta.locale)
            assertEquals(meta, awaitItem())
        }
        assertEquals("en_US", saved.captured.locale)
        assertEquals(clock, saved.captured.refreshedAtMs)
    }

    @Test
    fun `refresh within freshness window short-circuits when not forced`() = runTest {
        coEvery { api.metadata(any()) } returns fakeMetadataDto()
        val repo = newRepo(freshnessMs = 60_000L)
        // Prime cache via successful refresh.
        repo.refresh()
        clock += 1_000L  // still well inside the window

        val second = repo.refresh()
        assertTrue(second is Result.Success)
        coVerify(exactly = 1) { api.metadata("en_US") }
    }

    @Test
    fun `refresh force=true bypasses freshness window`() = runTest {
        coEvery { api.metadata(any()) } returns fakeMetadataDto()
        val repo = newRepo(freshnessMs = 60_000L)
        repo.refresh()
        repo.refresh(force = true)
        coVerify(exactly = 2) { api.metadata("en_US") }
    }

    @Test
    fun `refresh re-fetches when locale changes even within window`() = runTest {
        coEvery { api.metadata(any()) } returns fakeMetadataDto()
        val repo = newRepo(freshnessMs = 60_000L)
        repo.refresh(locale = "en_US")
        repo.refresh(locale = "ru_RU")
        coVerify(exactly = 1) { api.metadata("en_US") }
        coVerify(exactly = 1) { api.metadata("ru_RU") }
    }

    @Test
    fun `refresh surfaces API errors as Result Error`() = runTest {
        coEvery { api.metadata(any()) } throws IOException("offline")
        val repo = newRepo()
        val r = repo.refresh()
        assertTrue(r is Result.Error)
        assertEquals("offline", (r as Result.Error).throwable.message)
        assertNull(repo.current.value)
    }

    @Test
    fun `refresh resolves locale via provider when caller passes null`() = runTest {
        coEvery { locales.resolve(null) } returns "de_DE"
        coEvery { api.metadata("de_DE") } returns fakeMetadataDto()
        val repo = newRepo()
        repo.refresh(locale = null)
        coVerify { api.metadata("de_DE") }
    }

    @Test
    fun `loadFromCache survives RarityDto with null craftingCost and dustValue`() = runTest {
        // Battle.net returns "craftingCost":[null,null] for the "free" rarity (id=2);
        // before the fix this crashed kotlinx-serialization with JsonDecodingException.
        val payloadWithNulls = """
            {
              "sets":[],"setGroups":[],"types":[],
              "rarities":[
                {"id":1,"name":"Common","slug":"common","craftingCost":[40,400],"dustValue":[5,50]},
                {"id":2,"name":"Free","slug":"free","craftingCost":[null,null],"dustValue":[null,null]}
              ],
              "classes":[],"minionTypes":[],"keywords":[],
              "spellSchools":[],"gameModes":[],"mercenaryRoles":[]
            }
        """.trimIndent()
        coEvery { dao.getBlob("en_US") } returns
            MetadataBlobEntity(locale = "en_US", payloadJson = payloadWithNulls, refreshedAtMs = 500L)
        val repo = newRepo()

        val meta = repo.loadFromCache(null)
        assertNotNull(meta)
        assertEquals(2, meta!!.rarities.size)
        // Nulls must be filtered out at the mapper boundary so domain stays List<Int>.
        assertEquals(emptyList<Int>(), meta.rarities[2]!!.craftingCost)
        assertEquals(listOf(40, 400), meta.rarities[1]!!.craftingCost)
    }
}
