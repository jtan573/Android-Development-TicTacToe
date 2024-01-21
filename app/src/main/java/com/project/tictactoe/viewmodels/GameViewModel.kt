package com.project.tictactoe.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.sourceInformationMarkerEnd
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tictactoe.network.BroadcastEvent
import com.project.tictactoe.network.Game
import com.project.tictactoe.network.GameEvent
import com.project.tictactoe.network.GameEventType
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.SupabaseService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

class GameViewModel: ViewModel() {

    private val boardSize = 3
    private val player1Symbol = "X"
    private val player2Symbol = "O"

    // 2D array representing the game board
    private val gameBoard = Array(boardSize) { Array(boardSize) { "" } }

    // Variable to keep track of the current player
    private var currentPlayer = player1Symbol

    // Get game board cells
    fun getBoard(): Array<Array<String>> {
        return gameBoard
    }

    fun getCurrentPlayer(): String {
        return currentPlayer
    }

    // Function to make a move on the board
    fun makeMove(row: Int, col: Int) {
        if (gameBoard[row][col].isEmpty()) {
            gameBoard[row][col] = currentPlayer
            checkForWinner()
            switchPlayer()
            releaseTurn()
        }
    }

    // Function to switch to the next player
    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == player1Symbol) player2Symbol else player1Symbol
    }

    // Function to check for a winner
    fun checkForWinner(): String {
        // three in a row
        for (row in 0..2) {
            // Check if card in the row was chosen by any player before, else move to next row immediately
            if (gameBoard[row][0].isEmpty() || gameBoard[row][1].isEmpty() || gameBoard[row][2].isEmpty())
                continue
            if ((gameBoard[row][0] == gameBoard[row][1]) &&
                (gameBoard[row][1] == gameBoard[row][2])
            ) {
                return gameBoard[row][0]
            }
        }

        // three in a column
        for (col in 0..2) {
            // Check if card in the column was chosen by any player before, else move to next row immediately
            if (gameBoard[0][col].isEmpty() || gameBoard[1][col].isEmpty() || gameBoard[2][col].isEmpty())
                continue
            if ((gameBoard[0][col] == gameBoard[1][col]) &&
                (gameBoard[1][col] == gameBoard[2][col])) {
                return gameBoard[0][col]
            }
        }

        // three in a diagonal (sloping DOWN left to right)
        if (gameBoard[0][0].isEmpty() || gameBoard[1][1].isEmpty() || gameBoard[2][2].isEmpty()) {
            return ""
        }
        else {
            if ((gameBoard[0][0] == gameBoard[1][1]) &&
                (gameBoard[1][1] == gameBoard[2][2]))
                return gameBoard[0][0]
        }

        // three in a diagonal (sloping UP left to right)
        if (gameBoard[2][0].isEmpty() || gameBoard[1][1].isEmpty() || gameBoard[0][2].isEmpty()) {
            return ""
        }
        else {
            if ((gameBoard[2][0] == gameBoard[1][1]) &&
                (gameBoard[1][1] == gameBoard[0][2]))
                return gameBoard[2][0]
        }
        return ""
    }

    fun isBoardFull(): Boolean {
        for (row in 0..2) {
            for (col in 0..2) {
                if (gameBoard[row][col].isEmpty()) {
                    return false
                }
            }
        }
        return true
    }

    fun releaseTurn() {
        viewModelScope.launch {
            SupabaseService.releaseTurn()
        }
        viewModelScope.launch {
            SupabaseService.sendTurn()
        }
    }

    fun gameFinish(status: GameResult) {
        viewModelScope.launch {
            SupabaseService.gameFinish(status)
        }
    }

    fun leaveGame() {
        viewModelScope.launch {
            SupabaseService.leaveGame()
        }
    }
}