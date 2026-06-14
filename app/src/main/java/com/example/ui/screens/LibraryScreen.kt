package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: com.example.ui.MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("folder") } // "folder" or "material"
    var newItemName by remember { mutableStateOf("") }
    var newItemFormat by remember { mutableStateOf("pdf") } // "pdf" or "doc"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add to Vault")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Knowledge Vault",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Your personal offline dataset",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search vault protocols...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "RECENT ARCHIVES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val filteredItems = vaultItems.filter { it.title.contains(searchQuery, ignoreCase = true) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    val containerColor = when(item.iconType) {
                        "pdf" -> MaterialTheme.colorScheme.primaryContainer
                        "doc" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                    val contentColor = when(item.iconType) {
                        "pdf" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "doc" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                    val icon = when(item.iconType) {
                        "pdf" -> Icons.Default.PictureAsPdf
                        "doc" -> Icons.Default.Description
                        else -> Icons.Default.Folder
                    }
                    VaultCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        icon = icon,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        onDelete = { viewModel.deleteVaultItem(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (addType == "folder") "Create Folder" else "Import Material") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = addType == "folder",
                            onClick = { addType = "folder" },
                            label = { Text("Folder") },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                        )
                        FilterChip(
                            selected = addType == "material",
                            onClick = { addType = "material" },
                            label = { Text("Material") },
                            leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (addType == "material") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Format: ")
                            RadioButton(selected = newItemFormat == "pdf", onClick = { newItemFormat = "pdf" })
                            Text("PDF")
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(selected = newItemFormat == "doc", onClick = { newItemFormat = "doc" })
                            Text("DOC")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            if (addType == "folder") {
                                viewModel.addVaultFolder(newItemName)
                            } else {
                                viewModel.importVaultMaterial(newItemName, newItemFormat)
                            }
                            newItemName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun VaultCard(
    title: String, 
    subtitle: String, 
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = contentColor)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = contentColor.copy(alpha = 0.7f))
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
