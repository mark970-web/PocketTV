package com.mark.pockettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mark.pockettv.data.Favorite
import com.mark.pockettv.data.M3uChannel
import com.mark.pockettv.data.MainViewModel
import com.mark.pockettv.data.SeriesGrouper

private const val BOTTOM_PAD = 130 // clearance for floating nav + gesture bar

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onPlay: (url: String, title: String) -> Unit,
    onOpenSeries: (seriesId: String, name: String, cover: String) -> Unit,
    onOpenM3uSeries: (name: String) -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var settingsOpen by remember { mutableStateOf(false) }

    val isXtream = vm.active?.type == "xtream"

    Box(Modifier.fillMaxSize().background(Charcoal)) {
        Column(Modifier.fillMaxSize()) {

            // ---- Top bar ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                CircleGlassButton(onClick = { settingsOpen = true }) {
                    Icon(
                        Icons.Filled.Person, "Profile & settings",
                        tint = OnSurfaceEmber, modifier = Modifier.padding(8.dp)
                    )
                }
                Text(
                    text = when (tab) {
                        1 -> "Live"
                        2 -> "Movies"
                        3 -> "Series"
                        else -> "Home"
                    },
                    color = OnSurfaceEmber,
                    fontFamily = Lexend,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.6).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                CircleGlassButton(onClick = {
                    searchOpen = !searchOpen
                    if (!searchOpen) query = ""
                }) {
                    Icon(
                        Icons.Filled.Search, "Search",
                        tint = OnSurfaceVariantEmber, modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (searchOpen) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search…", fontFamily = Lexend) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmberContainer,
                        unfocusedBorderColor = OutlineVariantEmber,
                        focusedTextColor = OnSurfaceEmber,
                        unfocusedTextColor = OnSurfaceEmber,
                        cursorColor = EmberContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            if (vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmberContainer)
                }
            } else {
                when (tab) {
                    0 -> HomeTab(vm, onPlay, onOpenSeries, onOpenM3uSeries)
                    1 -> LiveTab(vm, query, isXtream, onPlay)
                    2 -> MoviesTab(vm, query, isXtream, onPlay)
                    3 -> SeriesTab(vm, query, isXtream, onOpenSeries, onOpenM3uSeries)
                }
            }
        }

        FloatingBottomNav(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (settingsOpen) {
        SettingsDialog(vm, onDismiss = { settingsOpen = false }, onLogout = onLogout)
    }
}

// ============================================================
// Home tab: hero + favorites + curated rows
// ============================================================

@Composable
private fun HomeTab(
    vm: MainViewModel,
    onPlay: (String, String) -> Unit,
    onOpenSeries: (String, String, String) -> Unit,
    onOpenM3uSeries: (String) -> Unit
) {
    val isXtream = vm.active?.type == "xtream"

    val m3uMovies = remember(vm.m3uChannels) { vm.m3uChannels.filter { it.kind == "movie" } }
    val m3uLive = remember(vm.m3uChannels) { vm.m3uChannels.filter { it.kind == "live" } }
    val m3uSeries = remember(vm.m3uChannels) {
        vm.m3uChannels.filter { it.kind == "series" }
            .groupBy { SeriesGrouper.seriesKey(it.name) }
            .map { (key, eps) -> Triple(key, eps.first().logo, eps.size) }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = BOTTOM_PAD.dp)) {

        // ---- Hero ----
        item {
            if (isXtream) {
                val featured = vm.vodStreams.firstOrNull()
                if (featured != null) {
                    val key = "movie:${featured.streamId}"
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HeroCard(
                            title = featured.name ?: "Featured",
                            subtitle = featured.rating?.let { "Rated $it" },
                            imageUrl = featured.icon,
                            isFavorite = vm.isFavorite(key),
                            onPlay = { onPlay(vm.movieUrl(featured), featured.name ?: "") },
                            onToggleFavorite = {
                                vm.toggleFavorite(
                                    Favorite(key, featured.name ?: "", featured.icon, "movie",
                                        featured.streamId ?: "", vm.movieUrl(featured))
                                )
                            }
                        )
                    }
                }
            } else {
                val featured = m3uMovies.firstOrNull { !it.logo.isNullOrBlank() }
                    ?: vm.m3uChannels.firstOrNull { !it.logo.isNullOrBlank() }
                    ?: vm.m3uChannels.firstOrNull()
                if (featured != null) {
                    val key = "m3u:${featured.url}"
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HeroCard(
                            title = featured.name,
                            subtitle = featured.group,
                            imageUrl = featured.logo,
                            isFavorite = vm.isFavorite(key),
                            onPlay = { onPlay(featured.url, featured.name) },
                            onToggleFavorite = {
                                vm.toggleFavorite(
                                    Favorite(key, featured.name, featured.logo, "m3u",
                                        featured.url, featured.url)
                                )
                            }
                        )
                    }
                }
            }
        }

        // ---- Favorites ----
        if (vm.favorites.isNotEmpty()) {
            item { SectionHeader("Your Favorites") }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(vm.favorites, key = { it.key }) { fav ->
                        PosterCard(
                            title = fav.title,
                            imageUrl = fav.image,
                            isFavorite = true,
                            onClick = {
                                when (fav.type) {
                                    "series" -> onOpenSeries(fav.refId, fav.title, fav.image ?: "")
                                    "m3useries" -> onOpenM3uSeries(fav.refId)
                                    else -> if (fav.url.isNotBlank()) onPlay(fav.url, fav.title)
                                }
                            },
                            onLongClick = { vm.toggleFavorite(fav) }
                        )
                    }
                }
            }
        }

        if (isXtream) {
            if (vm.vodStreams.size > 1) {
                item { SectionHeader("Trending Movies") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(vm.vodStreams.drop(1).take(25)) { vod ->
                            val key = "movie:${vod.streamId}"
                            PosterCard(
                                title = vod.name ?: "",
                                imageUrl = vod.icon,
                                isFavorite = vm.isFavorite(key),
                                rating = vod.rating?.takeIf { it.isNotBlank() && it != "0" },
                                onClick = { onPlay(vm.movieUrl(vod), vod.name ?: "") },
                                onLongClick = {
                                    vm.toggleFavorite(
                                        Favorite(key, vod.name ?: "", vod.icon, "movie",
                                            vod.streamId ?: "", vm.movieUrl(vod))
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (vm.series.isNotEmpty()) {
                item { SectionHeader("Series") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(vm.series.take(25)) { s ->
                            val key = "series:${s.seriesId}"
                            PosterCard(
                                title = s.name ?: "",
                                imageUrl = s.cover,
                                isFavorite = vm.isFavorite(key),
                                onClick = { onOpenSeries(s.seriesId ?: "", s.name ?: "", s.cover ?: "") },
                                onLongClick = {
                                    vm.toggleFavorite(
                                        Favorite(key, s.name ?: "", s.cover, "series", s.seriesId ?: "")
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (vm.liveStreams.isNotEmpty()) {
                item { SectionHeader("Live TV") }
                items(vm.liveStreams.take(12), key = { "l${it.streamId}" }) { ch ->
                    val key = "live:${ch.streamId}"
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        ChannelRow(
                            title = ch.name ?: "",
                            imageUrl = ch.icon,
                            subtitle = null,
                            isFavorite = vm.isFavorite(key),
                            onClick = { onPlay(vm.liveUrl(ch), ch.name ?: "") },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, ch.name ?: "", ch.icon, "live",
                                        ch.streamId ?: "", vm.liveUrl(ch))
                                )
                            }
                        )
                    }
                }
            }
        } else {
            // ---- M3U home: movies / series / live rows ----
            if (m3uMovies.isNotEmpty()) {
                item { SectionHeader("Movies") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(m3uMovies.take(25), key = { it.url }) { ch ->
                            val key = "m3u:${ch.url}"
                            PosterCard(
                                title = ch.name,
                                imageUrl = ch.logo,
                                isFavorite = vm.isFavorite(key),
                                onClick = { onPlay(ch.url, ch.name) },
                                onLongClick = {
                                    vm.toggleFavorite(
                                        Favorite(key, ch.name, ch.logo, "m3u", ch.url, ch.url)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (m3uSeries.isNotEmpty()) {
                item { SectionHeader("Series") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(m3uSeries.take(25), key = { it.first }) { (name, logo, count) ->
                            val key = "m3useries:$name"
                            PosterCard(
                                title = name,
                                imageUrl = logo,
                                isFavorite = vm.isFavorite(key),
                                subtitle = "$count episodes",
                                onClick = { onOpenM3uSeries(name) },
                                onLongClick = {
                                    vm.toggleFavorite(
                                        Favorite(key, name, logo, "m3useries", name)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (m3uLive.isNotEmpty()) {
                item { SectionHeader("Live TV") }
                items(m3uLive.take(15), key = { it.url }) { ch ->
                    val key = "m3u:${ch.url}"
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        ChannelRow(
                            title = ch.name,
                            imageUrl = ch.logo,
                            subtitle = ch.group,
                            isFavorite = vm.isFavorite(key),
                            onClick = { onPlay(ch.url, ch.name) },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, ch.name, ch.logo, "m3u", ch.url, ch.url)
                                )
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ============================================================
// Live tab — category-first
// ============================================================

@Composable
private fun LiveTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onPlay: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) } // id to name
    val searching = query.isNotBlank()

    if (isXtream) {
        val counts = remember(vm.liveStreams) {
            vm.liveStreams.groupingBy { it.categoryId ?: "" }.eachCount()
        }
        if (!searching && selected == null) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = BOTTOM_PAD.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.liveCategories, key = { it.categoryId ?: it.hashCode().toString() }) { c ->
                    CategoryRow(
                        name = c.categoryName ?: "Category",
                        count = counts[c.categoryId] ?: 0,
                        onClick = { selected = (c.categoryId ?: "") to (c.categoryName ?: "") }
                    )
                }
            }
        } else {
            val filtered = remember(vm.liveStreams, selected, query) {
                vm.liveStreams.filter { ch ->
                    (searching || ch.categoryId == selected?.first) &&
                        (!searching || (ch.name ?: "").contains(query, ignoreCase = true))
                }
            }
            Column {
                if (!searching) CategoryHeader(selected?.second ?: "", onBack = { selected = null })
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { "lv${it.streamId}" }) { ch ->
                        val key = "live:${ch.streamId}"
                        ChannelRow(
                            title = ch.name ?: "",
                            imageUrl = ch.icon,
                            subtitle = null,
                            isFavorite = vm.isFavorite(key),
                            onClick = { onPlay(vm.liveUrl(ch), ch.name ?: "") },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, ch.name ?: "", ch.icon, "live",
                                        ch.streamId ?: "", vm.liveUrl(ch))
                                )
                            }
                        )
                    }
                }
            }
        }
    } else {
        M3uCategoryBrowser(
            vm = vm,
            kind = "live",
            query = query,
            itemContent = { ch ->
                val key = "m3u:${ch.url}"
                ChannelRow(
                    title = ch.name,
                    imageUrl = ch.logo,
                    subtitle = ch.group,
                    isFavorite = vm.isFavorite(key),
                    onClick = { onPlay(ch.url, ch.name) },
                    onLongClick = {
                        vm.toggleFavorite(Favorite(key, ch.name, ch.logo, "m3u", ch.url, ch.url))
                    }
                )
            }
        )
    }
}

// ============================================================
// Movies tab — category-first with poster grid
// ============================================================

@Composable
private fun MoviesTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onPlay: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
    val searching = query.isNotBlank()

    if (isXtream) {
        val counts = remember(vm.vodStreams) {
            vm.vodStreams.groupingBy { it.categoryId ?: "" }.eachCount()
        }
        if (!searching && selected == null) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = BOTTOM_PAD.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.vodCategories, key = { it.categoryId ?: it.hashCode().toString() }) { c ->
                    CategoryRow(
                        name = c.categoryName ?: "Category",
                        count = counts[c.categoryId] ?: 0,
                        onClick = { selected = (c.categoryId ?: "") to (c.categoryName ?: "") }
                    )
                }
            }
        } else {
            val filtered = remember(vm.vodStreams, selected, query) {
                vm.vodStreams.filter { v ->
                    (searching || v.categoryId == selected?.first) &&
                        (!searching || (v.name ?: "").contains(query, ignoreCase = true))
                }
            }
            Column {
                if (!searching) CategoryHeader(selected?.second ?: "", onBack = { selected = null })
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { "v${it.streamId}" }) { vod ->
                        val key = "movie:${vod.streamId}"
                        PosterCard(
                            title = vod.name ?: "",
                            imageUrl = vod.icon,
                            isFavorite = vm.isFavorite(key),
                            rating = vod.rating?.takeIf { it.isNotBlank() && it != "0" },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPlay(vm.movieUrl(vod), vod.name ?: "") },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, vod.name ?: "", vod.icon, "movie",
                                        vod.streamId ?: "", vm.movieUrl(vod))
                                )
                            }
                        )
                    }
                }
            }
        }
    } else {
        M3uCategoryBrowser(
            vm = vm,
            kind = "movie",
            query = query,
            asGrid = true,
            gridItemContent = { ch ->
                val key = "m3u:${ch.url}"
                PosterCard(
                    title = ch.name,
                    imageUrl = ch.logo,
                    isFavorite = vm.isFavorite(key),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPlay(ch.url, ch.name) },
                    onLongClick = {
                        vm.toggleFavorite(Favorite(key, ch.name, ch.logo, "m3u", ch.url, ch.url))
                    }
                )
            }
        )
    }
}

// ============================================================
// Series tab — category-first; M3U series are grouped
// ============================================================

@Composable
private fun SeriesTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onOpenSeries: (String, String, String) -> Unit,
    onOpenM3uSeries: (String) -> Unit
) {
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
    val searching = query.isNotBlank()

    if (isXtream) {
        val counts = remember(vm.series) {
            vm.series.groupingBy { it.categoryId ?: "" }.eachCount()
        }
        if (!searching && selected == null) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = BOTTOM_PAD.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.seriesCategories, key = { it.categoryId ?: it.hashCode().toString() }) { c ->
                    CategoryRow(
                        name = c.categoryName ?: "Category",
                        count = counts[c.categoryId] ?: 0,
                        onClick = { selected = (c.categoryId ?: "") to (c.categoryName ?: "") }
                    )
                }
            }
        } else {
            val filtered = remember(vm.series, selected, query) {
                vm.series.filter { s ->
                    (searching || s.categoryId == selected?.first) &&
                        (!searching || (s.name ?: "").contains(query, ignoreCase = true))
                }
            }
            Column {
                if (!searching) CategoryHeader(selected?.second ?: "", onBack = { selected = null })
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { "s${it.seriesId}" }) { s ->
                        val key = "series:${s.seriesId}"
                        PosterCard(
                            title = s.name ?: "",
                            imageUrl = s.cover,
                            isFavorite = vm.isFavorite(key),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenSeries(s.seriesId ?: "", s.name ?: "", s.cover ?: "") },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, s.name ?: "", s.cover, "series", s.seriesId ?: "")
                                )
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Grouped M3U series: category list -> series grid -> episodes screen
        val allSeries = remember(vm.m3uChannels) {
            vm.m3uChannels.filter { it.kind == "series" }
                .groupBy { SeriesGrouper.seriesKey(it.name) }
                .map { (key, eps) ->
                    M3uSeriesSummary(key, eps.first().group, eps.first().logo, eps.size)
                }
        }
        val groups = remember(allSeries) {
            allSeries.groupingBy { it.group }.eachCount().toList().sortedBy { it.first }
        }
        if (!searching && selected == null) {
            if (groups.isEmpty()) {
                EmptyHint("No series detected in this playlist.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = BOTTOM_PAD.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(groups, key = { it.first }) { (name, count) ->
                        CategoryRow(name = name, count = count, onClick = { selected = name to name })
                    }
                }
            }
        } else {
            val filtered = remember(allSeries, selected, query) {
                allSeries.filter { s ->
                    (searching || s.group == selected?.first) &&
                        (!searching || s.name.contains(query, ignoreCase = true))
                }
            }
            Column {
                if (!searching) CategoryHeader(selected?.second ?: "", onBack = { selected = null })
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.name }) { s ->
                        val key = "m3useries:${s.name}"
                        PosterCard(
                            title = s.name,
                            imageUrl = s.logo,
                            isFavorite = vm.isFavorite(key),
                            subtitle = "${s.episodes} episodes",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenM3uSeries(s.name) },
                            onLongClick = {
                                vm.toggleFavorite(
                                    Favorite(key, s.name, s.logo, "m3useries", s.name)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class M3uSeriesSummary(
    val name: String,
    val group: String,
    val logo: String?,
    val episodes: Int
)

// ============================================================
// Shared M3U category browser (live + movies)
// ============================================================

@Composable
private fun M3uCategoryBrowser(
    vm: MainViewModel,
    kind: String,
    query: String,
    asGrid: Boolean = false,
    itemContent: (@Composable (M3uChannel) -> Unit)? = null,
    gridItemContent: (@Composable (M3uChannel) -> Unit)? = null
) {
    var selected by remember { mutableStateOf<String?>(null) }
    val searching = query.isNotBlank()

    val ofKind = remember(vm.m3uChannels, kind) { vm.m3uChannels.filter { it.kind == kind } }
    val groups = remember(ofKind) {
        ofKind.groupingBy { it.group }.eachCount().toList().sortedBy { it.first }
    }

    if (ofKind.isEmpty()) {
        EmptyHint("Nothing detected in this section for this playlist.")
        return
    }

    if (!searching && selected == null) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = BOTTOM_PAD.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups, key = { it.first }) { (name, count) ->
                CategoryRow(name = name, count = count, onClick = { selected = name })
            }
        }
    } else {
        val filtered = remember(ofKind, selected, query) {
            ofKind.filter { ch ->
                (searching || ch.group == selected) &&
                    (!searching || ch.name.contains(query, ignoreCase = true))
            }
        }
        Column {
            if (!searching) CategoryHeader(selected ?: "", onBack = { selected = null })
            if (asGrid && gridItemContent != null) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.url }) { ch -> gridItemContent(ch) }
                }
            } else if (itemContent != null) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = BOTTOM_PAD.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.url }) { ch -> itemContent(ch) }
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = OnSurfaceVariantEmber, fontFamily = Lexend, fontSize = 13.sp)
    }
}

// ============================================================
// Settings
// ============================================================

@Composable
private fun SettingsDialog(vm: MainViewModel, onDismiss: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainer,
        titleContentColor = OnSurfaceEmber,
        textContentColor = OnSurfaceVariantEmber,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = Gold, fontFamily = Lexend) }
        },
        title = { Text("Settings (v1.6)", fontFamily = Lexend, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { vm.refresh(); onDismiss() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Refresh, null, tint = Gold)
                        Text("  Refresh content", color = Gold, fontFamily = Lexend)
                    }
                }

                Text("Video player", fontFamily = Lexend, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var external by remember { mutableStateOf(vm.useExternalPlayer) }
                    FilterChip(
                        selected = !external,
                        onClick = { external = false; vm.useExternalPlayer = false },
                        label = { Text("Built-in", fontFamily = Lexend) }
                    )
                    FilterChip(
                        selected = external,
                        onClick = { external = true; vm.useExternalPlayer = true },
                        label = { Text("External (Samsung)", fontFamily = Lexend) }
                    )
                }
                Text(
                    "External hands playback to your phone's video player app.",
                    fontFamily = Lexend, fontSize = 12.sp
                )

                Text("Live stream format", fontFamily = Lexend, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var format by remember { mutableStateOf(vm.liveFormat) }
                    FilterChip(
                        selected = format == "m3u8",
                        onClick = { format = "m3u8"; vm.liveFormat = "m3u8" },
                        label = { Text("HLS (.m3u8)", fontFamily = Lexend) }
                    )
                    FilterChip(
                        selected = format == "ts",
                        onClick = { format = "ts"; vm.liveFormat = "ts" },
                        label = { Text("MPEG-TS (.ts)", fontFamily = Lexend) }
                    )
                }

                Text("Playlists", fontFamily = Lexend, fontSize = 13.sp)
                vm.playlists.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { vm.selectPlaylist(p); onDismiss() }) {
                            Text(
                                if (p.id == vm.active?.id) "● ${p.name}" else p.name,
                                color = if (p.id == vm.active?.id) Gold else OnSurfaceEmber,
                                fontFamily = Lexend,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            vm.deletePlaylist(p)
                            if (vm.active == null) {
                                onDismiss()
                                onLogout()
                            }
                        }) { Text("Delete", color = EmberPrimary, fontFamily = Lexend) }
                    }
                }
                TextButton(onClick = { onDismiss(); onLogout() }) {
                    Text("Add another playlist", color = Gold, fontFamily = Lexend)
                }
            }
        }
    )
}
