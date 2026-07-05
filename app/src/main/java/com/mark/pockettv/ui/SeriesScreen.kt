package com.mark.pockettv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.mark.pockettv.data.MainViewModel
import com.mark.pockettv.data.SeriesInfoResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    vm: MainViewModel,
    seriesId: String,
    seriesName: String,
    cover: String,
    onPlay: (url: String, title: String) -> Unit,
    onBack: () -> Unit
) {
    var info by remember { mutableStateOf<SeriesInfoResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedSeason by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(seriesId) {
        loading = true
        info = vm.fetchSeriesInfo(seriesId)
        selectedSeason = info?.episodes?.keys?.sortedBy { it.toIntOrNull() ?: 0 }?.firstOrNull()
        loading = false
    }

    Scaffold(
        containerColor = Night,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        info?.info?.name ?: seriesName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Night,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val episodesBySeason = info?.episodes ?: emptyMap()
        val seasons = episodesBySeason.keys.sortedBy { it.toIntOrNull() ?: 0 }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(modifier = Modifier.padding(16.dp)) {
                    AsyncImage(
                        model = info?.info?.cover?.ifBlank { null } ?: cover,
                        contentDescription = seriesName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 110.dp, height = 165.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(
                            info?.info?.name ?: seriesName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val plot = info?.info?.plot
                        if (!plot.isNullOrBlank()) {
                            Text(
                                plot,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 7,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            if (seasons.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(seasons) { season ->
                            FilterChip(
                                selected = selectedSeason == season,
                                onClick = { selectedSeason = season },
                                label = { Text("Season $season") }
                            )
                        }
                    }
                }

                val episodes = episodesBySeason[selectedSeason].orEmpty()
                items(episodes, key = { it.id ?: it.hashCode().toString() }) { ep ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
                        ChannelRow(
                            title = "E${ep.episodeNum ?: "?"} — ${ep.title ?: "Episode"}",
                            imageUrl = ep.info?.image,
                            subtitle = ep.info?.plot,
                            isFavorite = false,
                            onClick = {
                                onPlay(vm.episodeUrl(ep), ep.title ?: seriesName)
                            },
                            onLongClick = {}
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "No episodes returned by the provider.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
