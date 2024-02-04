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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.project.tictactoe.network.ActionResult
import com.project.tictactoe.network.SupabaseCallback
import com.project.tictactoe.viewmodels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Game(id=f321a542-c717-401e-9e8d-b47f4a99f4cb,
// player1=Player(id=0d8e8374-4059-4a5e-9c6f-18074e50fac7, name=habib, isMyTurn=, isInviter=false),
// player2=Player(id=f78fe7a7-4d64-45fe-a8e2-57ac6ef25c41, name=rohi, isMyTurn=, isInviter=false),
// gameState=PLAYER_READY)

class GameViewModel() : ViewModel(), SupabaseCallback {
    val sharedViewModel: SharedViewModel = SharedViewModel()

    private val _gameBoard: MutableState<Array<Array<String>>> =
        mutableStateOf(Array(3) { Array(3) { "" } })
    val gameBoard: MutableState<Array<Array<String>>> = _gameBoard

    // Track the current player's turn; assume "X" starts
    private val _currentPlayer = mutableStateOf("")
    val currentPlayer: State<String> = _currentPlayer

    var countMoves: Int = 0

    private val _isMyTurn = mutableStateOf(false)
    val isMyTurn: State<Boolean> = _isMyTurn

    private val _gameResult = mutableStateOf(GameResult.IN_PROGRESS)
    val gameResult: State<GameResult> = _gameResult


    init {
        SupabaseService.callbackHandler = this
        if (SupabaseService.currentGame?.player1 == SupabaseService.player) {
            _currentPlayer.value = "X"
        } else {
            _currentPlayer.value = "O"
        }
        if (SupabaseService.player?.isInviter == true) {
            _isMyTurn.value = true
        } else {
            _isMyTurn.value = false
        }
        // Set the initial game board
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                _gameBoard.value[row][col] = ""
            }
        }
    }

    fun onCellClicked(row: Int, col: Int) {
        if (gameBoard.value[row][col].isEmpty()) { // Check if cell is empty
            updateBoard(row, col, currentPlayer.value)
            viewModelScope.launch {
                SupabaseService.sendTurn(row, col)
                SupabaseService.releaseTurn()
                _isMyTurn.value = false
                val result = checkForWin()
                if (result) {
                    SupabaseService.gameFinish(GameResult.LOSE)
                    _gameResult.value = GameResult.WIN
                } else if (countMoves == 9) {
                    SupabaseService.gameFinish(GameResult.DRAW)
                    _gameResult.value = GameResult.DRAW
                }
            }

        }
    }

    private fun updateBoard(row: Int, col: Int, symbol: String) {
        val newBoard = gameBoard.value.copyOf() // Copy the current board
        newBoard[row][col] = symbol // Update the cell with the player's symbol
        _gameBoard.value = newBoard
        countMoves++
    }

    private fun checkForWin(): Boolean {
        // Check rows for a win
        for (i in 0 until 3) {
            if (gameBoard.value[i][0] == gameBoard.value[i][1] &&
                gameBoard.value[i][0] == gameBoard.value[i][2] &&
                gameBoard.value[i][0].isNotEmpty()
            ) {
                return true
            }
        }

        // Check columns for a win
        for (i in 0 until 3) {
            if (gameBoard.value[0][i] == gameBoard.value[1][i] &&
                gameBoard.value[0][i] == gameBoard.value[2][i] &&
                gameBoard.value[0][i].isNotEmpty()
            ) {
                return true
            }
        }

        // Check diagonals for a win
        if (gameBoard.value[0][0] == gameBoard.value[1][1] &&
            gameBoard.value[0][0] == gameBoard.value[2][2] &&
            gameBoard.value[0][0].isNotEmpty()
        ) {
            return true
        }
        if (gameBoard.value[0][2] == gameBoard.value[1][1] &&
            gameBoard.value[0][2] == gameBoard.value[2][0] &&
            gameBoard.value[0][2].isNotEmpty()
        ) {
            return true
        }

        return false
    }

    // Reset the game board for a new game
    fun resetGame() {
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                _gameBoard.value[row][col] = ""
            }
        }
        countMoves = 0
    }

    override suspend fun playerReadyHandler() {
        println("playerReadyHandler() from GameViewModel")
    }

    /**
     * This will be called when the other player releases the turn and it is your turn.
     */
    override suspend fun releaseTurnHandler() {
        _isMyTurn.value = true
    }

    override suspend fun actionHandler(x: Int, y: Int) {
        withContext(Dispatchers.Main)
        {
            updateBoard(x, y, if (currentPlayer.value == "X") "O" else "X")
        }
    }

    override suspend fun answerHandler(status: ActionResult) {
        TODO("Not yet implemented")
    }

    override suspend fun finishHandler(status: GameResult) {
        gameFinished(status)
    }

    private fun gameFinished(status: GameResult) {
        _gameResult.value = status
    }

}