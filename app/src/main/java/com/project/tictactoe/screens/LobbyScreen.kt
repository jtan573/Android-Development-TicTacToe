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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.viewmodels.LobbyViewModel

@Composable
fun LobbyScreen(lobbyViewModel: LobbyViewModel = viewModel(), navController: NavController) {

    val currentPlayer = lobbyViewModel.currentPlayer
    val onlineUsers = lobbyViewModel.onlineUsers

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
                                        UserRow(currentPlayer = currentPlayer, user = it, lobbyViewModel = lobbyViewModel)
                                    }
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
fun UserRow(currentPlayer: Player, user: Player, lobbyViewModel: LobbyViewModel) {
    Text(
        text = user.name,
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        color = Color.DarkGray,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Left
    )
    IconButton(
        onClick = {
            lobbyViewModel.sendInvitation(user)
            // Player who sent an invite becomes the Inviter, and hence player 1
            currentPlayer.isMyTurn = true
            currentPlayer.isInviter = true
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Send,
            contentDescription = "send_invite",
            tint = Color.DarkGray
        )
    }
}