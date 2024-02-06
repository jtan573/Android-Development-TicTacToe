package com.project.tictactoe.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.Game
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.ServerState
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.viewmodels.LobbyViewModel
import com.project.tictactoe.viewmodels.SharedViewModel


@Composable
fun LobbyScreen(navController: NavController) {

    val lobbyViewModel: LobbyViewModel = viewModel()
    val currentPlayer = lobbyViewModel.currentPlayer
    val onlineUsers = lobbyViewModel.onlineUsers
    val receivedChallenges by SupabaseService.gamesFlow.collectAsState()

    val sharedViewModel: SharedViewModel = viewModel()

    val serverState by SupabaseService.serverState.collectAsState()


    LaunchedEffect(serverState) {
        println("serverState from LaunchedEffect in LobbyScreen.")
        if (serverState == ServerState.GAME) {
            println("Navigating to GameScreen")
            navController.navigate(Screen.GameScreen.route)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(70.dp),
                title = {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "LOBBY",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            lobbyViewModel.leaveLobby()
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "back_icon",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.InvitationScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Invites",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color.DarkGray
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Online Users",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Check if there are any online users
            if (onlineUsers.isEmpty() || onlineUsers.size == 1) {
                println("online users from LobbyScreen: " + onlineUsers)
                Text(
                    text = "No online users.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn {
                    onlineUsers.forEach {
                        item {
                            if (currentPlayer != null) {
                                if (it.id != currentPlayer.id) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        UserRow(
                                            currentPlayer = currentPlayer,
                                            user = it,
                                            lobbyViewModel = lobbyViewModel,
                                            sharedViewModel = sharedViewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // show a list of all invitations received
            Text(
                text = "Invitations",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (receivedChallenges.isEmpty()) {
                Text(
                    text = "No invitations received.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn {
                    receivedChallenges.forEach {
                        item {
                            if (currentPlayer != null) {
                                if (it.player2.id == currentPlayer.id) {
                                    InvitationRow(
                                        currentPlayer = currentPlayer,
                                        game = it,
                                        lobbyViewModel = lobbyViewModel,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationRow(
    currentPlayer: Player?, game: Game,
    lobbyViewModel: LobbyViewModel,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(text = game.player2.name)
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                lobbyViewModel.acceptInvitation(game)
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
        ) {
            Text(text = "Accept")
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                lobbyViewModel.declineInvitation(game)
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
        ) {
            Text(text = "Decline")
        }
    }
}

@Composable
fun UserRow(
    currentPlayer: Player,
    user: Player,
    lobbyViewModel: LobbyViewModel,
    sharedViewModel: SharedViewModel
) {
    var inviteButtonText by remember { mutableStateOf("Invite") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(text = user.name)
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                lobbyViewModel.sendInvitation(user)
                println("print from lobbyscreen: sendInvitation()")
                SupabaseService.player?.isInviter = true
                inviteButtonText = "Invitation sent"
            },
            modifier = Modifier
                .padding(10.dp)
                .height(40.dp)
        ) {
            Text(text = inviteButtonText)
        }
    }
}