package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.UltraInstinctApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val userPreferencesRepo by lazy { com.example.data.UserPreferencesRepo(this) }
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(database.taskDao(), userPreferencesRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UltraInstinctApp(viewModel = viewModel)
            }
        }
    }
}
