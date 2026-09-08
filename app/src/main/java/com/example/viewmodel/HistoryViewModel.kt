package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.TransferRecord
import com.example.data.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransferRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        TransferRepository(db.transferDao())
    }

    val transfers: StateFlow<List<TransferRecord>> = repository.allTransfers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteTransfer(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransferById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }
}
