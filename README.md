# MALTrack

MALTrack is a Kotlin + Jetpack Compose Android app for tracking anime and manga with MyAnimeList.
It blends official MAL account data with Jikan and AniList signals for discovery, list management, profile browsing, and airing tracking.

## Current Version

- `v1.2.10`

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
  - Recent submitted search history (dropdown)
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
- On-demand Details extras loading:
  - Reviews load only when requested
  - Recommendations load only when requested

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

## v1.2.1 Highlights

- Seasonal and Top 100 reliability/performance fixes:
  - Reduced duplicate navigation/state races
  - Faster first render for heavy discovery pages
  - Better cancellation handling for overlapping loads
- Details page loading improvements:
  - Two-phase loading strategy to show core content faster
  - Background metadata hydration for related/recommendation cards
  - Reduced metadata request fanout to lower network pressure
- Profile and metadata request throttling:
  - Lower burst concurrency for favorite metadata lookups
  - Improved overall responsiveness when navigating between profile/discovery/details

## v1.2.2 Highlights

- Top 100 back-navigation fix:
  - Prevented stale async responses from reopening Top lists after returning home
- Top 100 visual header alignment:
  - Back button and title style now consistent with details-page style direction
- Additional seasonal/top UX stability polishing:
  - Better request invalidation and smoother transitions between Home and discovery views

## v1.2.3 Highlights

- Details page network-load optimization:
  - Reviews switched to manual "Load Reviews" on-demand fetch
  - Recommendations switched to manual "Load Recommendations" on-demand fetch
  - Removed automatic prefetch triggers for both reviews and recommendations
- UI consistency polish for load actions:
  - Refined "Load Reviews" and "Load Recommendations" button typography/alignment
- Home resilience fallback:
  - Added soft-timeout behavior to avoid prolonged blank/blocked loading states

## Versioning Note

- `v1.2.4` was intentionally skipped on GitHub.
- Reason: its changes were rolled into `v1.2.5` so the release tag, app `versionName`, and uploaded APK all stay aligned.

## v1.2.5 Highlights

- Search and trigger cleanup:
  - Recent search history now saves only on explicit submit (not typing-triggered calls)
  - Added recent-search dropdown suggestions for Home and User List search
  - Preserved submitted search while switching User List status tabs
- Discovery/background load reductions:
  - Removed delayed/background expansion fetch patterns in Seasonal/Top flows
  - Reduced hidden offscreen list preloading to lower unnecessary work
- General responsiveness improvements from fewer overlapping data triggers
- Version alignment and release consistency:
  - App version bumped to `1.2.5` (`versionCode 8`)
  - Release APK updated to match tagged release `v1.2.5`

## v1.2.6 Highlights

- Stronger force refresh behavior:
  - User Anime/Manga list refresh now cancels in-flight background backfill jobs before clearing caches
  - Home now supports explicit force refresh from the toolbar and bypasses cached home payload
- Faster fallback behavior under slow network:
  - User Anime/Manga list loads now use a 1.5s soft-timeout strategy
  - If first response is slow, UI unblocks and continues loading in background instead of feeling stuck
- List loading path optimization:
  - Faster first-page render with background full-list completion for smoother initial open

## v1.2.7 Highlights

- Startup load pressure reduction:
  - Removed eager prefetch fanout at login so only the selected startup surface initializes
  - Profile data is no longer prefetched globally at startup
- User list first-load optimization:
  - Reduced initial User Anime/Manga list page size to lightweight first fetches (`limit=40`)
  - Removed immediate full backfill expansion on initial list open
- Home startup/network cleanup:
  - Added duplicate-load guarding for Home fetch calls
  - Reduced redundant refresh triggers from preference flow churn
  - Switched manga recommendation startup path to fallback source to avoid repeated MAL `manga/suggestions` 404 noise

## v1.2.8 Highlights

- User List search behavior fix:
  - Clearing search via `X` now properly clears active filtering state
  - Returning from search no longer leaves stale list filters applied
- Profile navigation polish:
  - Added proper back navigation on other-user profile pages
- Seasonal UI/label fixes:
  - Home seasonal subtitle stabilized to current season/year label behavior
  - Seasonal picker year input now constrained to `1917..(current year + 1)`
  - Added `Jump to Current` seasonal action and aligned its pill styling/height with season switcher controls
- Details-page relation/recommendation UX refresh:
  - Anime Details now uses 2 pills (`Related Anime`, `Recommendations`) instead of inline rows
  - Manga Details now uses 2 pills (`Related Manga`, `Recommendations`) with matching behavior
  - Each pill opens a dedicated popup page with grid cards and consistent card metadata styling
- Metadata hydration coverage expansion:
  - Increased related/recommendation card metadata fetch cap from `8` to `30` for better `Members` and `MAL` score population

## v1.2.9 Highlights

- Anime Details relation/recommendation flow update:
  - Replaced inline rows with 2 pills (`Related Anime`, `Recommendations`)
  - Added dedicated popup grid pages for both sections
- Manga Details relation/recommendation flow parity:
  - Added matching 2-pill flow (`Related Manga`, `Recommendations`)
  - Added dedicated popup grid pages with consistent card metadata presentation
- Card metadata population improvement:
  - Increased details metadata hydration cap (`8 -> 30`) to reduce `N/A` stats on deeper cards
- User list countdown stability fix:
  - Unified list/grid countdown formatter and refreshed timer behavior for airing entries
- AniList airing fetch resilience improvements:
  - Added soft-timeout guard for AniList GraphQL airing lookups to avoid long UI stalls
  - Added batched AniList ID fetches for more stable response behavior under load
  - Added short-lived airing metadata cache (`airingAt` source data) to reduce repeat network latency

## v1.2.10 Highlights

- User-list countdown persistence fix:
  - Restored missing airing metadata when returning to cached user-list pages (no manual refresh needed)
- AniList airing data reliability update:
  - Added bounded-time AniList enrichment fetches with cache-aware reuse to reduce long blocking waits
  - Improved countdown stability across root navigation (Home/Profile/User List transitions)

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
