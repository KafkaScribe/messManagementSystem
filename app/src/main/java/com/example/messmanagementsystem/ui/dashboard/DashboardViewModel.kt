package com.example.messmanagementsystem.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MessRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val userName: String = "",
    val userId: String = "",
    val messId: String? = null,
    val messName: String? = null,
    val statusInMess: String? = null,
    val role: String = "user",
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val messRepository = MessRepository()
    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(DashboardState(isLoading = true))
    val uiState: StateFlow<DashboardState> = _uiState

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.getCurrentUserDetails().fold(
                onSuccess = { user ->
                    if (user != null) {
                        var messName: String? = null
                        if (user.statusInMess == "invited" && user.messId != null) {
                            val mess = messRepository.getAssignedMessInfo(user.messId)
                            messName = mess?.name
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            userName = user.name.ifEmpty { "User" },
                            userId = user.uid,
                            messId = user.messId,
                            messName = messName,
                            statusInMess = user.statusInMess,
                            role = user.role,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "User data not found")
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun acceptInvitation() {
        val uid = _uiState.value.userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.acceptInvite(uid).fold(
                onSuccess = {
                    loadUserData() // Refresh UI state
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun declineInvitation() {
        val uid = _uiState.value.userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.declineInvite(uid).fold(
                onSuccess = {
                    loadUserData() // Refresh UI state
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }
}
