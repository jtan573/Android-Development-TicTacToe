package com.project.tictactoe.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.AlertDialog
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.network.Player
import com.project.tictactoe.viewmodels.GameViewModel

@SuppressLint("UnrememberedMutableState")
@Composable
fun GameScreen(gameViewModel: GameViewModel = viewModel(), player: Player, navController: NavController) {

    val board = gameViewModel.getBoard()
    var currentPlayer by remember { mutableStateOf(gameViewModel.getCurrentPlayer()) }
    var isMyTurn = true
    // var isMyTurn by remember { mutableStateOf(player.isMyTurn) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(70.dp),
                title = {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "GAME",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            gameViewModel.leaveGame()
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isMyTurn) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Your Turn",
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
            }
            else {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Opponent's Turn",
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
            }

            // Continue game while board is not full and there is no winner
            while (!gameViewModel.isBoardFull() && !gameViewModel.checkForWinner()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                           item { BoardView(gameViewModel, board[row][col], player) }
                        }
                    }
                }
            }
            /*
            // When game ends
            // First scenario: A draw
            if (gameViewModel.isBoardFull() && !gameViewModel.checkForWinner()) {
                gameViewModel.gameFinish(GameResult.DRAW)
                GameEndDialog(GameResult.DRAW, navController)
            }
            // Second scenario: A win from current player
            else {
                gameViewModel.gameFinish(GameResult.WIN)
                GameEndDialog(GameResult.WIN, navController)
            }
            */
        }
    }
}

@Composable
fun BoardView(gameViewModel: GameViewModel, cell: String , currentPlayer: Player) {

    var playerAssignment: String = cell

    // Buttons are only activated if it is the current player's turn
    if (currentPlayer.isMyTurn) {
        Button(
            onClick = {
                if (cell.isNotEmpty()) {
                    // TODO: send an alert to say cannot click
                } else {
                    gameViewModel.releaseTurn()
                    currentPlayer.isMyTurn = false
                }
            },
            modifier = Modifier.aspectRatio(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RectangleShape
        ) {
            if (playerAssignment == "X") {
                Icon(Icons.Filled.Clear, contentDescription = "player 1 symbol")
            } else if (playerAssignment == "O") {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = "player 2 symbol")
            }
        }
    }
    else {
        if (playerAssignment == "X") {
            Card( modifier = Modifier.aspectRatio(1f),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Icon(Icons.Filled.Clear, contentDescription = "player 1 symbol")
            }
        } else if (playerAssignment == "O") {
            Card( modifier = Modifier.aspectRatio(1f),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = "player 2 symbol")
            }
        } else {
            Card ( modifier = Modifier.aspectRatio(1f),
                border = BorderStroke(1.dp, Color.DarkGray) ) { }
        }
    }
}

@Composable
fun GameEndDialog(gameState: GameResult, navController: NavController) {
    AlertDialog(
        onDismissRequest = { navController.navigate(route = Screen.HomeScreen.route) },
        title = {
            Text(
                text = when (gameState) {
                    GameResult.WIN -> "You won!"
                    GameResult.LOSE -> "You lost!"
                    GameResult.DRAW -> "It's a draw!"
                    else -> {
                        ""
                    }
                },
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = when (gameState) {
                    GameResult.WIN -> "Congratulations! You won the game."
                    GameResult.LOSE -> "You lost the game. Better luck next time!"
                    GameResult.DRAW -> "It's a draw. Try again!"
                    else -> {
                        ""
                    }
                },
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Text(
                text = "Play again",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(route = Screen.LobbyScreen.route)
                    },
                fontSize = 16.sp
            )
        },
        dismissButton = {
            Text(
                text = "Exit",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(route = Screen.HomeScreen.route)
                    },
                fontSize = 16.sp
            )
        },
        backgroundColor = Color.LightGray
    )
}