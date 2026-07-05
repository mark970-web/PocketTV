package com.mark.pockettv

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mark.pockettv.data.MainViewModel
import com.mark.pockettv.ui.HomeScreen
import com.mark.pockettv.ui.LoginScreen
import com.mark.pockettv.ui.Charcoal
import com.mark.pockettv.ui.PocketTypography
import com.mark.pockettv.ui.PlayerScreen
import com.mark.pockettv.ui.PocketColors
import com.mark.pockettv.ui.SeriesScreen
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---- Crash reporter: saves any crash and shows it on next launch ----
        val crashFile = File(filesDir, "last_crash.txt")
        val previousCrash: String? =
            if (crashFile.exists()) runCatching { crashFile.readText() }.getOrNull() else null
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            runCatching { crashFile.writeText(Log.getStackTraceString(e)) }
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        setContent {
            MaterialTheme(colorScheme = PocketColors, typography = PocketTypography) {
                Surface(modifier = Modifier.fillMaxSize(), color = Charcoal) {
                    var crashText by remember { mutableStateOf(previousCrash) }
                    if (crashText != null) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = { Text("The app crashed last time") },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .heightIn(max = 400.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        "Screenshot this and send it to get a fix:\n\n" + (crashText ?: ""),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    runCatching { crashFile.delete() }
                                    crashText = null
                                }) { Text("Dismiss") }
                            }
                        )
                    }
                    val navController = rememberNavController()
                    val vm: MainViewModel = viewModel()

                    val start = if (vm.active != null) "home" else "login"

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
                                onPlay = { url, title ->
                                    navController.navigate(
                                        "player?url=${Uri.encode(url)}&title=${Uri.encode(title)}"
                                    )
                                },
                                onOpenSeries = { id, name, cover ->
                                    navController.navigate(
                                        "series?id=${Uri.encode(id)}&name=${Uri.encode(name)}&cover=${Uri.encode(cover)}"
                                    )
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
                                onPlay = { url, title ->
                                    navController.navigate(
                                        "player?url=${Uri.encode(url)}&title=${Uri.encode(title)}"
                                    )
                                },
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
