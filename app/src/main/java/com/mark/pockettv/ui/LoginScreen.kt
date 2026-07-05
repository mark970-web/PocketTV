package com.mark.pockettv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("PocketTV", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Add your IPTV playlist to get started",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Xtream Codes") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("M3U URL") })
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Playlist name (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (tab == 0) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Server URL (http://host:port)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = m3uUrl,
                onValueChange = { m3uUrl = it },
                label = { Text("M3U playlist URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        if (vm.loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (tab == 0) {
                        vm.addXtreamPlaylist(name, host, user, pass) { ok ->
                            if (ok) onLoggedIn()
                        }
                    } else {
                        vm.addM3uPlaylist(name, m3uUrl) { ok ->
                            if (ok) onLoggedIn()
                        }
                    }
                },
                enabled = if (tab == 0) {
                    host.isNotBlank() && user.isNotBlank() && pass.isNotBlank()
                } else {
                    m3uUrl.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Playlist")
            }
        }

        vm.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (vm.playlists.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("Saved playlists", color = TextSecondary, fontSize = 13.sp)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                vm.playlists.forEach { p ->
                    TextButton(onClick = {
                        vm.setActive(p)
                        onLoggedIn()
                    }) {
                        Text("${p.name}  •  ${p.type.uppercase()}")
                    }
                }
            }
        }
    }
}
