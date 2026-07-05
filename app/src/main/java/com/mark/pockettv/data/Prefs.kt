package com.mark.pockettv.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("pockettv", Context.MODE_PRIVATE)
    private val gson = Gson()

    var playlists: List<Playlist>
        get() {
            val json = sp.getString("playlists", null) ?: return emptyList()
            val type = object : TypeToken<List<Playlist>>() {}.type
            return try {
                val list: List<Playlist> = gson.fromJson(json, type) ?: emptyList()
                list.filter { p ->
                    @Suppress("USELESS_CAST", "SENSELESS_COMPARISON")
                    (p.id as String?) != null && (p.name as String?) != null && (p.type as String?) != null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            sp.edit().putString("playlists", gson.toJson(value)).apply()
        }

    var activePlaylistId: String?
        get() = sp.getString("active_playlist", null)
        set(value) {
            sp.edit().putString("active_playlist", value).apply()
        }

    var favorites: List<Favorite>
        get() {
            val json = sp.getString("favorites", null) ?: return emptyList()
            val type = object : TypeToken<List<Favorite>>() {}.type
            return try {
                val list: List<Favorite> = gson.fromJson(json, type) ?: emptyList()
                list.filter { f ->
                    @Suppress("USELESS_CAST", "SENSELESS_COMPARISON")
                    (f.key as String?) != null && (f.title as String?) != null &&
                        (f.type as String?) != null && (f.refId as String?) != null &&
                        (f.url as String?) != null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            sp.edit().putString("favorites", gson.toJson(value)).apply()
        }

    /** Live stream container: "m3u8" (HLS) or "ts" (MPEG-TS). Some providers only support one. */
    var liveFormat: String
        get() = sp.getString("live_format", "m3u8") ?: "m3u8"
        set(value) {
            sp.edit().putString("live_format", value).apply()
        }
}
