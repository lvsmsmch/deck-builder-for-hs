# Migration plan: Blizzard API → HearthstoneJSON

## Goal
Полностью убрать зависимость от Blizzard Hearthstone API. Перейти на HearthstoneJSON CDN для карт/изображений + локальный поиск + собственный deckstring-кодер. Standard-ротация — из python-hearthstone через raw GitHub. Глоссарий, Battlegrounds, Card Backs — выпилить.

## Конечное состояние

- ❌ Никаких близов: ни OAuth, ни `client_id/secret`, ни `AuthInterceptor`, ни `TokenCache`, ни сетевых вызовов к `*.battle.net`.
- ✅ Карты — `cards.collectible.json` от HearthstoneJSON, кэш в Room по локалям, инвалидация по build-номеру (HEAD `/v1/latest/`).
- ✅ Картинки — `art.hearthstonejson.com`: рендеры (Detail), tiles 256×59 (DeckView, SavedDecks).
- ✅ Поиск/фильтры — локальные in-memory по загруженному пулу.
- ✅ Decode/encode deck-кода — собственный Kotlin-порт алгоритма deckstring.
- ✅ Standard-сеты — `STANDARD_SETS` из `python-hearthstone/enums.py` через raw GitHub, с детектом отставания и баннером.
- ✅ Локализованные имена классов / rarity / типов / сетов — в `strings.xml` (en + ru).
- ❌ Battlegrounds экран — удалён.
- ❌ Card Backs экран — удалён.
- ❌ Glossary экран — удалён.

---

## Фазы

Каждая фаза заканчивается коммитом. Между фазами — `<<CONTINUE>>`.

### Фаза 1 — Deckstring decoder/encoder на Kotlin
- [ ] Написать `data/deckstring/Deckstring.kt`: `encode(deck): String` и `decode(code): DecodedDeck` (формат / heroes / cards as (dbfId, count)).
- [ ] Юнит-тесты на пачке известных кодов (привести 5-10 публичных deckstring-ов с известным составом).
- [ ] Заменить близовский `DeckRepositoryImpl.fetchDeck(code)` на локальный декод. Маппинг `dbfId → Card` пока через стаб (карты ещё придут в фазе 2-3).
- [ ] Удалить `DeckDto`, `DeckMappers`, относящиеся к близовскому формату ответа `/deck/{code}`.

**Commit:** `phase 1: kotlin deckstring codec, drop blizzard deck endpoint`

### Фаза 2 — HearthstoneJSON: загрузка и кэш карт
- [ ] Новый модуль `data/hsjson/`:
  - `HsJsonApi` (Retrofit): GET `cards.collectible.json` для конкретного билда и локали.
  - `BuildChecker`: HEAD `/v1/latest/{locale}/cards.collectible.json`, парсит build из Location.
  - `HsJsonRepository`: `loadCards(locale)`, `checkForUpdate()`.
- [ ] Новые DTO + мапперы под структуру HearthstoneJSON (id, dbfId, name, text, cost, attack, health, cardClass, rarity, set, mechanics, race, classes, multiClassGroup, …).
- [ ] Room: новая таблица `cards` (per-locale rows) или `cards_blob` per-locale (по аналогии с metadata-blob — обсуждается на этапе реализации; вероятно, нормализованные строки для индексации/фильтрации).
- [ ] Сохранять текущий `buildNumber` per-locale в DataStore.
- [ ] First-run: показать на splash-экране прогресс «Loading cards…», скачать JSON для текущей локали, заполнить таблицу, сохранить build.
- [ ] Subsequent runs: грузим из Room, фоном запускаем `checkForUpdate()`.
- [ ] Поддержка ETag через OkHttp cache (бонусом, бесплатно).

**Commit:** `phase 2: hearthstonejson cards loader and room cache`

### Фаза 3 — Миграция CardRepository на локальный пул
- [ ] `CardRepositoryImpl.searchCards(filters)` переписать: вместо сетевого вызова → in-memory фильтр по загруженному пулу.
- [ ] Все фильтры (cost, class, rarity, set, mechanics, text search) — локально.
- [ ] `CardRepository.getCard(id)` → лукап в Room.
- [ ] DeckRepository: маппинг `dbfId → Card` теперь работает через CardRepository.
- [ ] Удалить близовский `HearthstoneApi.searchCards`, `getCard`, относящиеся DTO/мапперы.

**Commit:** `phase 3: local search and card lookup over hsjson pool`

### Фаза 4 — Standard/Wild ротация
- [ ] `data/rotation/RotationApi`: GET raw URL `python-hearthstone/master/hearthstone/enums.py`, парс `STANDARD_SETS = (...)`.
- [ ] GitHub commits API: SHA + timestamp последнего коммита по `enums.py`.
- [ ] `RotationRepository.getStandardSets()`: локальный кэш в DataStore, фоновое обновление.
- [ ] Cross-check: `unknownSets = collectibleSets - standard - wild - knownNonStandard`. Если не пусто — флаг «STANDARD_SETS отстал», показывать неинвазивный баннер.
- [ ] Эффективный формат колоды (Standard/Wild) считается лениво: `isStandardLegal = all(card.set in standardSets)`. На UI — значок-предупреждение для сохранённых Standard-колод, в которых что-то ротировалось.
- [ ] Builder: при создании Standard-колоды picker фильтруется по standardSets.

**Commit:** `phase 4: standard rotation via python-hearthstone enums`

### Фаза 5 — Tiles в UI
- [ ] `CardThumbnail` (или новый `CardTile`): URL `https://art.hearthstonejson.com/v1/tiles/{id}.png`, размер 256×59.
- [ ] DeckView: список карт колоды показывает tile-строки вместо квадратных миниатюр.
- [ ] SavedDecks: превью использует tiles.
- [ ] Detail: оставляем полноценные рендеры (`/render/latest/{locale}/512x/{id}.png`).
- [ ] Coil: убедиться, что кэш конфигурирован адекватно.

**Commit:** `phase 5: hsjson tiles in deck list and saved decks`

### Фаза 6 — Локализованные лейблы метаданных
- [ ] Перенести имена классов, rarity, типов, рас в `strings.xml` (en + ru).
- [ ] Удалить `MetadataRepository`, `MetadataDao`, `MetadataBlobEntity`, `MetadataMappers`, близовские metadata DTO.
- [ ] Сеты: hardcoded map `setSlug → localizedName` в Kotlin (~30 актуальных сетов). При появлении нового — добавить вручную.
- [ ] `DeckBuilderApp`: убрать стартовый metadata refresh.

**Commit:** `phase 6: hardcoded localized labels, drop metadata layer`

### Фаза 7 — Удаление Blizzard infrastructure
- [ ] Удалить `data/auth/` целиком (`OAuthApi`, `TokenCache`, `AuthInterceptor`).
- [ ] Удалить близовский `HearthstoneApi`, оставшиеся DTO/мапперы.
- [ ] `NetworkProviders` / `NetworkModule`: убрать OAuth-клиент, оставить только HsJson и GitHub.
- [ ] `local.properties.example`: убрать `BLIZZARD_CLIENT_ID/SECRET`.
- [ ] `app/build.gradle.kts`: убрать `BuildConfig` поля для близов.
- [ ] README: переписать раздел «Configure Blizzard API credentials» — больше не нужны.

**Commit:** `phase 7: remove blizzard auth and api layer`

### Фаза 8 — Удаление выпиленных фич
- [ ] Battlegrounds: удалить `presentation/ui/screen/battlegrounds/` целиком, убрать из навигации, убрать пункт меню.
- [ ] Card Backs: удалить `presentation/ui/screen/cardbacks/`, навигацию, меню. Удалить `CardBackRepository`, `CardBackDto`, `CardBackMappers`.
- [ ] Glossary: удалить `presentation/ui/screen/glossary/`, навигацию, меню.
- [ ] `presentation/ui/screen/more/` — пересмотреть содержимое, удалить ссылки на удалённые фичи.
- [ ] `strings.xml`: вычистить неиспользуемые строки.
- [ ] DI-модули: убрать выпиленные репозитории и юзкейсы.

**Commit:** `phase 8: drop battlegrounds, cardbacks, glossary screens`

### Фаза 9 — Update flow и UX обновления
- [ ] `WorkManager` job: раз в сутки `checkForUpdate()` для cards и rotation.
- [ ] При старте приложения: один раз дёрнуть проверку фоном.
- [ ] Snackbar `Карты обновлены до билда XXXXXX` — только когда что-то реально применилось.
- [ ] Баннер на главном экране при `STANDARD_SETS` lag → кнопка «Проверить ещё раз».
- [ ] Settings: показать текущий build карт + дату последней проверки.
- [ ] Builder: при открытии редактора брать snapshot карт; обновления применять при выходе.

**Commit:** `phase 9: background update flow and UX`

### Фаза 10 — Чистка тестов и доков
- [ ] Привести в порядок существующие тесты (data-loading test suite — вероятно сломается).
- [ ] Удалить тесты на близовские пути.
- [ ] Добавить тесты на: deckstring codec, HsJson маппер, rotation cross-check, build-number парсинг.
- [ ] README: обновить архитектурные заметки и схему данных.
- [ ] CLAUDE.md / PROGRESS.md — финализировать.

**Commit:** `phase 10: tests and docs cleanup`

---

## Точки риска

- Builder во время апдейта — самый деликатный экран. Snapshot-стратегия обязательна.
- python-hearthstone задержка после rotation/нового сета — обработана баннером и cross-check.
- Размер cards.collectible.json (~3-5 МБ на локаль) — splash на первом запуске обязателен.
- Несколько локалей в кэше — LRU чистка, чтобы Room не пухла бесконечно (последние 2-3 локали).

## Не делаем (out of scope)

- Перевод приложения на multi-module — оставляем single-module.
- Замена Coil / Retrofit / Koin / Room — стек тот же.
- Перехостинг изображений HearthstoneJSON на свой CDN — для пет-проекта не нужно.
- Migration save-decks данных — Saved decks хранят deckstring-коды, а формат deckstring неизменен; переезжают сами.
