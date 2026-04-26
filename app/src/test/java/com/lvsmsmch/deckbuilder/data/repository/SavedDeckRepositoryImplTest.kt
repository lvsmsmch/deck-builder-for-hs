package com.lvsmsmch.deckbuilder.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lvsmsmch.deckbuilder.data.db.AppDatabase
import com.lvsmsmch.deckbuilder.data.db.dao.SavedDeckDao
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33], application = android.app.Application::class)
class SavedDeckRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SavedDeckDao
    private lateinit var repo: SavedDeckRepositoryImpl
    private var clock: Long = 1_000L

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.savedDeckDao()
        repo = SavedDeckRepositoryImpl(dao = dao, nowMs = { clock })
    }

    @After
    fun tearDown() = db.close()

    private fun deck(code: String, name: String? = "Aggro Mage"): Deck {
        val mage = ClassMeta(4, "mage", "Mage")
        val type = CardType(4, "minion", "Minion")
        val hero = Card(
            id = 637, slug = "jaina", name = "Jaina", text = null, flavorText = null,
            image = "img", cropImage = null, artistName = null, manaCost = 0,
            attack = null, health = null, durability = null, armor = null,
            classes = listOf(mage), cardSet = null, rarity = null,
            cardType = type, minionType = null, spellSchool = null,
            keywords = emptyList(), collectible = false, childIds = emptyList(),
        )
        val minion = hero.copy(id = 1, slug = "novice", name = "Novice", manaCost = 2)
        return Deck(
            code = code,
            format = GameFormat.STANDARD,
            hero = hero,
            heroClass = mage,
            cards = listOf(DeckCardEntry(minion, count = 2)),
        )
    }

    @Test
    fun `save then observeAll emits the deck`() = runTest {
        repo.save(deck("AAECAQ"), name = "My Mage")
        repo.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAECAQ", list.first().code)
            assertEquals("My Mage", list.first().name)
            assertEquals(GameFormat.STANDARD, list.first().format)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isSaved reflects DB state`() = runTest {
        assertFalse(repo.isSaved("X"))
        repo.save(deck("X"), name = null)
        assertTrue(repo.isSaved("X"))
    }

    @Test
    fun `delete removes the row`() = runTest {
        repo.save(deck("Y"), name = null)
        assertTrue(repo.isSaved("Y"))
        repo.delete("Y")
        assertFalse(repo.isSaved("Y"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `save rejects blank code`() = runTest {
        repo.save(deck(code = ""), name = null)
    }

    @Test
    fun `save with null name falls back to default`() = runTest {
        repo.save(deck("Z", name = null), name = null)
        val list = repo.observeAll().let { flow ->
            var captured: List<com.lvsmsmch.deckbuilder.domain.entities.DeckPreview> = emptyList()
            flow.test {
                captured = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            captured
        }
        assertEquals(1, list.size)
        assertNotNull(list.first().name)
        assertTrue("default name should mention class", list.first().name.contains("Mage"))
    }

    @Test
    fun `re-save preserves createdAtMs but updates updatedAtMs`() = runTest {
        clock = 100L
        repo.save(deck("R"), name = "v1")
        clock = 200L
        repo.save(deck("R"), name = "v2")
        repo.observeAll().test {
            val row = awaitItem().first()
            assertEquals("v2", row.name)
            assertEquals(200L, row.savedAtMs)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
