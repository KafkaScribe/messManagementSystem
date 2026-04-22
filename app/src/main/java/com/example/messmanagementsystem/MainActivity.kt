package com.example.messmanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.messmanagementsystem.ui.navigation.AppNavigation
import com.example.messmanagementsystem.ui.theme.MessManagementSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessManagementSystemTheme {
                AppNavigation()
            }
        }
    }
}