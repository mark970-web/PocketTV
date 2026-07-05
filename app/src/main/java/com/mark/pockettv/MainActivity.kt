package com.mark.pockettv

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.mark.pockettv.ui.Night
import com.mark.pockettv.ui.PlayerScreen
import com.mark.pockettv.ui.PocketColors
import com.mark.pockettv.ui.SeriesScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = PocketColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = Night) {
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
