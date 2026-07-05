# PocketTV — Personal IPTV Player for Android

A phone/tablet IPTV player built with Kotlin + Jetpack Compose.
Supports Xtream Codes API and M3U playlist URLs.

## Features (v1)
- Xtream Codes login (server URL + username + password) with credential check
- M3U playlist URL support with group parsing
- Bottom tabs: Home (favorites + content rows), Live, Movies, Series
- Category / group filter chips on every tab
- Search (magnifier icon in the top bar filters the current tab)
- Series details: seasons + episode list, tap to play
- Favorites: LONG-PRESS any poster or channel row to add/remove
- ExoPlayer (Media3) playback with HLS + MPEG-TS support
- Settings dialog: switch live format (.m3u8 / .ts), manage playlists, refresh

## Build & install
1. Open the PocketTV folder in Android Studio (Hedgehog or newer).
2. Let Gradle sync (first sync downloads dependencies — needs internet).
3. Plug in your phone with USB debugging on, press Run. Or Build > Build APK(s)
   and sideload app/build/outputs/apk/debug/app-debug.apk.

## Tips
- If live channels buffer or fail: Settings > switch to MPEG-TS (.ts).
- Long-press = favorite. Tap = play (movies/live) or open (series).
- Multiple playlists: add more via Settings > "Add another playlist",
  then switch between them in Settings.
- Rename the app: change android:label in app/src/main/AndroidManifest.xml
  and applicationId in app/build.gradle.kts.

## Roadmap ideas (v2+)
- EPG (XMLTV) guide with now/next
- Catchup / replay
- TMDB metadata (posters, ratings, plots for movies)
- Continue watching (store playback position)
- Picture-in-Picture
- Parental lock / hide categories
