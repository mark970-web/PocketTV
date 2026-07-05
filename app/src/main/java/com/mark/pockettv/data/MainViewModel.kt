package com.mark.pockettv.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    var playlists by mutableStateOf(prefs.playlists)
        private set
    var active by mutableStateOf(prefs.playlists.find { it.id == prefs.activePlaylistId })
        private set
    var favorites by mutableStateOf(prefs.favorites)
        private set

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    // Xtream content
    var liveCategories by mutableStateOf<List<XtreamCategory>>(emptyList())
        private set
    var liveStreams by mutableStateOf<List<LiveStream>>(emptyList())
        private set
    var vodCategories by mutableStateOf<List<XtreamCategory>>(emptyList())
        private set
    var vodStreams by mutableStateOf<List<VodStream>>(emptyList())
        private set
    var seriesCategories by mutableStateOf<List<XtreamCategory>>(emptyList())
        private set
    var series by mutableStateOf<List<SeriesItem>>(emptyList())
        private set

    // M3U content
    var m3uChannels by mutableStateOf<List<M3uChannel>>(emptyList())
        private set

    private var api: XtreamApi? = null

    var liveFormat: String
        get() = prefs.liveFormat
        set(value) {
            prefs.liveFormat = value
        }

    init {
        active?.let { loadContent(it) }
    }

    // ---------- Playlist management ----------

    fun addXtreamPlaylist(
        name: String,
        host: String,
        user: String,
        pass: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val cleanHost = host.trim().trimEnd('/')
                val testApi = ApiFactory.create(cleanHost)
                val response = testApi.login(user.trim(), pass.trim())
                if (response.userInfo == null) throw IllegalStateException("Invalid credentials")

                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name.ifBlank { cleanHost.removePrefix("http://").removePrefix("https://") },
                    type = "xtream",
                    host = cleanHost,
                    username = user.trim(),
                    password = pass.trim()
                )
                savePlaylist(playlist)
                setActive(playlist)
                onResult(true)
            } catch (e: Exception) {
                error = "Login failed: ${e.message ?: "unknown error"}"
                onResult(false)
            } finally {
                loading = false
            }
        }
    }

    fun addM3uPlaylist(name: String, url: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val text = ApiFactory.fetchText(url)
                val parsed = M3uParser.parse(text)
                if (parsed.isEmpty()) throw IllegalStateException("No channels found in playlist")

                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name.ifBlank { "M3U Playlist" },
                    type = "m3u",
                    m3uUrl = url.trim()
                )
                savePlaylist(playlist)
                setActive(playlist)
                onResult(true)
            } catch (e: Exception) {
                error = "Could not load playlist: ${e.message ?: "unknown error"}"
                onResult(false)
            } finally {
                loading = false
            }
        }
    }

    private fun savePlaylist(playlist: Playlist) {
        val updated = prefs.playlists + playlist
        prefs.playlists = updated
        playlists = updated
    }

    fun deletePlaylist(playlist: Playlist) {
        val updated = prefs.playlists.filterNot { it.id == playlist.id }
        prefs.playlists = updated
        playlists = updated
        if (active?.id == playlist.id) {
            val next = updated.firstOrNull()
            if (next != null) setActive(next) else clearActive()
        }
    }

    fun setActive(playlist: Playlist) {
        prefs.activePlaylistId = playlist.id
        active = playlist
        loadContent(playlist)
    }

    private fun clearActive() {
        prefs.activePlaylistId = null
        active = null
        liveCategories = emptyList(); liveStreams = emptyList()
        vodCategories = emptyList(); vodStreams = emptyList()
        seriesCategories = emptyList(); series = emptyList()
        m3uChannels = emptyList()
    }

    // ---------- Content loading ----------

    fun loadContent(playlist: Playlist) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                if (playlist.type == "xtream") {
                    val a = ApiFactory.create(playlist.host)
                    api = a
                    val u = playlist.username
                    val p = playlist.password
                    liveCategories = runCatching { a.liveCategories(u, p) }.getOrDefault(emptyList())
                    liveStreams = runCatching { a.liveStreams(u, p) }.getOrDefault(emptyList())
                    vodCategories = runCatching { a.vodCategories(u, p) }.getOrDefault(emptyList())
                    vodStreams = runCatching { a.vodStreams(u, p) }.getOrDefault(emptyList())
                    seriesCategories = runCatching { a.seriesCategories(u, p) }.getOrDefault(emptyList())
                    series = runCatching { a.seriesList(u, p) }.getOrDefault(emptyList())
                    m3uChannels = emptyList()
                } else {
                    val text = ApiFactory.fetchText(playlist.m3uUrl)
                    m3uChannels = M3uParser.parse(text)
                    liveCategories = emptyList(); liveStreams = emptyList()
                    vodCategories = emptyList(); vodStreams = emptyList()
                    seriesCategories = emptyList(); series = emptyList()
                }
            } catch (e: Exception) {
                error = "Failed to load content: ${e.message ?: "unknown error"}"
            } finally {
                loading = false
            }
        }
    }

    fun refresh() {
        active?.let { loadContent(it) }
    }

    // ---------- Stream URL builders (Xtream) ----------

    fun liveUrl(stream: LiveStream): String {
        val p = active ?: return ""
        return "${p.host}/live/${p.username}/${p.password}/${stream.streamId}.${prefs.liveFormat}"
    }

    fun movieUrl(vod: VodStream): String {
        val p = active ?: return ""
        val ext = vod.containerExtension?.ifBlank { null } ?: "mp4"
        return "${p.host}/movie/${p.username}/${p.password}/${vod.streamId}.$ext"
    }

    fun episodeUrl(episode: Episode): String {
        val p = active ?: return ""
        val ext = episode.containerExtension?.ifBlank { null } ?: "mp4"
        return "${p.host}/series/${p.username}/${p.password}/${episode.id}.$ext"
    }

    suspend fun fetchSeriesInfo(seriesId: String): SeriesInfoResponse? {
        val p = active ?: return null
        val a = api ?: ApiFactory.create(p.host).also { api = it }
        return try {
            a.seriesInfo(p.username, p.password, seriesId)
        } catch (e: Exception) {
            error = "Failed to load series: ${e.message ?: "unknown error"}"
            null
        }
    }

    // ---------- Favorites ----------

    fun isFavorite(key: String): Boolean = favorites.any { it.key == key }

    fun toggleFavorite(fav: Favorite) {
        val updated = if (isFavorite(fav.key)) {
            favorites.filterNot { it.key == fav.key }
        } else {
            favorites + fav
        }
        prefs.favorites = updated
        favorites = updated
    }
}
