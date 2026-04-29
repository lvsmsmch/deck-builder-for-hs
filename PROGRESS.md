# Progress

Migration: Blizzard API → HearthstoneJSON. См. `PLAN.md`.

- [~] Phase 1 — Kotlin deckstring codec, drop Blizzard deck endpoint
  - [x] codec + tests
  - [ ] DeckRepositoryImpl swap (deferred to Phase 3, requires local card lookup)
- [ ] Phase 2 — HearthstoneJSON cards loader and Room cache
- [ ] Phase 3 — Local search and card lookup over HsJson pool
- [ ] Phase 4 — Standard rotation via python-hearthstone enums
- [ ] Phase 5 — HsJson tiles in deck list and saved decks
- [ ] Phase 6 — Hardcoded localized labels, drop metadata layer
- [ ] Phase 7 — Remove Blizzard auth and api layer
- [ ] Phase 8 — Drop Battlegrounds, Card Backs, Glossary screens
- [ ] Phase 9 — Background update flow and UX
- [ ] Phase 10 — Tests and docs cleanup
