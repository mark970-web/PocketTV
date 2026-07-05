package com.mark.pockettv.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mark.pockettv.data.Favorite
import com.mark.pockettv.data.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = Night,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        vm.active?.name ?: "PocketTV",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Night,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        searchOpen = !searchOpen
                        if (!searchOpen) query = ""
                    }) {
                        Icon(Icons.Filled.Search, "Search", tint = TextPrimary)
                    }
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Filled.Refresh, "Refresh", tint = TextPrimary)
                    }
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Filled.Settings, "Settings", tint = TextPrimary)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceNavy) {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, "Home") }, label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.LiveTv, "Live") }, label = { Text("Live") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Movie, "Movies") }, label = { Text("Movies") }
                )
                NavigationBarItem(
                    selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(Icons.Filled.Tv, "Series") }, label = { Text("Series") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (searchOpen) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search…") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (tab) {
                    0 -> HomeTab(vm, query, onPlay, onOpenSeries)
                    1 -> LiveTab(vm, query, isXtream, onPlay)
                    2 -> MoviesTab(vm, query, isXtream, onPlay)
                    3 -> SeriesTab(vm, query, isXtream, onPlay, onOpenSeries)
                }
            }
        }
    }

    if (settingsOpen) {
        SettingsDialog(vm, onDismiss = { settingsOpen = false }, onLogout = onLogout)
    }
}

// ---------- Home tab: favorites + content rows ----------

@Composable
private fun HomeTab(
    vm: MainViewModel,
    query: String,
    onPlay: (String, String) -> Unit,
    onOpenSeries: (String, String, String) -> Unit
) {
    val isXtream = vm.active?.type == "xtream"
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {

        if (vm.favorites.isNotEmpty()) {
            item { SectionHeader("Favorites") }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
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
            if (vm.vodStreams.isNotEmpty()) {
                item { SectionHeader("Movies") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(vm.vodStreams.take(25)) { vod ->
                            val key = "movie:${vod.streamId}"
                            PosterCard(
                                title = vod.name ?: "",
                                imageUrl = vod.icon,
                                isFavorite = vm.isFavorite(key),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
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
                items(vm.liveStreams.take(15), key = { "l${it.streamId}" }) { ch ->
                    val key = "live:${ch.streamId}"
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
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
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                columns = GridCells.Adaptive(112.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered, key = { "v${it.streamId}" }) { vod ->
                    val key = "movie:${vod.streamId}"
                    PosterCard(
                        title = vod.name ?: "",
                        imageUrl = vod.icon,
                        isFavorite = vm.isFavorite(key),
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
                columns = GridCells.Adaptive(112.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    color = TextSecondary, fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Live stream format", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var format by remember { mutableStateOf(vm.liveFormat) }
                    FilterChip(
                        selected = format == "m3u8",
                        onClick = { format = "m3u8"; vm.liveFormat = "m3u8" },
                        label = { Text("HLS (.m3u8)") }
                    )
                    FilterChip(
                        selected = format == "ts",
                        onClick = { format = "ts"; vm.liveFormat = "ts" },
                        label = { Text("MPEG-TS (.ts)") }
                    )
                }
                Text(
                    "If live channels won't play, switch the format — providers differ.",
                    color = TextSecondary, fontSize = 12.sp
                )
                Text("Playlists", color = TextSecondary, fontSize = 13.sp)
                vm.playlists.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { vm.setActive(p); onDismiss() }) {
                            Text(if (p.id == vm.active?.id) "● ${p.name}" else p.name)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            vm.deletePlaylist(p)
                            if (vm.active == null) {
                                onDismiss()
                                onLogout()
                            }
                        }) { Text("Delete") }
                    }
                }
                TextButton(onClick = { onDismiss(); onLogout() }) {
                    Text("Add another playlist")
                }
            }
        }
    )
}
