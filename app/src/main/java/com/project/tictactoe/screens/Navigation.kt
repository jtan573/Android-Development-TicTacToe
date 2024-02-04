package com.project.tictactoe.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.SupabaseService
import com.tictactoe.screen.GameScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {

    val navController = rememberNavController()
    val currentPlayer by remember { mutableStateOf(Player(name = "", isMyTurn = "")) }

    Scaffold(

    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = Screen.WelcomeScreen.route
        ) {
            composable(Screen.WelcomeScreen.route) {
                WelcomeScreen(navController = navController, player = currentPlayer)
                SupabaseService.setPlayer(currentPlayer)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(navController = navController, player = currentPlayer)
            }
            composable(Screen.HelpScreen.route) {
                HelpScreen(navController = navController)
            }
            composable(Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController)
            }
            composable(Screen.LobbyScreen.route) {
                LobbyScreen(navController = navController)
            }
            composable(Screen.InvitationScreen.route) {
                InvitationScreen(navController = navController)
            }
            composable(Screen.GameScreen.route) {
                GameScreen(navController = navController)
            }
        }
    }

}

