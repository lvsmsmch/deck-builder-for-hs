# Deck Builder for Hearthstone

Native Android app for browsing the Hearthstone card pool. Card data and images come from [HearthstoneJSON](https://hearthstonejson.com/) (no Blizzard API credentials required). Browse and search the card pool, inspect cards, decode shared deck codes, build constructed decks and export the canonical deck code.

The architectural plan lives at `.claude/deck_builder_app_plan.md`. Design mockups (12 screens, dark theme) at `.claude/design-mockup.html`.

## Stack

Kotlin 2.1, Jetpack Compose (Material 3), Koin 4 DI, Retrofit 2 + OkHttp 4 + kotlinx-serialization, Coil 3 for images, Room for persistence, DataStore for prefs. Single-module, single-Activity. JVM 17, minSdk 26, targetSdk 35.

## Getting started

### 1. Open in Android Studio

Open the project root in Android Studio (Iguana / Hedgehog or newer). Wait for Gradle sync. The wrapper jar is generated automatically on first sync if missing.

### 2. (Optional) Firebase Crashlytics

Crashlytics is wired but inactive without configuration. To enable:

1. Register the app `com.lvsmsmch.deckbuilder` (debug variant: `com.lvsmsmch.deckbuilder.debug`) in [Firebase console](https://console.firebase.google.com/).
2. Download `google-services.json` and drop it in `app/`.
3. Re-sync. The conditional plugin block in `app/build.gradle.kts` picks it up automatically.

Without `google-services.json` the app builds and runs fine — `CrashReporter` becomes a no-op.

### 3. Build and run

Pick the `app` configuration, choose an emulator or device, hit Run. Default build variant is `debug`.

## Project layout

```
app/src/main/java/com/lvsmsmch/deckbuilder/
├── DeckBuilderApp.kt                Application + Koin start + background hydrate
├── MainActivity.kt                  Single-activity host
├── di/                              Koin modules (network / data / domain / presentation)
├── domain/
│   ├── common/                      Result, UiState
│   ├── entities/                    Card, Deck, AppPreferences, …
│   ├── repositories/                Interfaces only — clean-arch
│   └── usecases/                    One per user action
├── data/
│   ├── hsjson/                      HearthstoneJSON CDN: Retrofit api, DTOs, build checker, repository
│   ├── rotation/                    python-hearthstone STANDARD_SETS via raw GitHub
│   ├── deckstring/                  Kotlin deckstring encoder/decoder
│   ├── db/                          Room database, entities, DAOs
│   ├── prefs/                       DataStore + PreferencesRepository
│   ├── repository/                  Repository implementations
│   └── crash/                       CrashReporter wrapper
└── presentation/
    └── ui/
        ├── theme/                   Color tokens, typography, shapes, theme
        ├── navigation/              Type-safe routes + nav graph
        ├── components/              Reusable: CardThumbnail, ManaCurve, …
        └── screen/                  One folder per feature
```

## Languages

UI strings live in `res/values/strings.xml` (en) and `res/values-ru/strings.xml` (ru). The card data language follows the user's pick in **Settings → Card language** and persists via DataStore.

## Architecture notes

- `HsJsonRepository` downloads `cards.collectible.json` per locale, caches rows in Room (`cards` table), and invalidates against the build number returned by the `/v1/latest/{locale}/` redirect.
- `CardRepositoryImpl` runs all search/filter logic in-memory over the loaded HsJson snapshot — no network calls per query.
- `DeckRepositoryImpl` uses the in-tree deckstring codec (`data/deckstring/`) to decode/encode shared deck codes, resolving `dbfId → Card` via `CardRepository`.
- `RotationRepositoryImpl` parses `STANDARD_SETS` from `python-hearthstone/enums.py` (raw GitHub), backed by DataStore. Cross-checks against the loaded card pool to detect lag after a new expansion.
- Localized labels for class / rarity / type / race / spell school live in `strings.xml` (en + ru); slug → `@StringRes` lookup is in `presentation/ui/labels/CardLabels.kt`.

## Reference

- HearthstoneJSON: <https://hearthstonejson.com/>
- python-hearthstone enums: <https://github.com/HearthSim/python-hearthstone/blob/master/hearthstone/enums.py>
- Project plan (working document): `.claude/deck_builder_app_plan.md`
