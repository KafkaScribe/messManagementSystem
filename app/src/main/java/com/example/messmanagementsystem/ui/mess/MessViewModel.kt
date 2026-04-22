package com.example.messmanagementsystem.ui.mess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messmanagementsystem.data.repository.MessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MessUiState {
    object Idle : MessUiState()
    object Loading : MessUiState()
    data class Success(val messId: String) : MessUiState()
    data class Error(val message: String) : MessUiState()
}

class MessViewModel : ViewModel() {
    private val repository = MessRepository()

    private val _uiState = MutableStateFlow<MessUiState>(MessUiState.Idle)
    val uiState: StateFlow<MessUiState> = _uiState

    fun createMess(name: String, location: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = MessUiState.Loading
            repository.createMess(name, location, userId).fold(
                onSuccess = { messId -> _uiState.value = MessUiState.Success(messId) },
                onFailure = { _uiState.value = MessUiState.Error(it.message ?: "Failed to create mess") }
            )
        }
    }

    fun joinMess(inviteCode: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = MessUiState.Loading
            repository.joinMess(inviteCode, userId).fold(
                onSuccess = { messId -> _uiState.value = MessUiState.Success(messId) },
                onFailure = { _uiState.value = MessUiState.Error(it.message ?: "Failed to join mess") }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = MessUiState.Idle
    }
}
