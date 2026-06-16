# Deck Builder for Hearthstone

Native Android app for browsing Hearthstone cards, inspecting card details,
building decks, importing shared deck codes, and saving decks locally.

Card data and images are provided by [HearthstoneJSON](https://hearthstonejson.com/).
Deck code import/export uses an in-app deckstring codec, so no Blizzard API
credentials are required.

## Features

- Browse and search the collectible Hearthstone card pool.
- Filter by class, format, rarity, card type, minion type, spell school, keyword,
  mana cost, and set.
- View localized card details and related cards.
- Build Standard or Wild decks and export the canonical deck code.
- Import existing Hearthstone deck codes.
- Save decks locally, rename them, delete them, and copy their codes.
- Choose app theme and card data language.
- Refresh card data in the background with WorkManager.

## Tech Stack

- Kotlin 2.1
- Jetpack Compose and Material 3
- Koin
- Retrofit, OkHttp, and kotlinx.serialization
- Coil
- Room
- DataStore
- WorkManager
- Firebase Crashlytics and Analytics, enabled only when Firebase config is present

The project is a single-module, single-activity Android app.

## Requirements

- Android Studio Iguana, Hedgehog, or newer
- JDK 17
- Android SDK 35
- Min SDK 26

## Build

Open the project root in Android Studio and run the `app` configuration, or build
from the command line:

```bash
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Optional Firebase Setup

Crash reporting is inactive unless a Firebase configuration file is added.

To enable it:

1. Register `com.lvsmsmch.deckbuilder` in the Firebase console.
2. Download `google-services.json`.
3. Place it in the `app/` directory.
4. Sync and rebuild the project.

Without `google-services.json`, the app still builds and runs normally.

## Project Layout

```text
app/src/main/java/com/lvsmsmch/deckbuilder/
├── DeckBuilderApp.kt
├── MainActivity.kt
├── di/
├── domain/
│   ├── common/
│   ├── entities/
│   ├── repositories/
│   └── usecases/
├── data/
│   ├── crash/
│   ├── db/
│   ├── deckstring/
│   ├── hsjson/
│   ├── prefs/
│   ├── repository/
│   ├── rotation/
│   └── update/
└── presentation/
    └── ui/
        ├── components/
        ├── labels/
        ├── navigation/
        ├── screen/
        └── theme/
```

## Data Flow

- `HsJsonRepository` downloads and caches collectible card data by locale.
- `CardRepositoryImpl` serves card search, filters, sorting, and lookups from the
  cached card snapshot.
- `DeckRepositoryImpl` decodes and encodes Hearthstone deck codes, resolving card
  ids through the local card repository.
- `RotationRepositoryImpl` reads Standard set data from the HearthSim
  `python-hearthstone` project and caches it locally.
- `UpdateRunner` coordinates card data refreshes, rotation refreshes, and update
  notifications for app startup, manual refreshes, and the daily WorkManager job.

## Localization

UI strings are available in English and Russian:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ru/strings.xml`

Card data language is selected in the app settings and stored with DataStore.

## Tests

Run unit tests with:

```bash
./gradlew testDebugUnitTest
```

## Data Sources

- [HearthstoneJSON](https://hearthstonejson.com/)
- [HearthSim python-hearthstone](https://github.com/HearthSim/python-hearthstone)

This project is not affiliated with or endorsed by Blizzard Entertainment.
