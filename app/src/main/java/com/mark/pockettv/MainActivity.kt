package com.mark.pockettv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mark.pockettv.data.MainViewModel
import com.mark.pockettv.ui.Charcoal
import com.mark.pockettv.ui.HomeScreen
import com.mark.pockettv.ui.Lexend
import com.mark.pockettv.ui.LoginScreen
import com.mark.pockettv.ui.M3uSeriesScreen
import com.mark.pockettv.ui.PlayerScreen
import com.mark.pockettv.ui.PocketColors
import com.mark.pockettv.ui.SeriesScreen
import com.mark.pockettv.ui.pocketTypography
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---- Crash capture: saves any crash so the shell can display it ----
        val crashFile = File(filesDir, "last_crash.txt")
        val previousCrash: String? =
            if (crashFile.exists()) runCatching { crashFile.readText() }.getOrNull() else null
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            runCatching { crashFile.writeText(Log.getStackTraceString(e)) }
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // Load bundled Lexend only if it verifiably works on this device
        Lexend = runCatching {
            if (ResourcesCompat.getFont(this, R.font.lexend) != null)
                FontFamily(Font(R.font.lexend))
            else null
        }.getOrNull() ?: FontFamily.Default

        setContent {
            var started by remember { mutableStateOf(false) }
            var crashText by remember { mutableStateOf(previousCrash) }

            if (!started) {
                // ============================================================
                // DIAGNOSTIC SHELL — deliberately primitive so it CANNOT fail:
                // no theme, no custom font, no navigation, no view models.
                // ============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF131313))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(40.dp))
                    BasicText(
                        "PocketTV — diagnostic v1.6",
                        style = TextStyle(color = Color.White, fontSize = 20.sp)
                    )
                    Spacer(Modifier.height(8.dp))
                    BasicText(
                        if (crashText == null)
                            "No crash recorded. Tap START APP."
                        else
                            "A crash was recorded. Screenshot the red text below and send it.",
                        style = TextStyle(color = Color(0xFFBBBBBB), fontSize = 13.sp)
                    )
                    Spacer(Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color(0xFFE68A4D))
                            .clickable { started = true }
                    ) {
                        BasicText(
                            "START APP",
                            style = TextStyle(color = Color.White, fontSize = 16.sp)
                        )
                    }

                    if (crashText != null) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(Color(0xFF353534))
                                .clickable {
                                    runCatching { crashFile.delete() }
                                    crashText = null
                                }
                        ) {
                            BasicText(
                                "CLEAR CRASH LOG",
                                style = TextStyle(color = Color.White, fontSize = 13.sp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        BasicText(
                            crashText ?: "",
                            style = TextStyle(
                                color = Color(0xFFFF8A8A),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                }
            } else {
                // ============================================================
                // REAL APP
                // ============================================================
                MaterialTheme(colorScheme = PocketColors, typography = pocketTypography()) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Charcoal) {
                        val navController = rememberNavController()
                        val vm: MainViewModel = viewModel()

                        val start = if (vm.active != null) "home" else "login"

                        val play: (String, String) -> Unit = { url, title ->
                            if (vm.useExternalPlayer) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(url), "video/*")
                                    putExtra("title", title)
                                }
                                runCatching {
                                    startActivity(Intent.createChooser(intent, "Play with"))
                                }
                            } else {
                                navController.navigate(
                                    "player?url=${Uri.encode(url)}&title=${Uri.encode(title)}"
                                )
                            }
                        }

                        NavHost(navController = navController, startDestination = start) {

                            composable("login") {
                                LoginScreen(vm = vm, onLoggedIn = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                })
                            }

                            composable("home") {
                                HomeScreen(
                                    vm = vm,
                                    onPlay = play,
                                    onOpenSeries = { id, name, cover ->
                                        navController.navigate(
                                            "series?id=${Uri.encode(id)}&name=${Uri.encode(name)}&cover=${Uri.encode(cover)}"
                                        )
                                    },
                                    onOpenM3uSeries = { name ->
                                        navController.navigate("m3useries?name=${Uri.encode(name)}")
                                    },
                                    onLogout = {
                                        navController.navigate("login")
                                    }
                                )
                            }

                            composable(
                                route = "series?id={id}&name={name}&cover={cover}",
                                arguments = listOf(
                                    navArgument("id") { type = NavType.StringType; defaultValue = "" },
                                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                                    navArgument("cover") { type = NavType.StringType; defaultValue = "" }
                                )
                            ) { entry ->
                                SeriesScreen(
                                    vm = vm,
                                    seriesId = entry.arguments?.getString("id").orEmpty(),
                                    seriesName = entry.arguments?.getString("name").orEmpty(),
                                    cover = entry.arguments?.getString("cover").orEmpty(),
                                    onPlay = play,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "m3useries?name={name}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType; defaultValue = "" }
                                )
                            ) { entry ->
                                M3uSeriesScreen(
                                    vm = vm,
                                    seriesName = entry.arguments?.getString("name").orEmpty(),
                                    onPlay = play,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "player?url={url}&title={title}",
                                arguments = listOf(
                                    navArgument("url") { type = NavType.StringType; defaultValue = "" },
                                    navArgument("title") { type = NavType.StringType; defaultValue = "" }
                                )
                            ) { entry ->
                                PlayerScreen(
                                    url = entry.arguments?.getString("url").orEmpty(),
                                    title = entry.arguments?.getString("title").orEmpty()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
