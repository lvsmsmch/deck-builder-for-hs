# Progress

Migration: Blizzard API → HearthstoneJSON. См. `PLAN.md`.

- [x] Phase 1 — Kotlin deckstring codec, drop Blizzard deck endpoint
  - [x] codec + tests
  - [x] DeckRepositoryImpl swap (done in Phase 3)
- [x] Phase 2 — HearthstoneJSON cards loader and Room cache
  - [x] HsJsonApi + DTO + entity-mapper
  - [x] Room cards table per (locale, dbfId), AppDatabase v3
  - [x] BuildChecker (latest-redirect parsing) + HsJsonRepository + per-locale build store
  - [x] DI wiring; background ensureLoaded + checkForUpdate on app start
  - [ ] Splash UI for first-run download progress (still deferred — phase 9 covers update UX)
- [x] Phase 3 — Local search and card lookup over HsJson pool
  - HsJsonCardEntity → Card domain mapper (slug normalization, art.hearthstonejson.com URLs)
  - CardRepositoryImpl: in-memory predicate + sort over HsJsonRepository snapshot
  - DeckRepositoryImpl: Deckstring codec + per-dbfId resolution via CardRepository
  - Removed Blizzard searchCards/card/deck endpoints, CardDto, DeckDto, mappers, related test
- [x] Phase 4 — Standard rotation via python-hearthstone enums
  - RotationApi: raw enums.py + GitHub commits (sha + committedAt)
  - EnumsParser: STANDARD_SETS tuple + full CardSet enum members
  - RotationStore (DataStore) + RotationRepositoryImpl with mutex, status() cross-check
  - DeckLegality.kt: isStandardLegal(deck/card), rotatedOut(deck)
  - DI wiring + DeckBuilderApp.kickOffRotationRefresh background load
  - Unit tests for EnumsParser
- [x] Phase 5 — HsJson tiles in deck list and saved decks
  - CardTile composable (256×59 hsjson tile URL)
  - DeckCardRow: tile-strip with rarity border, mana gem, count pill
  - SavedDeckRow: hero tile preview (96×32) replacing gradient placeholder
  - SavedDeckEntity.heroSlug + DeckPreview.heroSlug; AppDatabase v4 (destructive)
- [x] Phase 6 — Hardcoded localized labels, drop metadata layer
  - strings.xml en+ru: class/rarity/type/race/spell-school labels
  - CardLabels helpers (slug → @StringRes) for class/rarity/type/race/school
  - FilterSheet, CardLibrary ClassChips, DeckBuilder ClassPicker switched to stringResource
  - SavedDecksScreen className → classLabel(classSlug)
  - BuilderViewModel resolves heroCardId via hsjson hero search
  - Builder pool format restriction now reads from RotationRepository
  - Removed: MetadataRepository(+Impl), RefreshMetadataUseCase, Metadata entity,
    MetadataDao/Entity/Dto/Mappers, /hearthstone/metadata endpoint
  - DI/AppDatabase v5/DeckBuilderApp updated; metadata-related tests dropped
  - SettingsScreen: removed Refresh metadata row + section
  - GlossaryViewModel stubbed (full removal in phase 8)
  - NewSetBanner removed (banner UX returns later via hsjson)
- [x] Phase 7 — Remove Blizzard auth and api layer
  - Deleted `data/auth/` (OAuthApi, TokenCache, AuthInterceptor) and `data/network/`
    (HearthstoneApi, NetworkProviders, CardBackDto/TokenDto, CardBackMappers)
  - NetworkModule now creates HsJson OkHttp/Retrofit/Json directly; no OAuth/API qualifiers
  - CardBackRepositoryImpl stubbed to empty Page (full removal in phase 8)
  - app/build.gradle.kts: dropped BLIZZARD_* buildConfig fields, Properties helper
  - proguard-rules.pro: keep rule retargeted to `data.hsjson.dto` + `data.rotation`
  - README: rewrote intro, dropped credentials section, refreshed layout + architecture notes
- [x] Phase 8 — Drop Battlegrounds, Card Backs, Glossary screens
  - Deleted `presentation/ui/screen/{battlegrounds,cardbacks,glossary}/` entirely
  - Removed Battlegrounds/CardBacks/Glossary routes from `Routes.kt`, `AppNavGraph.kt`
  - BottomBar: dropped BG tab + Shield icon
  - MoreScreen: only Settings row remains (Glossary + Card backs entries gone)
  - Domain/Data: deleted `CardBackRepository(+Impl)`, `CardBack` entity, `SearchCardBacksUseCase`
  - Stripped `BattlegroundsMeta` field/class from `Card`, dropped `gameMode`/`tiers`/`GameMode` from `CardFilters`
  - DI: pruned vm/usecase/repo bindings across Presentation/Domain/Data modules
  - strings.xml (en+ru): removed `nav_battlegrounds`, `bg_*`, `glossary_*`, `cardbacks_*`, `more_glossary*`, `more_cardbacks*`
- [x] Phase 9 — Background update flow and UX
  - WorkManager `androidx.work:work-runtime-ktx` added; `UpdateWorker` (CoroutineWorker
    via Koin) scheduled daily by `UpdateScheduler.scheduleDaily()` from `DeckBuilderApp`
  - `data/update/`: `UpdateNotifier` (SharedFlow events + StateFlow rotation status),
    `UpdateRunner` (single entry-point used both at app start and from the worker —
    runs ensureLoaded + checkForUpdate + rotation refresh + cross-check, persists
    `lastUpdateCheckAtMs` pref)
  - Global `SnackbarHost` in `AppNavGraph` Scaffold; `CardsUpdated` event renders
    `snackbar_cards_updated` on every screen
  - `CardLibraryScreen` rotation-lag banner subscribes to `notifier.rotationStatus`,
    "Check again" button re-runs the runner
  - `SettingsScreen`: new "Card data" section showing current build + last check
    timestamp (formatted via `DateFormat.MEDIUM/SHORT`)
  - `AppPreferences.lastUpdateCheckAtMs` + DataStore key + `setLastUpdateCheckAt`
  - en + ru strings (settings_section_updates, settings_cards_build,
    settings_last_check, snackbar_cards_updated, library_rotation_lag_*)
  - Builder snapshot: pool already loads on screen entry via paginated
    `SearchCardsUseCase` (no live observation), so cards don't shift mid-edit
- [x] Phase 10 — Tests and docs cleanup
  - Existing test suite reviewed; no Blizzard-era stale tests remain (those were
    removed in earlier phases). Deckstring codec + EnumsParser tests carried over.
  - Added `BuildCheckerParseTest` — covers the build-segment URL parser
    (canonical redirect, non-default locale, missing/non-numeric build, empty
    URL). `parseBuildFromUrl` switched from `private` to `internal` so tests
    can hit it directly without spinning up an OkHttp roundtrip.
  - Added `HsJsonMappersTest` — DTO→entity (hot fields, leading/trailing
    comma CSV, payload preserved as JSON, multi-class precedence, null
    collectible defaulting) and entity→domain (slug normalisation,
    `art.hearthstonejson.com` URL shape, fallback `unknown` card type,
    class token resolution, domain-slug helper).
  - Added `DeckLegalityTest` — `isStandardLegal` for cards/decks (slug
    re-uppercasing to `BLACK_TEMPLE` token), `rotatedOut` ordering,
    `RotationStatus.isOutdated` cross-check both ways, and the
    `toRotationToken` helper.
  - README: added `data/update/` to the layout tree and a paragraph in the
    architecture notes describing `UpdateRunner` / `UpdateScheduler` /
    `UpdateNotifier` and the daily WorkManager flow.
