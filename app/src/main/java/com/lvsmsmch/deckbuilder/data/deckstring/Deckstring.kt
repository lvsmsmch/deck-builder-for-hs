package com.lvsmsmch.deckbuilder.data.deckstring

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Hearthstone deckstring codec.
 *
 * Canonical binary layout (HearthSim spec):
 *   0x00 reserved
 *   varint version (1)
 *   varint format (1=Wild, 2=Standard, 3=Classic, 4=Twist)
 *   varint numHeroes; numHeroes × varint hero dbfId
 *   varint numX1; numX1 × varint dbfId            (one copy)
 *   varint numX2; numX2 × varint dbfId            (two copies)
 *   varint numXN; numXN × (varint dbfId, varint count)
 *   [optional sideboard block:
 *     varint hasSideboards (0/1)
 *     if 1: three groups, each suffixed per-card with owner dbfId varint
 *   ]
 * The whole byte stream is base64-encoded.
 */
object Deckstring {

    private const val RESERVED_BYTE = 0
    private const val VERSION = 1

    fun decode(code: String): DeckstringPayload {
        val cleaned = code.trim()
        require(cleaned.isNotEmpty()) { "Deckstring is empty" }
        val raw = try {
            Base64.getDecoder().decode(cleaned)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Deckstring is not valid base64", e)
        }
        ByteArrayInputStream(raw).use { stream ->
            val reserved = stream.readByteOrThrow()
            require(reserved == RESERVED_BYTE) { "Bad reserved byte: $reserved" }
            val version = stream.readVarInt()
            require(version == VERSION) { "Unsupported deckstring version: $version" }

            val format = DeckstringFormat.fromCode(stream.readVarInt())

            val heroCount = stream.readVarInt()
            val heroes = ArrayList<Int>(heroCount).apply {
                repeat(heroCount) { add(stream.readVarInt()) }
            }

            val cards = ArrayList<DeckstringCard>()
            repeat(stream.readVarInt()) { cards += DeckstringCard(stream.readVarInt(), 1) }
            repeat(stream.readVarInt()) { cards += DeckstringCard(stream.readVarInt(), 2) }
            repeat(stream.readVarInt()) {
                val dbfId = stream.readVarInt()
                val count = stream.readVarInt()
                cards += DeckstringCard(dbfId, count)
            }

            val sideboards = if (stream.available() == 0) {
                emptyList()
            } else {
                val flag = stream.readVarInt()
                if (flag != 1) {
                    emptyList()
                } else {
                    buildList {
                        repeat(stream.readVarInt()) {
                            val dbfId = stream.readVarInt()
                            val owner = stream.readVarInt()
                            add(DeckstringSideboardCard(dbfId, 1, owner))
                        }
                        repeat(stream.readVarInt()) {
                            val dbfId = stream.readVarInt()
                            val owner = stream.readVarInt()
                            add(DeckstringSideboardCard(dbfId, 2, owner))
                        }
                        repeat(stream.readVarInt()) {
                            val dbfId = stream.readVarInt()
                            val count = stream.readVarInt()
                            val owner = stream.readVarInt()
                            add(DeckstringSideboardCard(dbfId, count, owner))
                        }
                    }
                }
            }

            return DeckstringPayload(format, heroes, cards, sideboards)
        }
    }

    fun encode(payload: DeckstringPayload): String {
        require(payload.heroes.isNotEmpty()) { "Deck must have at least one hero" }
        require(payload.cards.all { it.count > 0 && it.dbfId > 0 }) {
            "All card entries must have positive dbfId and count"
        }

        val out = ByteArrayOutputStream()
        out.write(RESERVED_BYTE)
        out.writeVarInt(VERSION)
        out.writeVarInt(payload.format.code)

        out.writeVarInt(payload.heroes.size)
        payload.heroes.forEach(out::writeVarInt)

        val (x1, x2, xn) = partitionByCount(payload.cards)
        writeGroup1(out, x1)
        writeGroup1(out, x2)
        writeGroupN(out, xn)

        if (payload.sideboards.isNotEmpty()) {
            out.writeVarInt(1)
            val (sx1, sx2, sxn) = partitionByCountSb(payload.sideboards)
            writeSbGroup1(out, sx1)
            writeSbGroup1(out, sx2)
            writeSbGroupN(out, sxn)
        }

        return Base64.getEncoder().encodeToString(out.toByteArray())
    }

    private fun partitionByCount(
        cards: List<DeckstringCard>,
    ): Triple<List<DeckstringCard>, List<DeckstringCard>, List<DeckstringCard>> {
        val x1 = cards.filter { it.count == 1 }.sortedBy { it.dbfId }
        val x2 = cards.filter { it.count == 2 }.sortedBy { it.dbfId }
        val xn = cards.filter { it.count > 2 }.sortedBy { it.dbfId }
        return Triple(x1, x2, xn)
    }

    private fun partitionByCountSb(
        cards: List<DeckstringSideboardCard>,
    ): Triple<List<DeckstringSideboardCard>, List<DeckstringSideboardCard>, List<DeckstringSideboardCard>> {
        val x1 = cards.filter { it.count == 1 }.sortedWith(compareBy({ it.ownerDbfId }, { it.dbfId }))
        val x2 = cards.filter { it.count == 2 }.sortedWith(compareBy({ it.ownerDbfId }, { it.dbfId }))
        val xn = cards.filter { it.count > 2 }.sortedWith(compareBy({ it.ownerDbfId }, { it.dbfId }))
        return Triple(x1, x2, xn)
    }

    private fun writeGroup1(out: ByteArrayOutputStream, group: List<DeckstringCard>) {
        out.writeVarInt(group.size)
        group.forEach { out.writeVarInt(it.dbfId) }
    }

    private fun writeGroupN(out: ByteArrayOutputStream, group: List<DeckstringCard>) {
        out.writeVarInt(group.size)
        group.forEach {
            out.writeVarInt(it.dbfId)
            out.writeVarInt(it.count)
        }
    }

    private fun writeSbGroup1(out: ByteArrayOutputStream, group: List<DeckstringSideboardCard>) {
        out.writeVarInt(group.size)
        group.forEach {
            out.writeVarInt(it.dbfId)
            out.writeVarInt(it.ownerDbfId)
        }
    }

    private fun writeSbGroupN(out: ByteArrayOutputStream, group: List<DeckstringSideboardCard>) {
        out.writeVarInt(group.size)
        group.forEach {
            out.writeVarInt(it.dbfId)
            out.writeVarInt(it.count)
            out.writeVarInt(it.ownerDbfId)
        }
    }
}

data class DeckstringPayload(
    val format: DeckstringFormat,
    val heroes: List<Int>,
    val cards: List<DeckstringCard>,
    val sideboards: List<DeckstringSideboardCard> = emptyList(),
) {
    val firstHero: Int? get() = heroes.firstOrNull()
}

data class DeckstringCard(val dbfId: Int, val count: Int)

data class DeckstringSideboardCard(val dbfId: Int, val count: Int, val ownerDbfId: Int)

enum class DeckstringFormat(val code: Int) {
    WILD(1),
    STANDARD(2),
    CLASSIC(3),
    TWIST(4);

    companion object {
        fun fromCode(code: Int): DeckstringFormat = entries.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown deckstring format code: $code")
    }
}

private fun ByteArrayInputStream.readByteOrThrow(): Int {
    val b = read()
    if (b < 0) throw IllegalArgumentException("Unexpected end of deckstring stream")
    return b
}

private fun ByteArrayInputStream.readVarInt(): Int {
    var result = 0
    var shift = 0
    while (true) {
        val b = read()
        if (b < 0) throw IllegalArgumentException("Truncated varint in deckstring stream")
        result = result or ((b and 0x7F) shl shift)
        if ((b and 0x80) == 0) return result
        shift += 7
        if (shift > 35) throw IllegalArgumentException("Varint too long in deckstring stream")
    }
}

private fun ByteArrayOutputStream.writeVarInt(value: Int) {
    require(value >= 0) { "Varint must be non-negative, got $value" }
    var v = value
    while (true) {
        val low7 = v and 0x7F
        v = v ushr 7
        if (v == 0) {
            write(low7)
            return
        }
        write(low7 or 0x80)
    }
}
