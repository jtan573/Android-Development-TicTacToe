package com.project.tictactoe.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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

data class Cell(
    val rowId: Int,
    val colId: Int,
    var belongsToPlayer: Int = 0,
    var isChosen: MutableState<Boolean> = mutableStateOf(false)
) {
    fun isSelected(player: Player) {
        isChosen.value = true
        belongsToPlayer = if (player.isInviter) {
            1
        } else {
            2
        }
    }
}

data class Board(
    val cells: MutableList<Cell>
) {
    private fun getBoard(): MutableList<Cell> {
        return cells
    }

    fun checkCell(row: Int, col: Int): Int {
        cells.forEach {
            if (it.rowId == row && it.colId == col) {
                return it.belongsToPlayer
            }
        }
        return 0
    }

    fun isBoardFull(): Boolean {
        cells.forEach {
            if (it.isChosen == mutableStateOf(false))
                return false
        }
        return true
    }

    /**
     * Check if current player has won
     */
    fun checkForWin(): Boolean {
        // three in a row
        for (row in 0..2) {
            // Check if card in the row was chosen by any player before, else move to next row immediately
            if (checkCell(row,0) == 0 || checkCell(row,1) == 0 || checkCell(row,2) == 0)
                continue
            if ((checkCell(row,0) == checkCell(row,1)) &&
                (checkCell(row,1) == checkCell(row,2))) {
                return true
            }
        }

        // three in a column
        for (col in 0..2) {
            // Check if card in the column was chosen by any player before, else move to next row immediately
            if (checkCell(0,col) == 0 || checkCell(1,col) == 0 || checkCell(2,col) == 0)
                continue
            if ((checkCell(0,col) == checkCell(1,col)) &&
                (checkCell(1,col) == checkCell(2,col))) {
                return true
            }
        }

        // three in a diagonal (sloping DOWN left to right)
        if (checkCell(0,0) == 0 || checkCell(1,1) == 0 || checkCell(2,2) == 0) {
            return false
        }
        else {
            if ((checkCell(0,0) == checkCell(1,1)) &&
                (checkCell(1,1) == checkCell(2,2)))
                return true
        }

        // three in a diagonal (sloping UP left to right)
        if (checkCell(2,0) == 0 || checkCell(1,1) == 0 || checkCell(0,2) == 0) {
            return false
        }
        else {
            if ((checkCell(2,0) == checkCell(1,1)) &&
                (checkCell(1,1) == checkCell(0,2)))
                return true
        }

        return false
    }
}

class GameViewModel: ViewModel() {
    val cells = mutableStateListOf<Cell>()
    val board = Board(cells)

    init {
        cells.clear()
        val tempCards = mutableStateListOf<Cell>()
        for (row in 0..2) {
            for (col in 0..2) {
                tempCards.add(Cell(rowId = row, colId = col, isChosen = mutableStateOf(false)))
            }
        }
        cells.addAll(tempCards)
    }

    fun releaseTurn() {
        viewModelScope.launch {
            SupabaseService.releaseTurn()
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