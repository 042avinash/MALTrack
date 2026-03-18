# MALTrack

MALTrack is a Kotlin Android app for tracking anime and manga with MyAnimeList.  
It combines MyAnimeList account features with Jikan and AniList data for discovery, list management, airing updates, and profile browsing.

## Features

- MyAnimeList OAuth login
- Personalized Home sections:
    - Continue Watching
    - Continue Reading
    - Discovery quick actions (Seasonal / Top 100)
    - Random Anime
    - Personalized Anime Picks / Manga Picks
- Refreshable recommendation rows (`Refresh Picks`)
- Seasonal anime chart
- Top 100 Anime / Top 100 Manga discovery
- Anime and manga detail pages
- User lists with tabs, search, filters, grid/list layouts, and manual refresh
- Profile page with favorites, friends, and MAL profile linking
- Airing countdowns and released-episode progress for currently airing anime
- Episode notifications for anime in your watching list
- Test notification option in Settings
- Persistent settings for theme, startup page, title language, NSFW toggle, defaults, and Home section visibility
- Image download support from anime details

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
