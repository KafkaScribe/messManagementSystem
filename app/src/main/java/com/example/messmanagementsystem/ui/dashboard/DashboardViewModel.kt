package com.example.messmanagementsystem.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MessRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MemberStat(
    val name: String,
    val mealCount: Int,
    val deposit: Double,
    val cost: Double,
    val balance: Double
)

data class DashboardState(
    val userName: String = "",
    val userId: String = "",
    val phoneNumber: String = "",
    val messId: String? = null,
    val messName: String? = null,
    val statusInMess: String? = null,
    val role: String = "user",
    
    // Dashboard Stats
    val totalDeposit: Double = 0.0,
    val totalMealCount: Int = 0,
    val totalMealCost: Double = 0.0,
    val mealRate: Double = 0.0,
    val totalOtherCost: Double = 0.0,
    val messBalance: Double = 0.0,
    
    val myMealCount: Int = 0,
    val myDeposit: Double = 0.0,
    val myCost: Double = 0.0,
    val myBalance: Double = 0.0,
    
    val memberStats: List<MemberStat> = emptyList(),
    
    val notifications: List<com.example.messmanagementsystem.data.model.Notification> = emptyList(),

    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val messRepository = MessRepository()
    private val userRepository = UserRepository()
    private val depositRepository = com.example.messmanagementsystem.data.repository.DepositRepository()
    private val expenseRepository = com.example.messmanagementsystem.data.repository.ExpenseRepository()
    private val mealRepository = com.example.messmanagementsystem.data.repository.MealRepository()
    private val notificationRepository = com.example.messmanagementsystem.data.repository.NotificationRepository()
    
    private val _uiState = MutableStateFlow(DashboardState(isLoading = true))
    val uiState: StateFlow<DashboardState> = _uiState

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            loadUserData()
        }
    }

    private suspend fun loadUserData() {
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
                            phoneNumber = user.phoneNumber,
                            messId = user.messId,
                            messName = messName,
                            statusInMess = user.statusInMess,
                            role = user.role,
                            isLoading = false
                        )
                        
                        // Load stats if user has a mess
                        if (user.messId != null && user.statusInMess == "approved") {
                            loadDashboardStats(user.messId, user.uid)
                        }
                        
                        // Load notifications
                        val notifs = notificationRepository.getNotificationsForUser(user.uid)
                        _uiState.value = _uiState.value.copy(notifications = notifs)
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

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            val uid = _uiState.value.userId
            if (uid.isNotEmpty()) {
                val notifs = notificationRepository.getNotificationsForUser(uid)
                _uiState.value = _uiState.value.copy(notifications = notifs)
            }
        }
    }

    private suspend fun loadDashboardStats(messId: String, currentUserId: String) {
        try {
            val monthId = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
            
            val members = userRepository.getMembersByMess(messId)
            val deposits = depositRepository.getDepositsForMessByMonth(messId, monthId)
            val expenses = expenseRepository.getExpensesForMessByMonth(messId, monthId)
            val meals = mealRepository.getMealsForMessByMonth(members.map { it.uid }, monthId)
            
            val totalDeposit = deposits.sumOf { it.amount }
            
            val mealCosts = expenses.filter { it.expenseCategory == "meal_cost" }
            val totalMealCost = mealCosts.sumOf { it.amount }
            
            val otherCosts = expenses.filter { it.expenseCategory == "other_cost" }
            val totalOtherCost = otherCosts.sumOf { it.amount }
            val sharedOtherCosts = otherCosts.filter { it.otherCostType == "shared" }
            val individualOtherCosts = otherCosts.filter { it.otherCostType == "individual" }
            
            val totalSharedOtherCost = sharedOtherCosts.sumOf { it.amount }
            val memberCount = maxOf(1, members.size)
            val sharedCostPerMember = totalSharedOtherCost / memberCount

            var totalMealCount = 0
            meals.forEach { meal ->
                if (meal.hasBreakfast) totalMealCount++
                if (meal.hasLunch) totalMealCount++
                if (meal.hasDinner) totalMealCount++
            }
            
            val mealRate = if (totalMealCount > 0) totalMealCost / totalMealCount else 0.0
            val messBalance = totalDeposit - (totalMealCost + totalOtherCost)

            val memberStatsList = members.map { member ->
                val memberMeals = meals.filter { it.userId == member.uid }
                var memberMealCount = 0
                memberMeals.forEach { meal ->
                    if (meal.hasBreakfast) memberMealCount++
                    if (meal.hasLunch) memberMealCount++
                    if (meal.hasDinner) memberMealCount++
                }
                
                val memberDeposit = deposits.filter { it.userId == member.uid }.sumOf { it.amount }
                
                val memberIndividualCost = individualOtherCosts
                    .filter { it.targetUsers.contains(member.uid) }
                    .sumOf { exp -> exp.amount / maxOf(1, exp.targetUsers.size) }
                    
                val memberTotalCost = (memberMealCount * mealRate) + memberIndividualCost + sharedCostPerMember
                val memberBalance = memberDeposit - memberTotalCost
                
                MemberStat(
                    name = member.name.ifEmpty { "Unknown" },
                    mealCount = memberMealCount,
                    deposit = memberDeposit,
                    cost = memberTotalCost,
                    balance = memberBalance
                )
            }
            
            val myStat = memberStatsList.find { members[memberStatsList.indexOf(it)].uid == currentUserId }
            
            _uiState.value = _uiState.value.copy(
                totalDeposit = totalDeposit,
                totalMealCount = totalMealCount,
                totalMealCost = totalMealCost,
                mealRate = mealRate,
                totalOtherCost = totalOtherCost,
                messBalance = messBalance,
                myMealCount = myStat?.mealCount ?: 0,
                myDeposit = myStat?.deposit ?: 0.0,
                myCost = myStat?.cost ?: 0.0,
                myBalance = myStat?.balance ?: 0.0,
                memberStats = memberStatsList
            )
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
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
