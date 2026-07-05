package com.mark.pockettv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface XtreamApi {

    @GET("player_api.php")
    suspend fun login(
        @Query("username") user: String,
        @Query("password") pass: String
    ): AuthResponse

    @GET("player_api.php")
    suspend fun liveCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun liveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams"
    ): List<LiveStream>

    @GET("player_api.php")
    suspend fun vodCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun vodStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<VodStream>

    @GET("player_api.php")
    suspend fun seriesCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun seriesList(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series"
    ): List<SeriesItem>

    @GET("player_api.php")
    suspend fun seriesInfo(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("series_id") seriesId: String,
        @Query("action") action: String = "get_series_info"
    ): SeriesInfoResponse
}

object ApiFactory {

    const val USER_AGENT = "PocketTV/1.0"

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
            )
        }
        .build()

    fun create(baseUrl: String): XtreamApi {
        val clean = baseUrl.trim().trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(clean)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApi::class.java)
    }

    suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url.trim()).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
            response.body?.string() ?: throw IllegalStateException("Empty response")
        }
    }
}

object M3uParser {

    fun parse(content: String): List<M3uChannel> {
        val channels = mutableListOf<M3uChannel>()
        var name = ""
        var logo: String? = null
        var group = "Uncategorized"

        val logoRegex = Regex("tvg-logo=\"(.*?)\"")
        val groupRegex = Regex("group-title=\"(.*?)\"")

        content.lineSequence().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    name = line.substringAfterLast(",").trim()
                    logo = logoRegex.find(line)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                    group = groupRegex.find(line)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                        ?: "Uncategorized"
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    val kind = when {
                        line.contains("/movie/", ignoreCase = true) -> "movie"
                        line.contains("/series/", ignoreCase = true) -> "series"
                        else -> "live"
                    }
                    channels.add(
                        M3uChannel(
                            name = name.ifBlank { line.substringAfterLast('/') },
                            logo = logo,
                            group = group,
                            url = line,
                            kind = kind
                        )
                    )
                    name = ""
                    logo = null
                    group = "Uncategorized"
                }
            }
        }
        return channels
    }
}
