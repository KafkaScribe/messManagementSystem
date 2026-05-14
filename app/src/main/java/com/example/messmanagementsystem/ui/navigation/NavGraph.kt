package com.example.messmanagementsystem.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messmanagementsystem.ui.auth.LoginScreen
import com.example.messmanagementsystem.ui.auth.RegisterScreen
import com.example.messmanagementsystem.ui.dashboard.DashboardScreen
import com.example.messmanagementsystem.ui.mess.CreateMessScreen
import com.example.messmanagementsystem.ui.mess.JoinMessScreen
import com.example.messmanagementsystem.ui.mess.MessSelectionScreen
import com.example.messmanagementsystem.ui.mess.PendingApprovalScreen
import com.example.messmanagementsystem.ui.auth.AuthViewModel
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.example.messmanagementsystem.ui.admin.AdminPanelScreen
import com.example.messmanagementsystem.ui.common.PlaceholderScreen
import com.example.messmanagementsystem.ui.admin.AddDepositScreen
import com.example.messmanagementsystem.ui.admin.AddCostScreen
import com.example.messmanagementsystem.ui.admin.AllMembersScreen
import com.example.messmanagementsystem.ui.admin.ChangeManagerScreen
import com.example.messmanagementsystem.ui.admin.StartNewMonthScreen
import com.example.messmanagementsystem.ui.admin.DeleteMessScreen
import com.example.messmanagementsystem.ui.admin.RemoveMemberScreen
import com.example.messmanagementsystem.ui.admin.AddMemberScreen
import com.example.messmanagementsystem.ui.admin.AddMealScreen
import com.example.messmanagementsystem.ui.admin.ActiveMonthDetailsScreen
import com.example.messmanagementsystem.ui.admin.AllMonthScreen
import com.example.messmanagementsystem.ui.user.UserPanelScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Start destination is splash if user is logged in, else login
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "splash"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            val authRepo = AuthRepository()
            val uiState by produceState<Pair<Boolean, User?>>(initialValue = Pair(true, null)) {
                authRepo.getCurrentUserDetails().fold(
                    onSuccess = { value = Pair(false, it) },
                    onFailure = { value = Pair(false, null) }
                )
            }

            LaunchedEffect(uiState) {
                if (!uiState.first) {
                    val user = uiState.second
                    if (user == null || user.uid.isEmpty()) {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                    } else if (user.messId == null) {
                        navController.navigate("mess_selection") { popUpTo("splash") { inclusive = true } }
                    } else if (user.statusInMess == "pending") {
                        navController.navigate("pending_approval") { popUpTo("splash") { inclusive = true } }
                    } else {
                        navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate("splash") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { 
                    navController.navigate("splash") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.popBackStack() }
            )
        }
        composable("mess_selection") {
            val authViewModel: AuthViewModel = viewModel()
            MessSelectionScreen(
                onCreateClick = { navController.navigate("create_mess") },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("mess_selection") { inclusive = true }
                    }
                }
            )
        }
        composable("create_mess") {
            CreateMessScreen(
                onSuccess = { 
                    navController.navigate("dashboard") {
                        popUpTo("mess_selection") { inclusive = true }
                        popUpTo("create_mess") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("join_mess") {
            JoinMessScreen(
                onSuccess = { 
                    navController.navigate("pending_approval") {
                        popUpTo("mess_selection") { inclusive = true }
                        popUpTo("join_mess") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("pending_approval") {
            val authViewModel: AuthViewModel = viewModel()
            PendingApprovalScreen(
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("pending_approval") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            val authViewModel: AuthViewModel = viewModel()
            DashboardScreen(
                onNavigateToAdmin = { navController.navigate("admin_panel") },
                onNavigateToUserPanel = { navController.navigate("user_panel") },
                onNavigateToMessSelection = { 
                    navController.navigate("mess_selection") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("user_panel") {
            UserPanelScreen(
                onBack = { navController.popBackStack() },
                onAllMembers = { navController.navigate("admin_all_members") },
                onActiveMonthDetails = { navController.navigate("admin_active_month_details") },
                onAllMonth = { navController.navigate("admin_all_month") },
                onLeaveMessSuccess = {
                    navController.navigate("mess_selection") {
                        popUpTo("user_panel") { inclusive = true }
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("admin_panel") {
            AdminPanelScreen(
                onBack = { navController.popBackStack() },
                onAddMember = { navController.navigate("admin_add_member") },
                onAddDeposit = { navController.navigate("admin_add_deposit") },
                onAddMeal = { navController.navigate("admin_add_meal") },
                onAddCost = { navController.navigate("admin_add_cost") },
                onActiveMonthDetails = { navController.navigate("admin_active_month_details") },
                onAllMembers = { navController.navigate("admin_all_members") },
                onChangeManager = { navController.navigate("admin_change_manager") },
                onStartNewMonth = { navController.navigate("admin_start_new_month") },
                onAllMonth = { navController.navigate("admin_all_month") },
                onDeleteMess = { navController.navigate("admin_delete_mess") },
                onRemoveMember = { navController.navigate("admin_remove_member") }
            )
        }
        composable("admin_add_member") { PlaceholderScreen(title = "Add Mess Member") }
        composable("admin_add_member") { AddMemberScreen(onBack = { navController.popBackStack() }) }
        composable("admin_add_deposit") { AddDepositScreen(onBack = { navController.popBackStack() }) }
        composable("admin_add_meal") { AddMealScreen(onBack = { navController.popBackStack() }) }
        composable("admin_add_cost") { AddCostScreen(onBack = { navController.popBackStack() }) }
        composable("admin_active_month_details") { ActiveMonthDetailsScreen(onBack = { navController.popBackStack() }) }
        composable("admin_all_members") {
            AllMembersScreen(
                onBack = { navController.popBackStack() },
                onSelectMemberForManager = { /* navigate to change manager with selected? keeping simple */ }
            )
        }
        composable("admin_change_manager") {
            ChangeManagerScreen(onBack = { navController.popBackStack() })
        }
        composable("admin_remove_member") {
            RemoveMemberScreen(onBack = { navController.popBackStack() })
        }
        composable("admin_start_new_month") { StartNewMonthScreen(onBack = { navController.popBackStack() }) }
        composable("admin_all_month") { AllMonthScreen(onBack = { navController.popBackStack() }) }
        composable("admin_delete_mess") { DeleteMessScreen(onBack = { navController.popBackStack() }) }
    }
}
