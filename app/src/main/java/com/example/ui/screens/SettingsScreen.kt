package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: com.example.ui.MainViewModel,
    onNavigateBack: () -> Unit
) {
    var blfContent by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    val currentKey by viewModel.openRouterKey.collectAsStateWithLifecycle()
    
    LaunchedEffect(currentKey) {
        val key = currentKey
        if (!key.isNullOrBlank() && apiKey.isBlank()) {
            apiKey = key
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Preferences") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("AI Brain Link (OpenRouter Key)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-or-v1-...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveOpenRouterKey(apiKey) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Secure Link")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Import Master Schedule (.blf)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = blfContent,
                onValueChange = { blfContent = it },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text("Paste # Beyond Limit Format (.blf) code here to automatically construct schedule and database.") },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { 
                    viewModel.importBlfBackup(blfContent)
                    blfContent = ""
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Initialize Sequence")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
