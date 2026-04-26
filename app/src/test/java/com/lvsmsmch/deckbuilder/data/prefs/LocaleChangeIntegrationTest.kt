package com.lvsmsmch.deckbuilder.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.lvsmsmch.deckbuilder.data.db.dao.MetadataDao
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.repository.MetadataRepositoryImpl
import com.lvsmsmch.deckbuilder.util.fakeMetadataDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * End-to-end integration of the chain reported by the user as broken:
 *
 *   SetCardLocaleUseCase → DataStore commit → CurrentLocaleProvider.resolve(null)
 *     → MetadataRepositoryImpl.refresh(force=true) hits API in the *new* locale
 *
 * We use real DataStore + a real CurrentLocaleProvider + real MetadataRepositoryImpl,
 * mocking only the HTTP/DB seam. runBlocking (instead of runTest) because the chain
 * crosses Dispatchers.IO inside MetadataRepositoryImpl.
 */
class LocaleChangeIntegrationTest {

    private lateinit var tmpFile: File
    private lateinit var scope: CoroutineScope
    private lateinit var store: DataStore<Preferences>
    private lateinit var prefs: PreferencesRepositoryImpl
    private lateinit var locales: CurrentLocaleProvider
    private lateinit var metadata: MetadataRepositoryImpl

    private val api: HearthstoneApi = mockk()
    private val dao: MetadataDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        tmpFile = Files.createTempFile("locale-int", ".preferences_pb").toFile().also { it.delete() }
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { tmpFile })
        prefs = PreferencesRepositoryImpl(store)
        locales = CurrentLocaleProvider(prefs)
        metadata = MetadataRepositoryImpl(
            api = api, dao = dao,
            json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
            locales = locales,
        )
        coEvery { dao.getBlob(any()) } returns null
        coEvery { dao.upsert(any()) } just Runs
        coEvery { api.metadata(any()) } returns fakeMetadataDto()
    }

    @After
    fun tearDown() {
        scope.cancel()
        tmpFile.delete()
    }

    @Test
    fun `setCardLocale then refresh hits API in the new locale`() = runBlocking {
        // Default locale at start.
        assertEquals("en_US", prefs.current().cardLocale)

        prefs.setCardLocale("ru_RU")
        // Critical invariant: after setCardLocale returns, current() sees the new value.
        assertEquals("ru_RU", prefs.current().cardLocale)

        val r = metadata.refresh(force = true)
        assertEquals(true, r is com.lvsmsmch.deckbuilder.domain.common.Result.Success)
        coVerify { api.metadata("ru_RU") }
    }

    @Test
    fun `repeated locale switch resolves through the new locale each time`() = runBlocking {
        prefs.setCardLocale("de_DE")
        metadata.refresh(force = true)
        prefs.setCardLocale("ja_JP")
        metadata.refresh(force = true)
        coVerify { api.metadata("de_DE") }
        coVerify { api.metadata("ja_JP") }
    }
}
