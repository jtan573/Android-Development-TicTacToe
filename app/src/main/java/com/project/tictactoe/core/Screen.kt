package com.project.tictactoe.core

sealed class Screen(val route:String) {
    object WelcomeScreen: Screen("welcome_screen")
    object HomeScreen: Screen("home_screen")
    object HelpScreen: Screen("help_screen")
    object ProfileScreen: Screen("profile_screen")
    object LobbyScreen: Screen("lobby_screen")
    object InvitationScreen: Screen("invitation_screen")
    object GameScreen: Screen("game_Screen")
}
