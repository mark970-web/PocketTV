package com.mark.pockettv.data

import com.google.gson.annotations.SerializedName

// ---------- Xtream Codes API models ----------

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo? = null
)

data class UserInfo(
    @SerializedName("username") val username: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("exp_date") val expDate: String? = null
)

data class XtreamCategory(
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category_name") val categoryName: String? = null
)

data class LiveStream(
    @SerializedName("stream_id") val streamId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("stream_icon") val icon: String? = null,
    @SerializedName("category_id") val categoryId: String? = null
)

data class VodStream(
    @SerializedName("stream_id") val streamId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("stream_icon") val icon: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("container_extension") val containerExtension: String? = null,
    @SerializedName("rating") val rating: String? = null
)

data class SeriesItem(
    @SerializedName("series_id") val seriesId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("plot") val plot: String? = null
)

data class SeriesInfoResponse(
    @SerializedName("info") val info: SeriesMeta? = null,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>? = null
)

data class SeriesMeta(
    @SerializedName("name") val name: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null
)

data class Episode(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("container_extension") val containerExtension: String? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("episode_num") val episodeNum: String? = null,
    @SerializedName("info") val info: EpisodeInfo? = null
)

data class EpisodeInfo(
    @SerializedName("movie_image") val image: String? = null,
    @SerializedName("plot") val plot: String? = null
)

// ---------- M3U models ----------

data class M3uChannel(
    val name: String,
    val logo: String?,
    val group: String,
    val url: String,
    val kind: String // "live", "movie", "series"
)

// ---------- Local storage models ----------

data class Playlist(
    val id: String,
    val name: String,
    val type: String, // "xtream" or "m3u"
    val host: String = "",
    val username: String = "",
    val password: String = "",
    val m3uUrl: String = "",
    val filePath: String = ""
)

data class Favorite(
    val key: String,        // unique key: "type:id"
    val title: String,
    val image: String?,
    val type: String,       // "live", "movie", "series", "m3u"
    val refId: String,      // stream/series id or m3u url
    val url: String = ""    // direct playable url when known
)
