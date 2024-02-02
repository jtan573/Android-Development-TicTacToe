package com.tictactoe.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import com.tictactoe.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.viewmodels.GameViewModel


@Composable
fun GameScreen(navController: NavController, gameId: String? = null) {
    val sharedViewModel: SharedViewModel = viewModel()
    val gameViewModel: GameViewModel = viewModel()
    val isMyTurn by sharedViewModel.isMyTurn.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val opponentMove by sharedViewModel.opponentMove.collectAsState()


    // when opponentMove varianle is not null, then update the board with opponent's move
    if (opponentMove != null) {
        gameViewModel.updateBoard(opponentMove!!.first, opponentMove!!.second, "O")
    }

    LaunchedEffect(isMyTurn) {
        if (isMyTurn) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "It's your turn!",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val board = gameViewModel.getBoard()
    if (GameResult == GameResult.WIN || GameResult == GameResult.LOSE || GameResult == GameResult.DRAW) {
        GameEndDialog(gameState = GameResult, navController = navController)
    }

    Scaffold(
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
                            navController.navigate(Screen.ProfileScreen.route)
                        }
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle, contentDescription = "Profile",
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
        ) {
            for (row in 0 until 3) {
                Row {
                    for (col in 0 until 3) {
                        CellBox(
                            isMyTurn = isMyTurn,
                            cellContent = board[row][col],
                            onCellClicked = {
                                if (isMyTurn && board[row][col].isEmpty()) {
                                    gameViewModel.updateBoard(row, col)
                                    gameViewModel.releaseMyTurn()
                                } else if (board[row][col].isNotEmpty()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "This cell is not empty!",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data)
        }
    }


}


@Composable
fun CellBox(
    isMyTurn: Boolean,
    cellContent: String,
    onCellClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .clickable(enabled = isMyTurn && cellContent.isEmpty()) {
                onCellClicked()
            }
    ) {
        Text(
            text = cellContent, // Display the content dynamically
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.Center)
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
                        navController.popBackStack()
                    },
                fontSize = 16.sp
            )
        }
    )
}