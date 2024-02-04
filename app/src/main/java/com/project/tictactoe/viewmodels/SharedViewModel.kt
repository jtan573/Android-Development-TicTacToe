package com.project.tictactoe.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.project.tictactoe.network.ActionResult
import com.project.tictactoe.network.Game
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.ServerState
import com.project.tictactoe.network.SupabaseCallback
import com.project.tictactoe.network.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel(), SupabaseCallback {
    // Listen to the opponent who accepted your invitation to start the game
    private val _opponentReady = MutableStateFlow(false)
    val opponentReady: StateFlow<Boolean> = _opponentReady.asStateFlow()

    // check whether it is your turn or not
    private val _isMyTurn = MutableStateFlow(false)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn.asStateFlow()

    // keep track of opponent's move
    private val _opponentMove = MutableStateFlow<Pair<Int, Int>?>(null)
    val opponentMove: StateFlow<Pair<Int, Int>?> = _opponentMove.asStateFlow()

    // var currentPlayer: Player = Player()
    var currentPlayer by mutableStateOf(Player(name = "", isMyTurn = ""))

    private val _invitationResponse = MutableStateFlow<Game?>(null)
    val invitationResponse: StateFlow<Game?> = _invitationResponse

    private val _serverState = MutableStateFlow<ServerState>(ServerState.LOBBY)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    private val _challenger = MutableStateFlow<Boolean>(false)
    val challenger: StateFlow<Boolean> = _challenger.asStateFlow()




    fun setChallenger(challenger: Boolean) {
        _challenger.value = challenger
    }


    fun onInvitationAccepted(game: Game) {
        viewModelScope.launch {
            _invitationResponse.emit(game)
            // set isInviter in player to true
        }
    }


    fun resetGameStartEvent() {
        _invitationResponse.value = null
    }

    init {
        SupabaseService.callbackHandler = this
        viewModelScope.launch {
            SupabaseService.serverState.collect { newState ->
                _serverState.value = newState
            }
        }
    }

    fun changeUsername(newName: String) {
        currentPlayer = currentPlayer.copy(name = newName)
    }


    /**
     * This will be called when the other player sends a playerReady message
     */
    override suspend fun playerReadyHandler() {
        _opponentReady.value = true
        println("playerReadyHandler() from SharedViewModel")
    }

    /**
     * This will be called when the other player releases the turn and it is your turn.
     */
    override suspend fun releaseTurnHandler() {
        _isMyTurn.value = true
    }

    /**
     * This will be called when the other player sends a turn message (for example: select a field
     * in Tic Tac Toe, a column in Connect Four or a coordinate in Battleships).
     */
    override suspend fun actionHandler(x: Int, y: Int) {
        _opponentMove.value = Pair(x, y)

    }

    /**
     * This will be called in Battleships when the other player sends you the answer to a turn.
     * The status can be either Miss, Hit, or Sunk.
     */
    override suspend fun answerHandler(status: ActionResult) {
        // THIS IS NOT USED IN TIC TAC TOE
        TODO("Not yet implemented")
    }

    /**
     * This will be called when the game is finished. The status can be either Win, Lose, or Draw.
     */
    override suspend fun finishHandler(status: GameResult) {
        if (status == GameResult.WIN) {
            SupabaseService.gameFinish(GameResult.WIN)
        } else if (status == GameResult.LOSE) {
            SupabaseService.gameFinish(GameResult.LOSE)
        } else if (status == GameResult.DRAW) {
            SupabaseService.gameFinish(GameResult.DRAW)
        }
    }

}