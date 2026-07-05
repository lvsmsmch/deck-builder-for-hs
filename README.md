# Deck Builder for Hearthstone

Deck Builder for Hearthstone is a native Android app for browsing Hearthstone
cards and building decks.

The app lets users search the card pool, inspect card details, filter cards by
gameplay metadata, create Standard or Wild decks, import existing deck codes,
save decks locally, and copy deck codes for sharing.

Card data and images are loaded from [HearthstoneJSON](https://hearthstonejson.com/).
Deck codes are decoded and encoded inside the app, so Blizzard API credentials
are not required.

## Main Features

- Hearthstone card browser with search and filters.
- Card detail screen with localized text and artwork.
- Standard and Wild deck builder.
- Deck code import and export.
- Local saved decks.
- App theme and card language settings.
- Background card data updates.

## Built With

- Kotlin
- Jetpack Compose
- Material 3
- Koin
- Retrofit
- OkHttp
- kotlinx.serialization
- Coil
- Room
- DataStore
- WorkManager
- Firebase Crashlytics and Analytics


## Data Sources

- [HearthstoneJSON](https://hearthstonejson.com/)
- [HearthSim python-hearthstone](https://github.com/HearthSim/python-hearthstone)

This project is not affiliated with or endorsed by Blizzard Entertainment.
