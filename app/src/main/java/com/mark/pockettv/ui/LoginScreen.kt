package com.mark.pockettv.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mark.pockettv.data.MainViewModel

@Composable
fun LoginScreen(vm: MainViewModel, onLoggedIn: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.addM3uFilePlaylist(name, uri) { ok -> if (ok) onLoggedIn() }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmberContainer,
        unfocusedBorderColor = OutlineVariantEmber,
        focusedLabelColor = EmberPrimary,
        unfocusedLabelColor = OnSurfaceVariantEmber,
        focusedTextColor = OnSurfaceEmber,
        unfocusedTextColor = OnSurfaceEmber,
        cursorColor = EmberContainer
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "PocketTV",
            color = OnSurfaceEmber,
            fontFamily = Lexend,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Add your IPTV playlist to get started",
            color = OnSurfaceVariantEmber,
            fontFamily = Lexend,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        TabRow(
            selectedTabIndex = tab,
            containerColor = Charcoal,
            contentColor = Gold,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    color = EmberContainer,
                    modifier = Modifier.tabIndicatorOffset(positions[tab])
                )
            }
        ) {
            Tab(selected = tab == 0, onClick = { tab = 0 },
                text = { Text("Xtream", fontFamily = Lexend, fontSize = 13.sp) })
            Tab(selected = tab == 1, onClick = { tab = 1 },
                text = { Text("M3U URL", fontFamily = Lexend, fontSize = 13.sp) })
            Tab(selected = tab == 2, onClick = { tab = 2 },
                text = { Text("M3U File", fontFamily = Lexend, fontSize = 13.sp) })
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Playlist name (optional)", fontFamily = Lexend) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Server URL (http://host:port)", fontFamily = Lexend) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Username", fontFamily = Lexend) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password", fontFamily = Lexend) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            1 -> {
                OutlinedTextField(
                    value = m3uUrl,
                    onValueChange = { m3uUrl = it },
                    label = { Text("M3U playlist URL", fontFamily = Lexend) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            2 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.FolderOpen,
                        contentDescription = null,
                        tint = OnSurfaceVariantEmber,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Pick a .m3u / .m3u8 file from your phone.\nA copy is stored inside the app.",
                        color = OnSurfaceVariantEmber,
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (vm.loading) {
            CircularProgressIndicator(color = EmberContainer)
        } else {
            when (tab) {
                0 -> GradientPlayButton(text = "Add Playlist") {
                    if (host.isNotBlank() && user.isNotBlank() && pass.isNotBlank()) {
                        vm.addXtreamPlaylist(name, host, user, pass) { ok ->
                            if (ok) onLoggedIn()
                        }
                    }
                }
                1 -> GradientPlayButton(text = "Add Playlist") {
                    if (m3uUrl.isNotBlank()) {
                        vm.addM3uPlaylist(name, m3uUrl) { ok -> if (ok) onLoggedIn() }
                    }
                }
                2 -> GradientPlayButton(text = "Choose File") {
                    filePicker.launch("*/*")
                }
            }
        }

        vm.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = Lexend,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (vm.playlists.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("Saved playlists", color = OnSurfaceVariantEmber, fontFamily = Lexend, fontSize = 13.sp)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                vm.playlists.forEach { p ->
                    TextButton(onClick = {
                        vm.selectPlaylist(p)
                        onLoggedIn()
                    }) {
                        Text(
                            "${p.name}  •  ${p.type.uppercase()}",
                            color = Gold,
                            fontFamily = Lexend
                        )
                    }
                }
            }
        }
    }
}
