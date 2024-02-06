package com.project.tictactoe.viewmodels

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.project.tictactoe.core.Screen
import com.project.tictactoe.network.ActionResult
import com.project.tictactoe.network.Game
import com.project.tictactoe.network.GameResult
import com.project.tictactoe.network.Player
import com.project.tictactoe.network.ServerState
import com.project.tictactoe.network.SupabaseCallback
import com.project.tictactoe.network.SupabaseService
import com.project.tictactoe.network.SupabaseService.joinLobby
import com.project.tictactoe.network.SupabaseService.player
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LobbyViewModel() : ViewModel(){

    val currentPlayer = player

    private var _onlineUsers by mutableStateOf(emptyList<Player>())
    val onlineUsers: List<Player>
        get() = _onlineUsers


    var gameInvites = mutableStateListOf<Game>()


    private var _gamesInvitations by mutableStateOf(emptyList<Game>())
    val invitations: StateFlow<List<Game>> = SupabaseService.gamesFlow
    init {
        viewModelScope.launch {
            if (currentPlayer != null) {
                joinLobby(currentPlayer)
                // retrieve online users
                fetchOnlineUsers()
            }
        }
    }

    fun sendInvitation(toPlayer: Player) {
        viewModelScope.launch {
            try {
                SupabaseService.invite(toPlayer)
                SupabaseService.playerReady()

                println("print from sendInvitation()")
                println("SupabaseService.serverState: " + SupabaseService.serverState.value)
            } catch (e: Exception) {
                println("Error sendInvitation() in LobbyViewModel: ${e.message}")
            }
        }
    }

    fun acceptInvitation(game: Game) {
        viewModelScope.launch {
            try {
                SupabaseService.acceptInvite(game)
                SupabaseService.playerReady()
                println("playerReady() from LobbyViewModel in acceptInvitation()")
            } catch (e: Exception) {
                println("Error acceptInvitation() in LobbyViewModel: ${e.message}")
            }
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

    private fun fetchOnlineUsers() {
        viewModelScope.launch {
            SupabaseService.usersFlow.collect { users ->
                _onlineUsers = users
            }
        }
    }


}