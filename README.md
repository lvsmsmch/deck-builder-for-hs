# Deck Builder for Hearthstone

Native Android companion app for Blizzard's official Hearthstone Game Data API. Browse and search the entire card pool, inspect cards, decode shared deck codes, build constructed decks and export the canonical code, explore Battlegrounds tier lists, look up keywords, and view the card-back collection.

The architectural plan lives at `.claude/deck_builder_app_plan.md`. Design mockups (12 screens, dark theme) at `.claude/design-mockup.html`.

## Stack

Kotlin 2.1, Jetpack Compose (Material 3), Koin 4 DI, Retrofit 2 + OkHttp 4 + kotlinx-serialization, Coil 3 for images, Room for persistence, DataStore for prefs. Single-module, single-Activity. JVM 17, minSdk 26, targetSdk 35.

## Getting started

### 1. Open in Android Studio

Open the project root in Android Studio (Iguana / Hedgehog or newer). Wait for Gradle sync. The wrapper jar is generated automatically on first sync if missing.

### 2. Configure Blizzard API credentials

Copy `local.properties.example` → `local.properties` (this file is gitignored). Fill in:

```properties
sdk.dir=                              # filled in automatically by Android Studio
BLIZZARD_CLIENT_ID=your_client_id
BLIZZARD_CLIENT_SECRET=your_client_secret
```

You can register a client at <https://develop.battle.net/access/clients>. For `client_credentials` grant the redirect URL can be any dummy value.

### 3. (Optional) Firebase Crashlytics

Crashlytics is wired but inactive without configuration. To enable:

1. Register the app `com.lvsmsmch.deckbuilder` (debug variant: `com.lvsmsmch.deckbuilder.debug`) in [Firebase console](https://console.firebase.google.com/).
2. Download `google-services.json` and drop it in `app/`.
3. Re-sync. The conditional plugin block in `app/build.gradle.kts` picks it up automatically.

Without `google-services.json` the app builds and runs fine — `CrashReporter` becomes a no-op.

### 4. Build and run

Pick the `app` configuration, choose an emulator or device, hit Run. Default build variant is `debug`.

## Project layout

```
app/src/main/java/com/lvsmsmch/deckbuilder/
├── DeckBuilderApp.kt                Application + Koin start + metadata refresh
├── MainActivity.kt                  Single-activity host
├── di/                              Koin modules (network / data / domain / presentation)
├── domain/
│   ├── common/                      Result, UiState
│   ├── entities/                    Card, Deck, Metadata, AppPreferences, …
│   ├── repositories/                Interfaces only — clean-arch
│   └── usecases/                    One per user action
├── data/
│   ├── auth/                        OAuth + token cache + interceptor
│   ├── network/                     Retrofit api, DTOs, mappers
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

- `MetadataRepository` caches the entire `/hearthstone/metadata` payload as a JSON blob keyed by locale (Room table `metadata_blob`). Card mappers resolve `classId / rarityId / setId` against this in-memory snapshot.
- `TokenCache` (OAuth) is mutex-guarded — a thundering herd of requests after expiry triggers exactly one refresh.
- `AuthInterceptor` injects `Bearer` into authenticated calls and retries once on 401/403 after refreshing the token. The OAuth host is excluded to avoid recursion.
- `CardRepository.searchCards` builds query params from `CardFilters`; mana chip "7+" expands to `manaCost=7,8,9,10` because the API caps at 10.
- Deck Builder fetches class + neutral pools as two parallel searches and merges (the API's `class` param is single-value).
- New-set banner compares the highest `Expansion.id` in Standard against `prefs.lastSeenSetSlug`.

## Reference

- Battle.net Hearthstone API: <https://develop.battle.net/documentation/hearthstone>
- API client management: <https://develop.battle.net/access/clients>
- Project plan (working document): `.claude/deck_builder_app_plan.md`
