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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.project.tictactoe.network.Game
import com.project.tictactoe.viewmodels.LobbyViewModel

@Composable
fun InvitationScreen(lobbyViewModel: LobbyViewModel = viewModel(), navController: NavController) {

    val gameInvites = lobbyViewModel.gameInvites

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(70.dp),
                title = {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "GAME INVITES",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.LobbyScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "back_icon",
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
                text = "Game Requests",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "The following players have invited you:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Check if there are any online users
            if (gameInvites.isEmpty()) {
                Text(
                    text = "No game invitations.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn {
                    gameInvites.forEach {
                        item { InviteRow(it, lobbyViewModel) }
                    }
                }
            }
        }
    }
}

@Composable
fun InviteRow(invite: Game, lobbyViewModel: LobbyViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = invite.player1.name,
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                lobbyViewModel.acceptInvitation(invite)
            }
        ) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = "accept_invite")
        }
        IconButton(
            onClick = {
                lobbyViewModel.declineInvitation(invite)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "reject_invite",
                tint = Color.DarkGray
            )
        }
    }
}