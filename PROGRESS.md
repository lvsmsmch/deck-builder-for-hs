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
- [ ] Phase 4 — Standard rotation via python-hearthstone enums
- [ ] Phase 5 — HsJson tiles in deck list and saved decks
- [ ] Phase 6 — Hardcoded localized labels, drop metadata layer
- [ ] Phase 7 — Remove Blizzard auth and api layer
- [ ] Phase 8 — Drop Battlegrounds, Card Backs, Glossary screens
- [ ] Phase 9 — Background update flow and UX
- [ ] Phase 10 — Tests and docs cleanup
