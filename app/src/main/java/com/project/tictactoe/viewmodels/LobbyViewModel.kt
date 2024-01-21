package com.project.tictactoe.viewmodels

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tictactoe.network.Game
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.network.SupabaseService.joinLobby
import com.project.tictactoe.network.SupabaseService.player
import kotlinx.coroutines.launch

class LobbyViewModel: ViewModel() {

    val currentPlayer = player

    private val _onlineUsers = mutableStateListOf<Player>()
    val onlineUsers: SnapshotStateList<Player>
        get() = _onlineUsers

    private val _gameInvites = mutableStateListOf<Game>()
    val gameInvites: SnapshotStateList<Game>
        get() = _gameInvites

    init {
        viewModelScope.launch {
            if (currentPlayer != null) {
                joinLobby(currentPlayer)
            }
        }
    }

    fun sendInvitation(toPlayer: Player) {
        viewModelScope.launch {
            SupabaseService.invite(toPlayer)
        }
    }

    fun acceptInvitation(game: Game) {
        viewModelScope.launch {
            SupabaseService.acceptInvite(game)
            SupabaseService.playerReady()
            currentPlayer!!.isInviter = false
            currentPlayer.isMyTurn = "O"
        }
    }

    fun declineInvitation(game: Game) {
        viewModelScope.launch {
            SupabaseService.declineInvite(game)
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            SupabaseService.leaveLobby()
        }
    }
}