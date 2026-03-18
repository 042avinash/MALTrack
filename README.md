# MALTrack

MALTrack is a Kotlin Android app for tracking anime and manga with MyAnimeList. It combines MyAnimeList account features with extra data from Jikan and AniList to make discovery, list management, airing info, and profile browsing feel more complete inside one mobile app.

## Features

- MyAnimeList OAuth login
- Home discovery for anime and manga
- Seasonal anime chart
- Top anime and top manga browsing
- Anime and manga detail pages
- User lists with tabs, search, filters, grid/list layouts, and manual refresh
- Profile page with favorites, friends, and MAL profile linking
- Airing countdowns and released-episode progress for currently airing anime
- Notification support for new episodes from anime in your watching list
- Persistent settings for theme, startup page, title language, NSFW toggle, and default list behavior
- Image download support from the anime details screen

## Built With

- Kotlin
- Jetpack Compose
- Navigation Compose
- Hilt
- Retrofit
- OkHttp
- DataStore
- Coil
- WorkManager

## Data Sources

- [MyAnimeList API](https://myanimelist.net/apiconfig/references/api/v2)
- [Jikan API](https://jikan.moe/)
- [AniList GraphQL API](https://anilist.gitbook.io/anilist-apiv2-docs/)

## Getting Started

### Prerequisites

- Android Studio
- Android SDK configured locally
- A MyAnimeList API client ID

### Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Add your MyAnimeList client ID where the app expects it in the network/auth configuration.
4. Sync Gradle.
5. Run the app on an emulator or Android device.

## Project Structure

```text
app/src/main/java/com/example/myapplication/
|- data/
|  |- local/         # DataStore preferences and token storage
|  |- model/         # API and app models
|  |- remote/        # Retrofit API definitions
|  |- repository/    # Repository logic
|- di/               # Hilt modules
|- notifications/    # WorkManager notification logic
|- ui/               # Compose screens and ViewModels
|- MainActivity.kt   # App navigation shell
|- MalApplication.kt # Application setup
```

## Main Screens

- Login
- Home
- My List
- Anime Details
- Manga Details
- Profile
- Settings

## Notes

- Some features rely on multiple providers because MyAnimeList does not expose every piece of information needed for the UI.
- Notification behavior depends on device settings and Android notification permission support.
- Large list and discovery requests can be rate-limited by external APIs.

## Status

This is an actively evolving Android project focused on delivering a smoother MyAnimeList experience on mobile.

