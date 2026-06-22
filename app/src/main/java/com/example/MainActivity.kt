package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.OrganizerRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.OrganizerDashboard
import com.example.ui.OrganizerViewModel
import com.example.ui.OrganizerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Android Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = OrganizerRepository(database.organizerDao())

        // Initialize ViewModel
        val factory = OrganizerViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[OrganizerViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OrganizerDashboard(viewModel = viewModel)
            }
        }
    }
}

