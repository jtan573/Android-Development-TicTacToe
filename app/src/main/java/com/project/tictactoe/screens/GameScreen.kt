package com.tictactoe.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.viewmodels.GameViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, gameId: String? = null) {
    val gameViewModel: GameViewModel = viewModel()
    // Assuming isMyTurn and other states are properly managed in GameViewModel or SharedViewModel
    var isMyTurn: State<Boolean> = gameViewModel.isMyTurn

    val gameBoard by gameViewModel.gameBoard
    val gameResult by gameViewModel.gameResult


    if (gameResult == GameResult.WIN || gameResult == GameResult.LOSE || gameResult == GameResult.DRAW) {
        GameEndDialog(gameResult, navController)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(70.dp)
                    .background(Color.Gray),
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
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "back_icon",
                            tint = Color.DarkGray
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.ProfileScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle, contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Player: ${SupabaseService.currentGame?.player1?.name} vs ${
                    SupabaseService.currentGame?.player2?.name
                }", fontSize = 30.sp, modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Your symbol: ${gameViewModel.currentPlayer.value}", fontSize = 16.sp
            )
            Text(
                text = "Opponent's symbol: ${if (gameViewModel.currentPlayer.value == "X") "O" else "X"}",
            )

            if (isMyTurn.value) {
                Text(text = "Your turn", fontSize = 16.sp)
            } else {
                Text(text = "Opponent's turn", fontSize = 16.sp)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                // Other parameters
            ) {
                items(9) { index ->
                    val row = index / 3
                    val col = index % 3
                    CellBox(
                        isMyTurn = isMyTurn,
                        cellContent = gameBoard[row][col],
                        onCellClicked = {
                            gameViewModel.onCellClicked(row, col)
                        }
                    )
                }
            }
        }
    }


}


@Composable
fun CellBox(
    isMyTurn: State<Boolean>,
    cellContent: String,
    onCellClicked: () -> Unit
) {
    val context = LocalContext.current
    Button(
        onClick = {
            if (isMyTurn.value && cellContent.isEmpty()) {
                onCellClicked()
            } else {
                val message =
                    if (cellContent.isEmpty()) "It's not your turn" else "Cell already filled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
        shape = RectangleShape
    ) {
        Text(
            text = cellContent,
            fontSize = 30.sp
        )
    }
}


@Composable
fun GameEndDialog(gameState: GameResult, navController: NavController) {
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
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
                        navController.popBackStack()
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
                        navController.navigate(Screen.HomeScreen.route)
                    },
                fontSize = 16.sp
            )
        }
    )
}
