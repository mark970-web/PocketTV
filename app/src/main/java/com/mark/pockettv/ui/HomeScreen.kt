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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mark.pockettv.data.Favorite
import com.mark.pockettv.data.MainViewModel

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onPlay: (url: String, title: String) -> Unit,
    onOpenSeries: (seriesId: String, name: String, cover: String) -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var settingsOpen by remember { mutableStateOf(false) }

    val isXtream = vm.active?.type == "xtream"

    Box(Modifier.fillMaxSize().background(Charcoal)) {
        Column(Modifier.fillMaxSize()) {

            // ---- Glass top bar: avatar circle · centered title · search circle ----
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
                    letterSpacing = (-0.03).em,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                    0 -> HomeTab(vm, onPlay, onOpenSeries)
                    1 -> LiveTab(vm, query, isXtream, onPlay)
                    2 -> MoviesTab(vm, query, isXtream, onPlay)
                    3 -> SeriesTab(vm, query, isXtream, onPlay, onOpenSeries)
                }
            }
        }

        // ---- Floating pill navigation ----
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

// ---------- Home tab: hero + favorites + rows ----------

@Composable
private fun HomeTab(
    vm: MainViewModel,
    onPlay: (String, String) -> Unit,
    onOpenSeries: (String, String, String) -> Unit
) {
    val isXtream = vm.active?.type == "xtream"

    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp)) {

        // Hero: first movie (Xtream) or first channel (M3U)
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
                val featured = vm.m3uChannels.firstOrNull { !it.logo.isNullOrBlank() }
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
                item { SectionHeader("Trending Now") }
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
            val channels = vm.m3uChannels
            if (channels.isNotEmpty()) {
                item { SectionHeader("Channels") }
                items(channels.take(40), key = { it.url }) { ch ->
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

// ---------- Live tab ----------

@Composable
private fun LiveTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onPlay: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column {
        if (isXtream) {
            CategoryChips(
                categories = vm.liveCategories.mapNotNull { c ->
                    val id = c.categoryId ?: return@mapNotNull null
                    id to (c.categoryName ?: id)
                },
                selectedId = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            val filtered = vm.liveStreams.filter { ch ->
                (selectedCategory == null || ch.categoryId == selectedCategory) &&
                    (query.isBlank() || (ch.name ?: "").contains(query, ignoreCase = true))
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
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
        } else {
            M3uList(vm, query, kind = "live", onPlay = onPlay)
        }
    }
}

// ---------- Movies tab ----------

@Composable
private fun MoviesTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onPlay: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column {
        if (isXtream) {
            CategoryChips(
                categories = vm.vodCategories.mapNotNull { c ->
                    val id = c.categoryId ?: return@mapNotNull null
                    id to (c.categoryName ?: id)
                },
                selectedId = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            val filtered = vm.vodStreams.filter { v ->
                (selectedCategory == null || v.categoryId == selectedCategory) &&
                    (query.isBlank() || (v.name ?: "").contains(query, ignoreCase = true))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp),
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
        } else {
            M3uList(vm, query, kind = "movie", onPlay = onPlay)
        }
    }
}

// ---------- Series tab ----------

@Composable
private fun SeriesTab(
    vm: MainViewModel,
    query: String,
    isXtream: Boolean,
    onPlay: (String, String) -> Unit,
    onOpenSeries: (String, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column {
        if (isXtream) {
            CategoryChips(
                categories = vm.seriesCategories.mapNotNull { c ->
                    val id = c.categoryId ?: return@mapNotNull null
                    id to (c.categoryName ?: id)
                },
                selectedId = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            val filtered = vm.series.filter { s ->
                (selectedCategory == null || s.categoryId == selectedCategory) &&
                    (query.isBlank() || (s.name ?: "").contains(query, ignoreCase = true))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp),
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
        } else {
            M3uList(vm, query, kind = "series", onPlay = onPlay)
        }
    }
}

// ---------- Shared M3U list with group chips ----------

@Composable
private fun M3uList(
    vm: MainViewModel,
    query: String,
    kind: String,
    onPlay: (String, String) -> Unit
) {
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    val ofKind = vm.m3uChannels.filter { it.kind == kind }
    val groups = ofKind.map { it.group }.distinct().sorted()

    Column {
        CategoryChips(
            categories = groups.map { it to it },
            selectedId = selectedGroup,
            onSelect = { selectedGroup = it }
        )
        val filtered = ofKind.filter { ch ->
            (selectedGroup == null || ch.group == selectedGroup) &&
                (query.isBlank() || ch.name.contains(query, ignoreCase = true))
        }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing here. This playlist may keep all content under Live.",
                    color = OnSurfaceVariantEmber, fontFamily = Lexend, fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.url }) { ch ->
                    val key = "m3u:${ch.url}"
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
}

// ---------- Settings dialog ----------

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
        title = { Text("Settings", fontFamily = Lexend, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { vm.refresh(); onDismiss() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Refresh, null, tint = Gold)
                        Text("  Refresh content", color = Gold, fontFamily = Lexend)
                    }
                }
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
                Text(
                    "If live channels won't play, switch the format — providers differ.",
                    fontFamily = Lexend, fontSize = 12.sp
                )
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
