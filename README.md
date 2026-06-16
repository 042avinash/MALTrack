# MALTrack

MALTrack is a Kotlin + Jetpack Compose Android app for tracking anime and manga with MyAnimeList.
It blends official MAL account data with Jikan and AniList signals for discovery, list management, profile browsing, and airing tracking.

![MALTrack Cover](assets/cover.png)

## Current Version

- `v1.4.4`

## v1.4.4 Highlights

- **Startup UX Polish**:
  - **Eliminated Login Flash**: Introduced a silent "boot gate" that handles authentication checks before showing any UI, removing the brief flash of the login screen for authenticated users.
  - **Reliable Cold-Start Notifications**: Fixed an issue where tapping a notification while the app was closed could leave the user stranded on the login screen; the app now properly preserves and navigates to the notification target after background authentication completes.
  - **Smarter Initial Routing**: Improved the startup flow to wait for authentication resolution before deciding between the Home, Notification, or Login screens.
- **Release Alignment**:
  - App version bumped to `v1.4.4` (`versionCode 29`).

## v1.4.3 Highlights

- **Notification System Upgrades**:
  - **Airing Page Mapping**: Tapping an airing notification now navigates directly to the specific anime's details page.
  - **Title Language Localization**: Notifications now respect the app's "Title Language" setting (Romaji, English, or Japanese).
  - **Battery Optimization Intelligence**: Added a proactive check for battery exemption when enabling notifications, with a dedicated warning dialog (Retry/Cancel) to ensure background alerts remain reliable.
- **Profile & User Cards**:
  - **Formula Overhaul**: Updated criteria for "Completionist", "Archivist", and "Binge Watcher" cards to better reflect both anime and manga list activity.
  - **New Milestones**: Introduced the "Club" card with progressive title updates (1K, 2.5K, 5K) and the "Founding Member" card for accounts joined before 2007.
- **Release Alignment**:
  - App version bumped to `v1.4.3` (`versionCode 28`).

## v1.4.2 Highlights

- Profile page resilience:
  - Added cleaner error handling with stable status-based messages
  - Improved retry behavior so the current profile can be re-fetched cleanly
  - Added lighter request throttling/backoff for Jikan profile calls
  - Split profile sections so friends and favorites can refresh independently
- Profile UX improvements:
  - Added profile identity cards with colorful pill styling
  - Added dedicated profile search flow and friend-state indicators
  - Moved profile actions into a cleaner circular action cluster
- Anime/Manga details polish:
  - Expanded community stats card logic and clarified sentiment descriptions
  - Aligned review, character, staff, themes, and availability fallback handling
  - Improved on-demand loading and refresh controls for heavy detail sections
- User list/search updates:
  - Search now supports switching between anime and manga targets more smoothly
  - Search can preserve the entered keyword while changing the target source
- Release/version alignment:
  - App version bumped to `1.4.2`
  - Release APK and metadata updated for the tagged release

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

## v1.2.11 Highlights

- Seasonal chart section-order stability:
  - Fixed media-type section order to a consistent sequence
  - Sorting now affects items within each section, not section position
- Seasonal collapse/expand UX and performance:
  - Collapsed sections now remove items from composition to prevent large empty gaps
  - Kept header-only visibility and added lightweight chevron rotation animation
- User-list long-tab completeness fix:
  - Restored reliable full-list backfill after fast first-page loads (Anime + Manga)
  - Prevented abrupt cutoff where long tabs showed only a few entries
- Profile favorites metadata coverage:
  - Increased favorite metadata fetch cap so later cards also populate `Members` and `MAL` score
  - Added better spacing between Favorite Characters and Favorite People sections

## v1.3.0 Highlights

- Icon system refresh:
  - Replaced launcher icon styling with new `square-m` icon direction
  - Unified launcher icon appearance across light/dark modes (single black-on-white style)
  - Added dedicated notification small icon resource to avoid blob-like status-bar rendering
- Notification icon correctness:
  - Switched episode notification small icon from launcher mipmap to a proper status-bar drawable
- Settings UX update:
  - Moved app branding/version block into a dedicated `App Info` settings item
  - Added an `App Info` popup dialog showing logo, app version, and creator details

## v1.3.1 Highlights

- Home top-bar visual cleanup:
  - Removed translucent bleed-through artifact in the Home search toolbar by increasing container opacity
- User List top-bar parity:
  - Applied the same toolbar opacity fix to User Anime/Manga list top bars for consistent appearance
- Home discovery card polish:
  - Increased discovery card container opacity (`Seasonal`, `Top 100`, `Random Anime`) to remove inner rectangular bleed-through artifacts

## v1.3.2 Highlights

- Branding refresh:
  - Updated launcher icon source to the new high-resolution brand mark and centered it for adaptive icon rendering
  - Improved lockscreen/status-bar notification visibility with a thicker, simplified small notification glyph
- Splash screen update:
  - Added dedicated launch splash theme with branded splash background image

## v1.3.3 Highlights

- User List refresh behavior:
  - Reintroduced pull-to-refresh on User Anime/Manga lists with a lightweight trigger flow
  - Refresh now targets only the currently visible status tab to reduce unnecessary reload work
- Related popup metadata polish:
  - Added clearer relation-type highlighting under related anime cards for better scanability
- Pull-to-refresh rollout:
  - Added pull-to-refresh support across major surfaces (Profile/Home/Seasonal/Top lists/Details/User Lists) and aligned stale-data refresh behavior improvements
  - Wired `MainActivity` to switch from splash theme to app theme at startup
- Settings/App info assets:
  - Updated in-app branding assets used in settings/app info surfaces
- README refresh:
  - Added new repository cover image and updated current version tracking

## v1.3.4 Highlights

- Home label clarity updates:
  - `From your MAL list` changed to `From your MAL` for Continue Watching/Reading
  - `Personalized Anime Picks` changed to `Recommended Anime`
  - `Personalized Manga Picks` changed to `Recommended Manga`
- Anime Details updates:
  - Added `People` section sourced from anime staff data
  - Added copy affordance for titles in Information section (English/Japanese/Romaji) via long-press and copy icon
  - Added currently-airing info chip behavior for unknown schedule data (`Aired: ? | Next Ep ?: ?`)
  - Added plan-to-watch progress behavior: changing episodes from `0 -> >0` auto-sets status to `watching` and fills start date only when empty
- Profile page updates:
  - Favorite anime cards now show user status icon and user score overlay
  - Added fallback placeholder avatar when profile/friend image is missing
  - Filtered profile external links to avoid showing RSS/feed-style entries as normal links
- Edit List Status numeric-input fix:
  - Episode/score-style counter text fields now support full backspace clear
  - Empty numeric input is now treated as `0` instead of getting stuck

## v1.3.5 Highlights

- Anime Details `Staff` section update:
  - Renamed section heading from `People` to `Staff` for accuracy
  - Staff data now loads on-demand instead of automatic background fetch
  - Added full-width `Show Staff Credits` action
  - Added full-width retry action (`Retry Staff Load`) when initial staff fetch returns empty/fails
- Details-page load overhead reduction:
  - Removed staff from automatic supplementary fetch fanout to reduce initial network pressure

## v1.3.6 Highlights

- Anime Details My List status accuracy improvement:
  - Added lightweight `my_list_status` refresh on details load
  - My List status/progress/date box now updates independently without forcing a full details-page refetch
- Release hardening and compatibility updates:
  - Application ID migrated to `com.maltrack.app`
  - Removed legacy `WRITE_EXTERNAL_STORAGE` manifest permission
  - Enabled release code/resource shrinking (`minify` + `shrinkResources`)
  - Added build-type backup policy via manifest placeholders (`allowBackup=true` debug, `allowBackup=false` release)

## v1.3.7 Highlights

- Anime Details Community Stats refresh:
  - Added a dedicated refresh action on the Community Stats heading
  - Refresh updates only stats-related data instead of reloading the whole details page
- Community Stats signal cards:
  - Added priority-based stat pills such as `Trending Now`, `Highly Completed`, `Highly Dropped`, `On Hold Risk`, and `Planned by Many`
  - Updated pill logic with more selective thresholds and natural explanatory popups
- Anime Details score distribution:
  - Added Jikan score distribution fetch
  - Added score distribution chart with `Scored by` count integrated into the chart header

## v1.3.8 Highlights

- Improved User Anime List search robustness:
  - Added normalized matching that ignores punctuation and whitespace for more reliable hits
  - Added support for multi-word query fragments matching across different parts of the title
- App branding update:
  - Updated logo assets and refreshed login screen presentation
- Version alignment:
  - App version bumped to `1.3.8` (`versionCode 23`)

## Version 1.3.9

### Community Sentiment Cards

Added a new sentiment and engagement card system powered by live MyAnimeList statistics.
These cards analyze audience behavior, score distribution, completion trends, and popularity patterns to surface how anime are perceived by the community beyond a single aggregate score.

Only the top 5 qualifying cards render for each anime.

| Card               | Meaning                                                            |
| ------------------ | ------------------------------------------------------------------ |
| **Trending**       | Currently seeing strong active watcher activity                    |
| **Beloved**        | Extremely high positive reception from viewers                     |
| **HiddenGem**      | Strong praise despite a smaller audience                           |
| **Polarizing**     | Viewers react very differently to the anime                        |
| **High Dropoff**   | Many viewers stop watching before completion                       |
| **Disliked**       | Noticeably negative overall reception                              |
| **High Retention** | Viewers consistently finish the anime                              |
| **Mixed**          | No strong consensus in audience scoring                            |
| **Stalled**        | Frequently placed on hold by viewers                               |
| **Mid**            | Mostly average or middle-range reception                           |
| **Broad Appeal**   | Widely approachable with very low negativity                       |
| **High Interest**  | Large number of users plan to watch it                             |
| **Niche**          | Appreciated strongly by a more specific audience                   |
| **Obscure**        | Very low visibility within the wider community                     |
| **Slowburn**       | Gradually appreciated over time rather than through immediate hype |

### Sentiment Card Improvements

* Added distinct icons and color identities for every card
* Reworked tooltip descriptions to better reflect audience sentiment
* Added support for behavioral and pacing-oriented tags like:

  * `Slowburn`
  * `High Dropoff`
  * `High Retention`
  * `Stalled`
* Improved detection logic for divisive and niche anime
* Added runtime-aware logic for Slowburn classification
* Refined rendering priority so only the most relevant cards appear
* Added support for mixed reception and audience fragmentation detection

### UI / UX

* Redesigned card naming for cleaner chip-style presentation
* Improved readability and visual distinction between positive, negative, and behavioral cards
* Added unique iconography for easier scanning
* Limited rendered cards to the top 5 matches to avoid visual clutter

## Version 1.4.0

### Community Sentiment System

Added a full community sentiment and engagement analysis system for both anime and manga using live MyAnimeList statistics.

The system now analyzes:

* score distribution
* audience sentiment
* completion behavior
* dropoff trends
* community interest
* niche popularity
* long-term reception patterns

### Added Sentiment Cards

* Trending
* Beloved
* HiddenGem
* Polarizing
* High Dropoff
* Disliked
* High Retention
* Mixed
* Stalled
* Mid
* Broad Appeal
* High Interest
* Niche
* Obscure
* Slowburn

### Sentiment Logic Improvements

* Added suppression rules to avoid contradictory card combinations
* Added priority-based rendering system
* Limited rendering to the top 5 strongest matching cards
* Added separate tuning logic for anime and manga statistics
* Added runtime-aware Slowburn detection
* Improved divisive and niche detection behavior

### UI / UX Improvements

* Added unique icons and colors for all sentiment cards
* Added detailed tooltip descriptions for every card
* Improved readability and visual distinction between positive, mixed, negative, and behavioral cards
* Added persistent refresh support even when community stats fail to load

### Search Improvements

Improved empty-state behavior inside user lists.

When no results are found, users can now:

* switch search scope to All
* search directly on MAL from inside the app
* jump between anime and manga search contexts while keeping the same query

### Technical Notes

* Anime and manga now use separately calibrated sentiment thresholds
* Suppression is applied before priority sorting and rendering
* Only the top 5 qualifying cards render at once to reduce visual clutter

## Version 1.4.1

### Home Search Preference

* Added a new Settings option for default Home search type (`Anime`/`Manga`)
* Home search media toggle now initializes from this saved preference
* Changing the Home toggle now also updates the saved default

### Details & Profile Reliability

* Added safer Jikan profile response handling for missing `data` payload cases
* Added friendlier profile error messaging for rate-limit (`429`), server (`5xx`), and not-found (`404`) scenarios

### Edit List Status (Anime)

* In Edit List Status, setting watched episodes equal to total episodes now auto-triggers completed flow:
  * status becomes `completed`
  * finish date is auto-filled when empty

### Picture Viewer UX

* Anime Details and Manga Details picture dialogs now support swipe navigation between available images
* Download action remains available for the currently visible image

### Manga Sentiment Formula Updates

* Updated manga-only sentiment thresholds:
  * `Beloved`: `highScoreShare >= 55` and `lowScoreShare <= 5`
  * `HiddenGem`: `highScoreShare >= 38` and `lowScoreShare <= 8` and `members < 35000`
  * `Polarizing`: `highScoreShare >= 25` and `score5to8Share >= 45` and `lowScoreShare >= 10`
  * `High Dropoff`: `dropRate >= 10` and `dropRate >= completionRate * 0.25`
  * `Mixed`: `largestBucketShare < 45` and `score5to6Share >= 12` and `lowScoreShare >= 5`
  * `Slowburn`: `highScoreShare >= 25` and `highScoreShare < 50` and `dropRate <= 5` and `lowScoreShare <= 5` and `members > 15000`
* Added suppression rule: `HiddenGem` suppresses `Beloved`



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
