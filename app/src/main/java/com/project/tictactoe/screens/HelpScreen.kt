package com.project.tictactoe.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import com.project.tictactoe.core.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen (navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(70.dp),
                title = {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "HOW TO PLAY?",
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
                .padding(paddingValues)
        ) {
            Column (
                modifier = Modifier.absolutePadding(10.dp)
            ){
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Text(text =
                "Welcome to Tic Tac Toe!" +
                        "\n" + "\n" + "How to play:" +
                        "\n" + "1. The game is played on a 3x3 grid." +
                        "\n" + "2. Each player takes turns marking a square with their symbol (X or O)." +
                        "\n" + "3. The first player to get three of their symbols in a row (horizontally, vertically, or diagonally) wins." +
                        "\n" + "4. If the grid is filled and no player has three in a row, the game is a draw." +
                        "\n" + "\n" + "Let's get started! Here's an example of the game board:"
                )
                displayExampleBoard()
                Text(text = "\nNow, you're ready to play. Have fun!")
            }
        }
    }
}

@Composable
fun displayExampleBoard() {
    Text(text =
    "\t" + " 1 | 2 | 3 " + "\n" +
            "\t" + " ------------" + "\n" +
            "\t" + " 4 | 5 | 6 " + "\n" +
            "\t" + " ------------" + "\n" +
            "\t" + " 7 | 8 | 9 "
    )
}