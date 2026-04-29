package com.lvsmsmch.deckbuilder.presentation.ui.labels

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lvsmsmch.deckbuilder.R

object CardLabels {

    val ClassOrder: List<String> = listOf(
        "druid", "hunter", "mage", "paladin", "priest",
        "rogue", "shaman", "warlock", "warrior",
        "demonhunter", "deathknight",
    )

    @StringRes
    fun classRes(slug: String?): Int = when (slug?.lowercase()) {
        "druid" -> R.string.class_druid
        "hunter" -> R.string.class_hunter
        "mage" -> R.string.class_mage
        "paladin" -> R.string.class_paladin
        "priest" -> R.string.class_priest
        "rogue" -> R.string.class_rogue
        "shaman" -> R.string.class_shaman
        "warlock" -> R.string.class_warlock
        "warrior" -> R.string.class_warrior
        "demonhunter" -> R.string.class_demonhunter
        "deathknight" -> R.string.class_deathknight
        else -> R.string.class_neutral
    }

    @StringRes
    fun classShortRes(slug: String?): Int = when (slug?.lowercase()) {
        "demonhunter" -> R.string.class_demonhunter_short
        "deathknight" -> R.string.class_deathknight_short
        else -> classRes(slug)
    }

    @StringRes
    fun rarityRes(slug: String?): Int = when (slug?.lowercase()) {
        "free" -> R.string.rarity_free
        "common" -> R.string.rarity_common
        "rare" -> R.string.rarity_rare
        "epic" -> R.string.rarity_epic
        "legendary" -> R.string.rarity_legendary
        else -> R.string.rarity_common
    }

    @StringRes
    fun typeRes(slug: String?): Int = when (slug?.lowercase()) {
        "minion" -> R.string.type_minion
        "spell" -> R.string.type_spell
        "weapon" -> R.string.type_weapon
        "hero" -> R.string.type_hero
        "location" -> R.string.type_location
        else -> R.string.type_minion
    }

    @StringRes
    fun raceRes(slug: String?): Int = when (slug?.lowercase()) {
        "beast" -> R.string.race_beast
        "demon" -> R.string.race_demon
        "dragon" -> R.string.race_dragon
        "elemental" -> R.string.race_elemental
        "mech" -> R.string.race_mech
        "murloc" -> R.string.race_murloc
        "naga" -> R.string.race_naga
        "pirate" -> R.string.race_pirate
        "quilboar" -> R.string.race_quilboar
        "totem" -> R.string.race_totem
        "undead" -> R.string.race_undead
        else -> R.string.race_beast
    }

    @StringRes
    fun spellSchoolRes(slug: String?): Int = when (slug?.lowercase()) {
        "arcane" -> R.string.school_arcane
        "fire" -> R.string.school_fire
        "frost" -> R.string.school_frost
        "holy" -> R.string.school_holy
        "nature" -> R.string.school_nature
        "shadow" -> R.string.school_shadow
        "fel" -> R.string.school_fel
        else -> R.string.school_arcane
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
