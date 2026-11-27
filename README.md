# MALTrack

MALTrack is a Kotlin and Jetpack Compose Android app for tracking anime and manga with MyAnimeList.
It combines MyAnimeList account and catalogue data with community metadata from Tenrai, plus AnimeSchedule airing data.

![MALTrack Cover](assets/cover.png)

## Current Version

- `v1.5.1`

### v1.5.1 Highlights

- **Profile**:
  - Added birthday and MAL supporter status to the signed-in profile.
  - Added anime and manga favorites through Jikan Edge and refined the profile header layout.
- **User List Search**:
  - Redesigned the no-results page with centered search actions styled as buttons.
- **Settings**:
  - Moved Page Style to the top of Appearance settings.
  - Kept the selected Anime/Manga sorting scope visible when reopening the sorting settings.
- **Release Alignment**:
  - App version bumped to `v1.5.1` (`versionCode 37`).

## Features

- **MyAnimeList account and lists**
  - Sign in with MAL OAuth and view your profile and anime/manga statistics.
  - Browse and edit your personal Anime and Manga lists by status: All, Watching/Reading, Completed, On Hold, Dropped, and Plan to Watch/Read.
  - Update score, episode/chapter/volume progress, dates, priority, tags, comments, and rewatch/reread details where supported; use quick progress actions from list cards.
  - Search your lists with recent-query suggestions, load additional MAL search results, and switch between Anime, Manga, or All when a search has no matches. Open a query on MAL directly from the no-results page.
  - Choose list or grid layout, sort entries, pull to refresh, and set default list sections and sorting separately for Anime and Manga.
- **Home and discovery**
  - Continue Watching and Continue Reading rows with shortcuts to your lists.
  - Recommended Anime and Manga picks based on your list activity, with independent refresh actions.
  - Browse current, upcoming, or historical seasonal charts; choose a season and year, sort/filter discovery results, and explore Top 100 Anime and Manga.
  - Open a random Anime and search MAL from Home with Anime or Manga as the selected search type; recent searches are available while searching.
  - Configure the startup page and each of six Home sections, and set independent Anime/Manga discovery sorting.
- **Anime and manga details**
  - View title information, catalogue stats, relations, and recommendations in dedicated popup lists.
  - See relation types, MAL scores, member counts, and your list status on related and recommendation cards.
  - Browse characters for Anime and Manga, Anime staff and opening/ending themes, reviews, and Anime streaming information; open the full review list when available.
  - View community statistics and sentiment cards; see the [Community Sentiment Cards and Related Info](#community-sentiment-cards-and-related-info) section for details.
  - Sentiment cards include Trending, Beloved, HiddenGem, Polarizing, High Dropoff, Disliked, High Retention, Mixed, Stalled, Mid, Broad Appeal, High Interest, Niche, Obscure, and Slowburn.
  - Anime and Manga use separate sentiment rules, and the five strongest matching cards appear with their own icons, colors, and explanations.
  - Edit your list status and progress from a title page, choose start/finish dates, and swipe through or save available images.
  - See airing information and next-episode countdowns on supported detail and list pages.
- **Your profile**
  - View your MAL profile details, birthday, gender, location, supporter status, and Anime/Manga statistics.
  - View profile signal cards, account-age milestones, Anime and Manga favorites, watched/read totals, scores, and activity summaries.
- **Episode notifications**
  - Enable or disable alerts for new episodes of Anime in your Watching list.
  - Check that alerts work with a test notification; Settings can guide you through notification permission and battery-optimization steps.
- **Settings and accessibility**
  - Choose System, Light, Dark, or AMOLED appearance, select from the built-in palettes, or create a custom accent color.
  - Set the app-wide list/grid page style and choose Romaji, English, or Japanese title display.
  - Control 18+ content visibility and whether NSFW counts or hidden-content notes appear beside list counts.
  - Configure startup, Home sections, Home search type, list sections/sorting, and discovery sorting.
  - Restore list defaults; open API credits and documentation, download the latest release, view app information, log out, or send feedback through GitHub.
  - Use pull-to-refresh, loading placeholders, vertical and horizontal interactive scrollbars; scrollbar gestures yield to cross-axis scrolling and edge-back gestures.

## Data Sources and API Credits

MALTrack uses these services for the features listed below. Their documentation is also available under **Settings → Support & About → API Credits & Docs**.

| Service | MALTrack usage | Documentation |
| --- | --- | --- |
| [MyAnimeList API v2](https://myanimelist.net/apiconfig/references/api/v2) | OAuth account access, anime and manga catalogues, personal lists, search, rankings, suggestions, and list updates. | [API v2 reference](https://myanimelist.net/apiconfig/references/api/v2) |
| [Jikan Edge](https://jikan.lucashdo.com/) | Anime and manga favorites shown on your profile. | [API documentation](https://jikan.lucashdo.com/docs) |
| [Tenrai](https://tenrai.org/) | Anime and manga details, characters, staff, reviews, themes, streaming and statistics, plus random Anime and publishing Manga discovery. | [API documentation](https://api.tenrai.org/documentation) |
| [AnimeSchedule](https://animeschedule.net/) | Airing schedules and next-episode countdown data used by details, lists, and episode alerts. | [API v3 documentation](https://animeschedule.net/api/v3/documentation) |

AnimeSchedule schedule data is cached briefly to limit repeated requests. Tenrai catalogue requests are throttled when the upstream service is rate-limited.

## Feedback and Releases

- [Download the latest Android release](https://github.com/042avinash/MALTrack/releases/latest).
- [Browse the MALTrack repository](https://github.com/042avinash/MALTrack).
- Send feedback from the app’s Settings page to open a prefilled GitHub issue. GitHub issues are public and require a GitHub account to submit.

## Community Sentiment Cards and Related Info

MALTrack analyzes MyAnimeList score distribution, audience sentiment, completion and drop-off behavior, community interest, niche popularity, and long-term reception. Anime and manga use separately tuned rules. Cards are ranked by priority, contradictory matches are suppressed, and up to five of the strongest cards are shown for a title.

| Card | Meaning |
| --- | --- |
| **Trending** | Strong current watcher activity |
| **Beloved** | Very positive audience reception |
| **HiddenGem** | Strong praise for a less widely followed title |
| **Polarizing** | Viewers are divided in their reactions |
| **High Dropoff** | Many viewers stop before completion |
| **Disliked** | Noticeably negative overall reception |
| **High Retention** | Viewers consistently finish the title |
| **Mixed** | No strong consensus in audience scoring |
| **Stalled** | Often placed on hold |
| **Mid** | Mostly average reception |
| **Broad Appeal** | Widely approachable with little negativity |
| **High Interest** | Many users plan to watch or read it |
| **Niche** | Strong appreciation from a specific audience |
| **Obscure** | Low visibility among MAL users |
| **Slowburn** | Appreciation builds gradually over time |

Cards use distinct icons and colors with explanatory descriptions. The anime details page also includes a score distribution chart with its scored-by count and refresh action, plus community-stat signal cards such as **Trending Now**, **Highly Completed**, **Highly Dropped**, **On Hold Risk**, and **Planned by Many**. Related and recommendation cards show title relationships, MAL scores, member counts, and your list status when available.

Manga sentiment has dedicated thresholds. Current formula notes include:

- **Beloved**: `highScoreShare >= 55` and `lowScoreShare <= 5`
- **HiddenGem**: `highScoreShare >= 38`, `lowScoreShare <= 8`, and `members < 35000`
- **Polarizing**: `highScoreShare >= 25`, `score5to8Share >= 45`, and `lowScoreShare >= 10`
- **High Dropoff**: `dropRate >= 10` and `dropRate >= completionRate * 0.25`
- **Mixed**: `largestBucketShare < 45`, `score5to6Share >= 12`, and `lowScoreShare >= 5`
- **Slowburn**: `highScoreShare >= 25`, `highScoreShare < 50`, `dropRate <= 5`, `lowScoreShare <= 5`, and `members > 15000`

The `HiddenGem` card suppresses `Beloved` for manga to avoid showing contradictory labels. The full version history below keeps the original sentiment implementation notes and formulas.

## Previous Versions

<details>
<summary>Show previous version changelogs</summary>

## v1.5.0 Highlights

- **Related and Recommendation Lists**:
  - Matched anime and manga popup list rows with the app's poster-and-details card style.
  - Added MAL relation badges to related manga in both list and grid views.
  - Tightened poster-to-details and row spacing, capped list titles to one line, and included user list status with score and MAL metadata.
- **Discovery and User List Layouts**:
  - Matched list-view spacing and side margins across anime and manga user lists, search results, Top 100 pages, and seasonal charts.
  - Updated discovery and user list loading shimmers to reflect the list card spacing and details tile.
- **Appearance and Settings**:
  - Added distinct MAL, Sakura, Fern, Ember, Midnight, Parchment, Lagoon, and Graphite palettes.
  - Added a link icon beside the latest version download action.
- **Release Alignment**:
  - App version bumped to `v1.5.0` (`versionCode 36`).

## v1.4.10 Highlights

- **List and Discovery Cards**:
  - Reworked Anime list and discovery details for clearer score, status, progress, and airing information.
  - Matched Discovery grid tile sizing and spacing to My List grids, and updated the My List loading shimmer to match its cards.
  - Displayed media types with readable labels, including `TV Special`.
- **Navigation and Appearance**:
  - Fixed Continue Watching and Continue Reading buttons to open the Watching and Reading tabs respectively.
  - Improved Settings back navigation and exit dialog text contrast.
- **Release Alignment**:
  - App version bumped to `v1.4.10` (`versionCode 35`).

## v1.4.9 Highlights

- **List and Discovery Controls**:
  - Added temporary page-style and sorting controls to anime and manga lists, with permanent defaults managed in Settings.
  - Added sorting controls to discovery pages, with persistent discovery sort defaults in Settings.
  - Preserved the selected My List tab when navigating away and returning.
- **MAL Search**:
  - Added a Load More flow to search across additional MAL result pages, up to 100 results.
- **Appearance and Profiles**:
  - Added seasonal palettes and a custom accent color picker with visible hue, saturation, and brightness gradients.
  - Refined the profile header and made its anime/manga selector match the home page selector.
  - Made the bottom navigation use a consistent profile icon and highlight it on the user's own profile.
- **Release Alignment**:
  - App version bumped to `v1.4.9` (`versionCode 34`).

## v1.4.8 Highlights

- **Settings Improvements**:
  - Organized preferences into dedicated Appearance, Startup & Browsing, Notifications, and Content pages, with account and support actions on the Settings landing page.
  - Added selectable color palettes, a system/light/dark/AMOLED mode selector, live settings summaries, and a compact app release link.
  - Added API credits and documentation links, GitHub issue feedback, and a persisted option to show NSFW list-count details.
- **List Count Clarity**:
  - Added the NSFW entry count when 18+ content is enabled, or a hidden-content note when it is disabled; this detail can be hidden in Content settings.
  - Separated cached Anime and Manga lists by the NSFW setting so counts and entries follow the selected filter.
- **Scrolling and Layout**:
  - Updated vertical and horizontal scrollbar gestures to wait for directional intent, reducing accidental activation during back swipes and cross-axis scrolling.
  - Stabilized Anime Characters, Voice Actors, and Manga Characters carousel card heights with consistent name and role truncation.
- **Release Alignment**:
  - App version bumped to `v1.4.8` (`versionCode 33`).

## v1.4.7 Highlights

- **Scrolling and Navigation**:
  - Added interactive, fixed-size scrollbars throughout the app, with a press animation and tap/drag seeking.
  - Added horizontal scrollbars for anime and manga detail sections and profile badges and favorites.
  - Preserved the selected manga list when opening a manga detail page and returning with Back.
- **Details Page Stability**:
  - Fixed a number-formatting crash in community badge descriptions on anime and manga details pages.
- **Release Alignment**:
  - App version bumped to `v1.4.7` (`versionCode 32`).

## v1.4.6 Highlights

- **Airing Schedule Reliability**:
  - Resolved rate-limiting issues caused by concurrent timetable requests from the AnimeSchedule API during tab preloading.
  - Implemented a synchronized request pattern with a 15-minute cache to ensure all list tabs reuse a single timetable fetch, improving stability for countdown timers.

## v1.4.5 Highlights

- **Profile and Airing Reliability**:
  - Removed the signed-in profile page's Jikan dependency to prevent profile 504 failures; profile data and manga statistics now come from MyAnimeList.
  - Replaced AniList airing lookups with authenticated AnimeSchedule data, including exact aired-episode counts, detail-page countdowns, and list-tile countdowns.
  - Restored reliable episode-notification checks through the shared AnimeSchedule schedule path.
- **Airing List UX**:
  - Personal Anime List tabs now prefetch airing metadata for their initial entries and show compact next-episode countdowns alongside watched, aired, and total episode counts.
- **Release Alignment**:
  - App version bumped to `v1.4.5` (`versionCode 30`).

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

## v1.3.8 Highlights

- Improved User Anime List search robustness:
  - Added normalized matching that ignores punctuation and whitespace for more reliable hits
  - Added support for multi-word query fragments matching across different parts of the title
- App branding update:
  - Updated logo assets and refreshed login screen presentation
- Version alignment:
  - App version bumped to `1.3.8` (`versionCode 23`)

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

## v1.3.6 Highlights

- Anime Details My List status accuracy improvement:
  - Added lightweight `my_list_status` refresh on details load
  - My List status/progress/date box now updates independently without forcing a full details-page refetch
- Release hardening and compatibility updates:
  - Application ID migrated to `com.maltrack.app`
  - Removed legacy `WRITE_EXTERNAL_STORAGE` manifest permission
  - Enabled release code/resource shrinking (`minify` + `shrinkResources`)
  - Added build-type backup policy via manifest placeholders (`allowBackup=true` debug, `allowBackup=false` release)

## v1.3.5 Highlights

- Anime Details `Staff` section update:
  - Renamed section heading from `People` to `Staff` for accuracy
  - Staff data now loads on-demand instead of automatic background fetch
  - Added full-width `Show Staff Credits` action
  - Added full-width retry action (`Retry Staff Load`) when initial staff fetch returns empty/fails
- Details-page load overhead reduction:
  - Removed staff from automatic supplementary fetch fanout to reduce initial network pressure

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

## v1.3.2 Highlights

- Branding refresh:
  - Updated launcher icon source to the new high-resolution brand mark and centered it for adaptive icon rendering
  - Improved lockscreen/status-bar notification visibility with a thicker, simplified small notification glyph
- Splash screen update:
  - Added dedicated launch splash theme with branded splash background image

## v1.3.1 Highlights

- Home top-bar visual cleanup:
  - Removed translucent bleed-through artifact in the Home search toolbar by increasing container opacity
- User List top-bar parity:
  - Applied the same toolbar opacity fix to User Anime/Manga list top bars for consistent appearance
- Home discovery card polish:
  - Increased discovery card container opacity (`Seasonal`, `Top 100`, `Random Anime`) to remove inner rectangular bleed-through artifacts

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

## v1.2.10 Highlights

- User-list countdown persistence fix:
  - Restored missing airing metadata when returning to cached user-list pages (no manual refresh needed)
- AniList airing data reliability update:
  - Added bounded-time AniList enrichment fetches with cache-aware reuse to reduce long blocking waits
  - Improved countdown stability across root navigation (Home/Profile/User List transitions)

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

## v1.2.6 Highlights

- Stronger force refresh behavior:
  - User Anime/Manga list refresh now cancels in-flight background backfill jobs before clearing caches
  - Home now supports explicit force refresh from the toolbar and bypasses cached home payload
- Faster fallback behavior under slow network:
  - User Anime/Manga list loads now use a 1.5s soft-timeout strategy
  - If first response is slow, UI unblocks and continues loading in background instead of feeling stuck
- List loading path optimization:
  - Faster first-page render with background full-list completion for smoother initial open

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

## Versioning Note

- `v1.2.4` was intentionally skipped on GitHub.
- Reason: its changes were rolled into `v1.2.5` so the release tag, app `versionName`, and uploaded APK all stay aligned.

## v1.2.3 Highlights

- Details page network-load optimization:
  - Reviews switched to manual "Load Reviews" on-demand fetch
  - Recommendations switched to manual "Load Recommendations" on-demand fetch
  - Removed automatic prefetch triggers for both reviews and recommendations
- UI consistency polish for load actions:
  - Refined "Load Reviews" and "Load Recommendations" button typography/alignment
- Home resilience fallback:
  - Added soft-timeout behavior to avoid prolonged blank/blocked loading states

## v1.2.2 Highlights

- Top 100 back-navigation fix:
  - Prevented stale async responses from reopening Top lists after returning home
- Top 100 visual header alignment:
  - Back button and title style now consistent with details-page style direction
- Additional seasonal/top UX stability polishing:
  - Better request invalidation and smoother transitions between Home and discovery views

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

</details>

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Hilt (DI)
- Retrofit + Kotlinx Serialization
- Coroutines + StateFlow
- DataStore (preferences/tokens)
- WorkManager (notifications/background work)

## Architecture

MALTrack is organized into a Compose presentation layer, data access, local storage, dependency injection, and background notification work.

- **UI and navigation**: `MainActivity` hosts app navigation. Compose screens render state and send user actions to their ViewModels.
- **ViewModels**: expose screen state with `StateFlow`, coordinate loading and edits, and call the repository or their specific services. `ProfileViewModel` also loads the signed-in profile's favorites from Jikan Edge.
- **Repository and APIs**: `AnimeRepository` coordinates MyAnimeList API v2, Tenrai, and AnimeSchedule, with shared caching and throttling for catalogue and schedule data.
- **Networking and dependency injection**: Retrofit services use Kotlinx Serialization. Hilt providers in `NetworkModule` configure clients and inject services; the MAL client adds the current bearer token from `TokenManager`.
- **Local state**: DataStore-backed `UserPreferencesManager` stores app preferences, while `TokenManager` stores OAuth tokens.
- **Background work**: WorkManager runs airing-notification checks through `AiringNotificationWorker` and its scheduler.

## Project Structure

```text
app/src/main/java/com/example/myapplication/
|- data/
|  |- local/         # DataStore preferences, OAuth tokens, and title helpers
|  |- model/         # Anime, manga, profile, and list data models
|  |- remote/        # Retrofit services and remote response models
|  |- repository/    # Catalogue, list, profile-stat, and schedule data access
|- di/               # Hilt network and app dependency providers
|- notifications/    # WorkManager airing notification worker and scheduler
|- ui/
|  |- theme/         # Color palettes, theme setup, and typography
|  |- *Screen.kt     # Compose screens for Home, lists, details, profile, and settings
|  |- *ViewModel.kt  # Screen state and UI actions
|  |- Scrollbars.kt  # Shared interactive scrollbars
|- MainActivity.kt   # App startup and navigation shell
|- MalApplication.kt # Hilt application setup
```

## Build

- Open in Android Studio
- Sync Gradle
- Build/run `app` module
