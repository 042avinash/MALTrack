# MALTrack

MALTrack is a Kotlin + Jetpack Compose Android app for tracking anime and manga with MyAnimeList.
It blends official MAL account data with Jikan and AniList signals for discovery, list management, profile browsing, and airing tracking.

## Current Version

- `v1.2.0`

## Core Features

- MyAnimeList OAuth login
- Personalized Home experience:
  - Continue Watching / Continue Reading
  - Discovery quick links (Seasonal / Top 100)
  - Random Anime
  - Personalized anime and manga picks
- Refreshable recommendation sections
- Seasonal chart and Top 100 discovery pages
- Full Anime Details and Manga Details flows
- User List and User Manga List:
  - Status tabs
  - Search
  - Grid/List layouts
  - Manual refresh
  - Countdown support for airing entries
- Profile screen:
  - Stats
  - Favorites
  - Friends
  - MAL profile linking
- Episode notifications for watching list
- Settings for:
  - Theme
  - Startup/default page behavior
  - Title language
  - NSFW toggle
  - Home section visibility
- Image download from details pages

## v1.1.0 Highlights

- Major app foundation expansion:
  - MyAnimeList OAuth flow and authenticated account integration
  - Home, User List, and Profile as core day-to-day surfaces
- Discovery and browsing upgrades:
  - Seasonal and Top 100 exploration flows
  - Random anime discovery and refreshed recommendation sections
- Details and list-management baseline:
  - Rich Anime/Manga detail pages
  - My List add/edit/delete and progress tracking workflows
- Airing and progress intelligence:
  - Airing schedule awareness
  - Episode progress tracking for currently airing shows
- Notifications and settings system:
  - Episode notification worker pipeline
  - App-level preferences for theme, language, NSFW, startup defaults, and visibility toggles
- Performance and architecture groundwork:
  - Repository + caching improvements for repeated navigation and data reuse
  - Better state persistence and session continuity

## v1.2.0 Highlights

- Unified details-page card system:
  - Recommendations and Related sections now use fixed grid cards
  - Members and MAL score shown consistently
  - User list status icon and user score shown on cards (with metadata fallback)
- Related cards now include relation-type chips (rounded rectangular labels)
- Manga Details brought closer to Anime Details UX:
  - MAL quick-open button
  - My List status/update parity
  - Updated related/recommendation cards and navigation
- Home/User List card presentation refinements:
  - More consistent poster sizing and alignment
  - Improved two-line title consistency
  - Cleaner status/score placement
  - Better shadow/overlay readability in grid cards
- Profile page polish:
  - Better action labeling
  - Cleaner friend row presentation
  - Favorites updated to consistent card patterns
  - Improved tab styling and behavior
- Loading-state improvements:
  - Skeleton shimmer loaders added across major pages (home, profile, settings, seasonal/top, user lists)
- Navigation stability improvements:
  - Better back-stack handling
  - Reduced duplicate destination instances
  - Exit confirmation on root destinations
- User-list search behavior improvements:
  - Search can be preserved while moving between status tabs

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Hilt (DI)
- Retrofit + Kotlinx Serialization
- Coroutines + StateFlow
- DataStore (preferences/tokens)
- WorkManager (notifications/background work)

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

## Build

- Open in Android Studio
- Sync Gradle
- Build/run `app` module
