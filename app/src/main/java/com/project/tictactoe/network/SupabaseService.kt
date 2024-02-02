package com.project.tictactoe.network

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.viewmodels.SharedViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.PresenceAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.decodeJoinsAs
import io.github.jan.supabase.realtime.decodeLeavesAs
import io.github.jan.supabase.realtime.presenceChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import java.util.UUID

@Serializable
data class Player(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerialName("name")
    var name: String,
    @SerialName("isMyTurn")
    var isMyTurn: String,
    @SerialName("isInviter")
    var isInviter: Boolean = false
)

@Serializable
data class Game(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerialName("player1")
    val player1: Player,
    @SerialName("player2")
    val player2: Player,
    @SerialName("gameState")
    var gameState: GameEventType = GameEventType.PLAYER_READY
)

enum class GameResult {
    DRAW,
    SURRENDER,
    WIN,
    LOSE;
}

enum class ActionResult {
    MISS,
    HIT,
    SUNK;
}

enum class GameType {
    BATTLESHIPS,
    CONNECT_FOUR,
    TIC_TAC_TOE;
}

enum class ServerState {
    NOT_CONNECTED,
    LOADING_LOBBY,
    LOADING_GAME,
    LOBBY,
    GAME,
    IDLE;
}

@Serializable
enum class GameEventType {
    PLAYER_READY,
    RELEASE_TURN,
    ACTION,
    ANSWER,
    FINISH;
}

@Serializable
data class GameEvent(
    @SerialName("type")
    val type: GameEventType,
    @SerialName("data")
    val data: List<Int>
)

enum class BroadcastEvent(name: String) {
    INVITE("game_invite"),
    ACCEPT("game_accept"),
    DECLINE("game_decline"),
    GAME_EVENT("game_event");
}



interface SupabaseCallback {

    suspend fun playerReadyHandler()
    suspend fun releaseTurnHandler()
    suspend fun actionHandler(x: Int, y: Int)
    suspend fun answerHandler(status: ActionResult)
    suspend fun finishHandler(status: GameResult)
}

object SupabaseService : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val _type: GameType = GameType.TIC_TAC_TOE
    private const val _supabaseUrl = "https://yrqrbupsuyfsyqlrfruw.supabase.co"
    private const val _supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlycXJidXBzdXlmc3lxbHJmcnV3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTczODc0NTEsImV4cCI6MjAxMjk2MzQ1MX0.LSEAvPq3gobs9eWhuxF-Ut_e8FNTvQCRumYUjoqMPlU"
    private val _client =
        createSupabaseClient(supabaseUrl = _supabaseUrl, supabaseKey = _supabaseKey) {
            install(Realtime)
        }
    private var _lobby: RealtimeChannel? = null
    private var _game: RealtimeChannel? = null
    private var lobbyPresenceChangeFlow: Flow<PresenceAction>? = null
    private val _lobbyJobs = mutableListOf<Job>()
    private var _gameJobs = mutableListOf<Job>()

    val serverState = MutableStateFlow(ServerState.NOT_CONNECTED)



    var player: Player? = null
        private set
    var users = mutableStateListOf<Player>()
        private set(value) {
            field = value
            _usersFlow.value = value
            println("Users: $value")
        }
    var games = mutableStateListOf<Game>()
        private set
    var currentGame: Game? = null
        private set
    var callbackHandler: SupabaseCallback? = null

    private val sharedViewModel: SharedViewModel = SharedViewModel()

    // invitation response listener state flow
    private val _invitationResponses = MutableStateFlow<Game?>(null)
    val invitationResponses: StateFlow<Game?> = _invitationResponses

    // invitation listener state flow
    private val _invitations = MutableStateFlow<List<Game>>(emptyList())
    val invitations: StateFlow<List<Game>> = _invitations

    private val _gamesFlow = MutableStateFlow<List<Game>>(emptyList())
    val gamesFlow: StateFlow<List<Game>> = _gamesFlow

    private val _usersFlow = MutableStateFlow<List<Player>>(emptyList())
    val usersFlow: StateFlow<List<Player>> = _usersFlow

    private val _serverState = MutableStateFlow(ServerState.NOT_CONNECTED)
    val serverStateFlow: StateFlow<ServerState> = _serverState

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()



    init {
        setupInvitationResponseListener()
        setupInvitationListener()
    }

    suspend fun joinLobby(player: Player) {
        serverState.value = ServerState.LOADING_LOBBY
        println("- Server state from serverState.value: " + serverState.value)
        if (_client.realtime.status.value == Realtime.Status.DISCONNECTED) {
            println("Connect to Realtime")
            _client.realtime.connect()
        }
        if (_lobby == null) {
            SupabaseService.player = player
            println("Create Channel")
            val lobby = _client.realtime.createChannel("lobby-$_type")

            val presenceJob = lobby.presenceChangeFlow()
                .onEach {
                    println(it.decodeJoinsAs<Player>())
                    val newUsers = it.decodeJoinsAs<Player>()
                        .filter { player ->
                            !users.contains(player)
                        }
                    users.addAll(newUsers)

                    _usersFlow.value = users

                    it.decodeLeavesAs<Player>()
                        .forEach { player ->
                            users.remove(player)
                            // remove player from _usersFlow
                            _usersFlow.value = users
                        }
                }
                .launchIn(coroutineScope)
            _lobbyJobs.add(presenceJob)

            val gameInvitations = lobby.broadcastFlow<Game>(BroadcastEvent.INVITE.name)
                .onEach { game ->
                    if ((game.player2.id == player.id || game.player1.id == player.id) && !games.contains(
                            game
                        )
                    ) {
                        games.add(game)
                        val updatedGames = games.toMutableList().apply { add(game) }
                        _gamesFlow.value = updatedGames

                    }
                }
                .launchIn(coroutineScope)
            _lobbyJobs.add(gameInvitations)
            val gameAccepts = lobby.broadcastFlow<Game>(BroadcastEvent.ACCEPT.name)
                .onEach { game ->
                    if (game.player1.id == player.id) {
                        joinGame(game)
                    }
                }
                .launchIn(coroutineScope)
            _lobbyJobs.add(gameAccepts)
            val gameDeclines = lobby.broadcastFlow<Game>(BroadcastEvent.DECLINE.name)
                .onEach { game ->
                    games.remove(game)
                }
                .launchIn(coroutineScope)
            _lobbyJobs.add(gameDeclines)

            println("Join Lobby")
            lobby.join(blockUntilJoined = true)
            println("Track user")
            lobby.track(Json.encodeToJsonElement(player).jsonObject)
            _lobby = lobby
            serverState.value = ServerState.LOBBY
            println("- Server state from serverState.value: " + serverState.value)
        }
    }

     suspend fun leaveLobby() {
        _lobbyJobs.forEach { it.cancel() }
        _lobbyJobs.clear()
        _lobby?.untrack()
        _lobby?.leave()
        _lobby = null

        users.clear()
        games.clear()
    }


    private suspend fun joinGame(
        game: Game
    ) {
        currentGame = game
        println("Leave Lobby")
        leaveLobby()
        serverState.value = ServerState.LOADING_GAME
        println("Create Channel")
        val gameChannel = _client.realtime.createChannel("${game.id}")

        println("Subscribe to the channel")
        val playerReadyJob = gameChannel.broadcastFlow<GameEvent>(BroadcastEvent.GAME_EVENT.name)
            .onEach { event ->
                when (event.type) {
                    GameEventType.PLAYER_READY -> {
                        callbackHandler?.playerReadyHandler()
                    }

                    GameEventType.RELEASE_TURN -> {
                        callbackHandler?.releaseTurnHandler()
                    }

                    GameEventType.ACTION -> {
                        val x = event.data.first()
                        val y = if (event.data.count() > 1) {
                            event.data[1]
                        } else {
                            0
                        }
                        callbackHandler?.actionHandler(x, y)
                    }

                    GameEventType.ANSWER -> {
                        val status = ActionResult.values().getOrNull(event.data.first())
                        if (status != null) {
                            callbackHandler?.answerHandler(status)
                        }
                    }

                    GameEventType.FINISH -> {
                        val status = GameResult.values().getOrNull(event.data.first())
                        if (status != null) {
                            callbackHandler?.finishHandler(status)
                        }
                    }
                }
            }
            .launchIn(coroutineScope)
        _gameJobs.add(playerReadyJob)

        println("Join Channel")
        gameChannel.join()
        println("Setup finished")
        _game = gameChannel
        serverState.value = ServerState.GAME
    }

    suspend fun invite(opponent: Player) {
        if (player != null) {
            val game = Game(player1 = player!!, player2 = opponent)
            sendMessageToLobby(BroadcastEvent.INVITE, Json.encodeToJsonElement(game).jsonObject)
        }
    }

    suspend fun acceptInvite(game: Game) {
        games.clear()
        sendMessageToLobby(BroadcastEvent.ACCEPT, Json.encodeToJsonElement(game).jsonObject)
        joinGame(game)
    }

    suspend fun declineInvite(game: Game) {
        games.remove(game)
        sendMessageToLobby(BroadcastEvent.DECLINE, Json.encodeToJsonElement(game).jsonObject)
    }

    private suspend fun sendMessageToLobby(event: BroadcastEvent, message: JsonObject) {
        _lobby?.broadcast(
            event = event.name,
            message = message
        )
    }

    suspend fun releaseTurn() {
        val event = GameEvent(GameEventType.RELEASE_TURN, listOf())
        sendMessageToGame(BroadcastEvent.GAME_EVENT, Json.encodeToJsonElement(event).jsonObject)
    }

    suspend fun gameFinish(status: GameResult) {
        val event = GameEvent(GameEventType.FINISH, listOf(status.ordinal))
        sendMessageToGame(BroadcastEvent.GAME_EVENT, Json.encodeToJsonElement(event).jsonObject)
    }

    suspend fun playerReady() {
        val event = GameEvent(GameEventType.PLAYER_READY, listOf())
        sendMessageToGame(BroadcastEvent.GAME_EVENT, Json.encodeToJsonElement(event).jsonObject)
    }

    suspend fun sendTurn(vararg values: Int) {
        val event = GameEvent(GameEventType.ACTION, values.toList())
        sendMessageToGame(BroadcastEvent.GAME_EVENT, Json.encodeToJsonElement(event).jsonObject)
    }

    suspend fun sendAnswer(result: ActionResult) {
        val event = GameEvent(GameEventType.ANSWER, listOf(result.ordinal))
        sendMessageToGame(BroadcastEvent.GAME_EVENT, Json.encodeToJsonElement(event).jsonObject)
    }

    suspend fun leaveGame() {
        gameFinish(GameResult.SURRENDER)
        _gameJobs.forEach { it.cancel() }
        _gameJobs.clear()
        _game?.untrack()
        _game = null
    }

    private suspend fun sendMessageToGame(event: BroadcastEvent, message: JsonObject) {
        _game?.broadcast(
            event = event.name,
            message = message
        )
    }

    private fun setupInvitationResponseListener() {
        viewModelScope.launch {
            _lobby?.broadcastFlow<Game>(BroadcastEvent.ACCEPT.name)
                ?.onEach { game ->
                    _invitationResponses.value = game
                }
                ?.launchIn(coroutineScope)
        }
        // You can add similar logic for decline if needed
    }

    fun handleInvitationAcceptance(game: Game) {
        // Trigger shared ViewModel method
        sharedViewModel.onInvitationAccepted(game)
    }

    private fun setupInvitationListener() {
        viewModelScope.launch {
            // Assuming 'lobby' is the channel where invitations are broadcasted
            _lobby?.broadcastFlow<Game>(BroadcastEvent.INVITE.name)
                ?.onEach { game ->
                    _invitations.value = _invitations.value + game
                }
                ?.launchIn(coroutineScope)
        }
    }

    // Function to set current player
    fun setPlayer(currentPlayer: Player) {
        player = currentPlayer
    }
}