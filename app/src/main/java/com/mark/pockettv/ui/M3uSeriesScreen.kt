package com.mark.pockettv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mark.pockettv.data.MainViewModel
import com.mark.pockettv.data.SeriesGrouper

/** Episode list for a series assembled from loose M3U entries. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3uSeriesScreen(
    vm: MainViewModel,
    seriesName: String,
    onPlay: (url: String, title: String) -> Unit,
    onBack: () -> Unit
) {
    val episodes = remember(vm.m3uChannels, seriesName) {
        vm.m3uChannels
            .filter { it.kind == "series" && SeriesGrouper.seriesKey(it.name) == seriesName }
            .sortedBy { SeriesGrouper.episodeOrder(it.name) }
    }

    Scaffold(
        containerColor = Charcoal,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        seriesName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = OnSurfaceEmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Charcoal,
                    titleContentColor = OnSurfaceEmber
                )
            )
        }
    ) { padding ->
        if (episodes.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No episodes found.",
                    color = OnSurfaceVariantEmber,
                    fontFamily = Lexend,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(episodes, key = { it.url }) { ep ->
                    val label = SeriesGrouper.episodeLabel(ep.name)
                    ChannelRow(
                        title = if (label.isNotBlank()) "$label — $seriesName" else ep.name,
                        imageUrl = ep.logo,
                        subtitle = ep.group,
                        isFavorite = false,
                        onClick = { onPlay(ep.url, ep.name) },
                        onLongClick = {}
                    )
                }
            }
        }
    }
}
