package com.lvsmsmch.deckbuilder.presentation.ui.labels

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat

object CardLabels {

    val ClassOrder: List<String> = listOf(
        "druid", "hunter", "mage", "paladin", "priest",
        "rogue", "shaman", "warlock", "warrior",
        "demonhunter", "deathknight",
    )
    fun classRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "druid" -> Res.string.class_druid
        "hunter" -> Res.string.class_hunter
        "mage" -> Res.string.class_mage
        "paladin" -> Res.string.class_paladin
        "priest" -> Res.string.class_priest
        "rogue" -> Res.string.class_rogue
        "shaman" -> Res.string.class_shaman
        "warlock" -> Res.string.class_warlock
        "warrior" -> Res.string.class_warrior
        "demonhunter" -> Res.string.class_demonhunter
        "deathknight" -> Res.string.class_deathknight
        else -> Res.string.class_neutral
    }
    fun classShortRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "demonhunter" -> Res.string.class_demonhunter_short
        "deathknight" -> Res.string.class_deathknight_short
        else -> classRes(slug)
    }
    fun rarityRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "free" -> Res.string.rarity_free
        "common" -> Res.string.rarity_common
        "rare" -> Res.string.rarity_rare
        "epic" -> Res.string.rarity_epic
        "legendary" -> Res.string.rarity_legendary
        else -> Res.string.rarity_common
    }
    fun typeRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "minion" -> Res.string.type_minion
        "spell" -> Res.string.type_spell
        "weapon" -> Res.string.type_weapon
        "hero" -> Res.string.type_hero
        "location" -> Res.string.type_location
        else -> Res.string.type_minion
    }
    fun raceRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "beast" -> Res.string.race_beast
        "demon" -> Res.string.race_demon
        "dragon" -> Res.string.race_dragon
        "elemental" -> Res.string.race_elemental
        "mech" -> Res.string.race_mech
        "murloc" -> Res.string.race_murloc
        "naga" -> Res.string.race_naga
        "pirate" -> Res.string.race_pirate
        "quilboar" -> Res.string.race_quilboar
        "totem" -> Res.string.race_totem
        "undead" -> Res.string.race_undead
        else -> Res.string.race_beast
    }
    fun spellSchoolRes(slug: String?): StringResource = when (slug?.lowercase()) {
        "arcane" -> Res.string.school_arcane
        "fire" -> Res.string.school_fire
        "frost" -> Res.string.school_frost
        "holy" -> Res.string.school_holy
        "nature" -> Res.string.school_nature
        "shadow" -> Res.string.school_shadow
        "fel" -> Res.string.school_fel
        else -> Res.string.school_arcane
    }
    fun keywordRes(slug: String?): StringResource? = when (slug?.lowercase()) {
        "battlecry" -> Res.string.keyword_battlecry
        "deathrattle" -> Res.string.keyword_deathrattle
        "taunt" -> Res.string.keyword_taunt
        "divine-shield" -> Res.string.keyword_divine_shield
        "charge" -> Res.string.keyword_charge
        "rush" -> Res.string.keyword_rush
        "lifesteal" -> Res.string.keyword_lifesteal
        "poisonous" -> Res.string.keyword_poisonous
        "tradeable" -> Res.string.keyword_tradeable
        "discover" -> Res.string.keyword_discover
        "secret" -> Res.string.keyword_secret
        "combo" -> Res.string.keyword_combo
        "overload" -> Res.string.keyword_overload
        "windfury" -> Res.string.keyword_windfury
        "stealth" -> Res.string.keyword_stealth
        "reborn" -> Res.string.keyword_reborn
        "outcast" -> Res.string.keyword_outcast
        "finale" -> Res.string.keyword_finale
        "immune" -> Res.string.keyword_immune
        "spellpower", "spell-damage" -> Res.string.keyword_spell_damage
        else -> null
    }
    fun expansionRes(slug: String?): StringResource? = when (slug?.lowercase()) {
        "core" -> Res.string.set_core
        "the-lost-city" -> Res.string.set_the_lost_city
        "space" -> Res.string.set_space
        "island-vacation" -> Res.string.set_island_vacation
        "whizbangs-workshop" -> Res.string.set_whizbangs_workshop
        "wild-west" -> Res.string.set_wild_west
        "titans" -> Res.string.set_titans
        "battle-of-the-bands", "festival-of-legends" -> Res.string.set_festival_of_legends
        "return-of-the-lich-king" -> Res.string.set_return_of_the_lich_king
        "revendreth" -> Res.string.set_revendreth
        "the-sunken-city", "voyage-to-the-sunken-city" -> Res.string.set_voyage_to_the_sunken_city
        "alterac-valley" -> Res.string.set_alterac_valley
        "the-barrens", "barrens" -> Res.string.set_barrens
        "darkmoon-faire" -> Res.string.set_darkmoon_faire
        "black-temple", "outlands" -> Res.string.set_outlands
        "stormwind" -> Res.string.set_stormwind
        "karazhan", "kara" -> Res.string.set_kara
        "gangs", "gadgetzan" -> Res.string.set_gangs
        "og", "old-gods" -> Res.string.set_og
        "scholomance" -> Res.string.set_scholomance
        "lootapalooza" -> Res.string.set_lootapalooza
        else -> null
    }
    fun formatFilterRes(format: CardFormatFilter): StringResource = when (format) {
        CardFormatFilter.ALL -> Res.string.format_all
        CardFormatFilter.STANDARD -> Res.string.format_standard
        CardFormatFilter.WILD -> Res.string.format_wild
    }
    fun formatRes(format: GameFormat): StringResource = when (format) {
        GameFormat.STANDARD -> Res.string.format_standard
        GameFormat.WILD -> Res.string.format_wild
        GameFormat.CLASSIC -> Res.string.format_classic
        GameFormat.TWIST -> Res.string.format_twist
        GameFormat.UNKNOWN -> Res.string.format_unknown
    }
}

@Composable
fun classLabel(slug: String?): String = stringResource(CardLabels.classRes(slug))

@Composable
fun classShortLabel(slug: String?): String = stringResource(CardLabels.classShortRes(slug))

@Composable
fun rarityLabel(slug: String?): String = stringResource(CardLabels.rarityRes(slug))

@Composable
fun typeLabel(slug: String?): String = stringResource(CardLabels.typeRes(slug))

@Composable
fun raceLabel(slug: String?): String = stringResource(CardLabels.raceRes(slug))

@Composable
fun spellSchoolLabel(slug: String?): String = stringResource(CardLabels.spellSchoolRes(slug))

@Composable
fun keywordLabel(slug: String?, fallback: String): String =
    CardLabels.keywordRes(slug)?.let { stringResource(it) } ?: fallback

@Composable
fun expansionLabel(slug: String?, fallback: String): String =
    CardLabels.expansionRes(slug)?.let { stringResource(it) } ?: fallback

@Composable
fun formatFilterLabel(format: CardFormatFilter): String = stringResource(CardLabels.formatFilterRes(format))

@Composable
fun formatLabel(format: GameFormat): String = stringResource(CardLabels.formatRes(format))
