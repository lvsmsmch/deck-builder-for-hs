# HearthstoneJSON API — LLM Reference

A self-contained reference for the HearthstoneJSON API. Designed to be fed directly into an LLM prompt so it can correctly construct requests, parse responses, and build image URLs.

## What it is

A free public API exposing all Hearthstone card data, maintained by the HearthSim team. Data is automatically extracted from the game files on every patch. **No registration, OAuth, or API keys required** — just plain GET requests against a CDN.

The website itself is licensed CC0. Card data remains © Blizzard Entertainment. The project is not affiliated with Blizzard.

For commercial use, the maintainers ask that you reach out at `contact@hearthsim.net`.

## Base URLs

- **Card data (JSON):** `https://api.hearthstonejson.com/v1/`
- **Images / renders (CDN):** `https://art.hearthstonejson.com/v1/`

URL pattern: `/v1/{BUILD}/{LOCALE}/{FILE}`. Use `latest` in place of `{BUILD}` to get a 302 redirect to the current build.

## Data endpoints

| URL | Returns |
|---|---|
| `https://api.hearthstonejson.com/v1/latest/enUS/cards.json` | **All** cards, including non-collectible (heroes, hero powers, enchantments/buffs, tokens, etc.) |
| `https://api.hearthstonejson.com/v1/latest/enUS/cards.collectible.json` | **Only collectible** cards — what shows up in a player's collection. Use this if non-collectibles aren't needed. |
| `https://api.hearthstonejson.com/v1/enums.json` | Full enum values and the list of available locales |

Available locales (`{LOCALE}`): `enUS`, `enGB`, `deDE`, `esES`, `esMX`, `frFR`, `itIT`, `jaJP`, `koKR`, `plPL`, `ptBR`, `ruRU`, `thTH`, `zhCN`, `zhTW`. The full current list is in `enums.json`.

## Card object structure

Fields are only present when their value is non-zero / non-empty.

```json
{
  "id": "EX1_116",
  "dbfId": 559,
  "name": "Leeroy Jenkins",
  "text": "<b>Charge</b>. <b>Battlecry:</b> Summon two 1/1 Whelps for your opponent.",
  "flavor": "At least he has Angry Chicken.",
  "artist": "Gabe from Penny Arcade",
  "attack": 6,
  "cardClass": "NEUTRAL",
  "collectible": true,
  "cost": 5,
  "elite": true,
  "faction": "ALLIANCE",
  "health": 2,
  "mechanics": ["BATTLECRY", "CHARGE"],
  "rarity": "LEGENDARY",
  "set": "EXPERT1",
  "type": "MINION"
}
```

### Identifiers

- `id` (string) — primary card identifier (e.g. `EX1_116`). Use this everywhere — for cross-referencing cards and for fetching images.
- `dbfId` (int) — numeric ID from the DBF; used inside Hearthstone deck codes.

### Enum-valued fields (strings)

- `type` — `HERO`, `MINION`, `SPELL`, `ENCHANTMENT` (buff), `WEAPON`, `HERO_POWER`.
- `cardClass` — class (formerly `playerClass`). `NEUTRAL`, `MAGE`, `WARRIOR`, `PRIEST`, `ROGUE`, `PALADIN`, `HUNTER`, `WARLOCK`, `SHAMAN`, `DRUID`, `DEMONHUNTER`, `DEATHKNIGHT`, etc.
- `rarity` — `FREE`, `COMMON`, `RARE`, `EPIC`, `LEGENDARY`. Cards with no rarity gem are those in the `CORE` set.
- `set` — the card's set. Also determines the watermark. Examples: `CORE`, `EXPERT1`, `NAXX`, `GVG`, `BRM`, `TGT`, `LOE`, plus all newer expansion codes.
- `faction` — `ALLIANCE`, `HORDE`, `NEUTRAL`. Unused in-game; data is unreliably stored.
- `race` — usually only on minions (`DRAGON`, `MURLOC`, `BEAST`, `DEMON`, `MECHANICAL`, `PIRATE`, `ELEMENTAL`, `TOTEM`, `UNDEAD`, `NAGA`, `ALL`, etc.).
- `multiClassGroup` — multiclass affiliation (`GRIMY_GOONS`, `JADE_LOTUS`, `KABAL`).
- `classes` — array of classes for multiclass cards. When present, `cardClass` is usually `NEUTRAL`.

Full list of every enum value: <https://github.com/HearthSim/python-hearthstone/blob/master/hearthstone/enums.py>

### Localized strings

`name`, `text`, `flavor`, `howToEarn`, `howToEarnGolden`, `targetingArrowText`, `collectionText` (a generic version of the text for cards that have two — e.g. Jade Golem cards, where `text` contains `{}` placeholders).

### Numeric / boolean fields

- `cost` (int) — mana cost. Always shown for minions/spells/weapons even if 0.
- `attack` (int) — always shown for minions and weapons.
- `health` (int) — always shown for minions and heroes.
- `durability` (int) — for weapons.
- `armor` (int) — for heroes.
- `collectible` (bool) — whether the card is collectible.
- `elite` (bool) — legendary frame (gold dragon around the card).
- `hideStats` (bool) — whether cost/atk/health should be hidden in display.
- `overload` (int) — Shaman overload value.
- `spellDamage` (int) — bonus to spell damage.

### Tag arrays

- `mechanics` — sorted array of `GameTag` flags **set** on the card. Common values: `BATTLECRY`, `DEATHRATTLE`, `CHARGE`, `TAUNT`, `DIVINE_SHIELD`, `STEALTH`, `WINDFURY`, `POISONOUS`, `FREEZE`, `IMMUNE`, `SECRET`, `QUEST`, `COMBO`, `CHOOSE_ONE`, `DISCOVER`, `INSPIRE`, `SILENCE`, `ADAPT`, `JADE_GOLEM`, `AURA`, `ENRAGED`, `MORPH`, `RITUAL`, `FORGETFUL`, `TOPDECK`, and others.
- `referencedTags` — tags the card *references* but doesn't have itself (e.g. Mad Scientist references `SECRET`).
- `entourage` — sorted array of card IDs the card can summon/generate randomly (e.g. Animal Companion, Ysera).
- `playRequirements` — object of `{key: param}` describing requirements to play or target with the card. Keys are `PlayReq` enum strings. Most params are `0`.

### Notes on specific tags

- `ImmuneToSpellpower` — set on cards that don't benefit from spell damage (e.g. Arcane Missiles).
- `InvisibleDeathrattle` — internal tag, mostly on boss cards.
- `AI_MUST_PLAY` — auto-cast AI hero powers.
- `COUNTER` — essentially just Counterspell.
- `EVIL_GLOW` — cards that glow red while in hand.
- `FORGETFUL` — "50% chance to attack the wrong target".
- `RITUAL` — buffs C'Thun.
- `UNTOUCHABLE` — minions that "do not count as minions".
- `TOPDECK` — revealed to the opponent when drawn.

For overload and spellpower, use the `overload` and `spellDamage` properties — not tags.

## Images

All images live on a separate CDN: `https://art.hearthstonejson.com/v1/`. Indexed by the same card `id`.

### Card art (artwork only — no frame, no text)

| Path | Description |
|---|---|
| `/orig/{ID}.png` | Original lossless PNG. Usually 512×512 but not guaranteed (some are 512×256). Use only if you really need lossless. |
| `/256x/{ID}.jpg` or `.webp` | 256×256, lossy |
| `/512x/{ID}.jpg` or `.webp` | 512×512, lossy |

Example: `https://art.hearthstonejson.com/v1/256x/EX1_001.jpg`

### Card tiles (the narrow rectangular thumbnail used in deck lists)

`/tiles/{ID}.{ext}` — 256×59, available as `.png`, `.jpg`, `.webp`.

Example: `https://art.hearthstonejson.com/v1/tiles/CS2_235.png`

### Full card renders (frame + text + stats — the card as displayed in the collection)

URL structure:

```
https://art.hearthstonejson.com/v1/render/{BUILD}/{LOCALE}/{RESOLUTION}/{CARD_ID}.{EXT}
```

- `{BUILD}` — `latest` or a specific build number
- `{LOCALE}` — e.g. `enUS`, `ruRU`, `deDE`
- `{RESOLUTION}` — `256x` or `512x` (width; height is currently 1.5× width)
- `{EXT}` — only `png` is supported right now (`webp` is on the wishlist)

Examples:

- `https://art.hearthstonejson.com/v1/render/latest/enUS/512x/EX1_001.png`
- `https://art.hearthstonejson.com/v1/render/latest/enUS/256x/EX1_001.png`
- `https://art.hearthstonejson.com/v1/render/latest/frFR/512x/EX1_001.png`
- `https://art.hearthstonejson.com/v1/render/latest/thTH/256x/EX1_001.png`

Renders are available for `MINION`, `SPELL`, `WEAPON`, and `HERO` types. Not for `ENCHANTMENT` or `HERO_POWER`.

If you need to render cards dynamically in a browser (e.g. with custom modifications), use the Sunwell library: <https://github.com/HearthSim/Sunwell>

## Practical guidelines

1. **Don't hotlink images directly from `art.hearthstonejson.com` on high-traffic sites.** The maintainers explicitly ask consumers to re-host images. Generation scripts from the game files are available in the HearthstoneJSON GitHub repo.
2. **Cache the JSON files.** `cards.json` only changes when the game patches — there's no point re-downloading on every request. To detect updates, follow the 302 from `/v1/latest/` and watch for the build number changing.
3. **"Everything is a card."** Heroes, hero powers, enchantment buffs, tokens, internal entities — all live in `cards.json`. For collection or deckbuilder UIs, use `cards.collectible.json`.
4. **`set` is what determines rotation.** Standard = the current rotation's sets + `CORE`; Wild = all sets. Rotation logic must be maintained on your side; the API doesn't expose "is in Standard."
5. **Fields can be missing.** Don't assume every card has, say, `attack`. Always check for presence. Spells have no `attack` or `health`.
6. **`text` contains HTML tags** (`<b>`, `<i>`, `\n`). Either render as HTML or strip the tags before displaying.
7. **Localization is per-file.** To support multiple languages, fetch one `cards.json` per `{LOCALE}` you need.

## Source repos and further docs

- API code and scripts: <https://github.com/HearthSim/HearthstoneJSON>
- Raw extracted game data: <https://github.com/HearthSim/hs-data>
- Full enum reference (Python): <https://github.com/HearthSim/python-hearthstone/blob/master/hearthstone/enums.py>
- Card ID conventions: <https://hearthsim.info/docs/cards/>
- Sunwell card renderer: <https://github.com/HearthSim/Sunwell>
- Official site: <https://hearthstonejson.com/>
