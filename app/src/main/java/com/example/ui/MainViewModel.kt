package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.OpenRouterMessage
import com.example.api.OpenRouterRequest
import com.example.api.RetrofitClient
import com.example.data.BlfParser
import com.example.data.BlfScheduleDay
import com.example.data.Task
import com.example.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.example.data.UserPreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val taskDao: TaskDao,
    private val userPreferencesRepo: UserPreferencesRepo
) : ViewModel() {

    data class VaultItem(
        val id: String,
        val title: String,
        val subtitle: String,
        val isFolder: Boolean,
        val iconType: String
    )

    val userName: StateFlow<String?> = userPreferencesRepo.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val openRouterKey: StateFlow<String?> = userPreferencesRepo.openRouterKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    private val _scheduleDays = MutableStateFlow<List<BlfScheduleDay>>(emptyList())
    val scheduleDays: StateFlow<List<BlfScheduleDay>> = _scheduleDays.asStateFlow()

    fun saveUserName(name: String) {
        viewModelScope.launch {
            userPreferencesRepo.saveUserName(name)
        }
    }
    
    fun saveOpenRouterKey(key: String) {
        viewModelScope.launch {
            userPreferencesRepo.saveOpenRouterKey(key)
        }
    }
    
    val blfBackupContent: StateFlow<String?> = userPreferencesRepo.blfBackupFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    fun importBlfBackup(content: String) {
        viewModelScope.launch {
            userPreferencesRepo.saveBlfBackup(content)
            withContext(Dispatchers.Default) {
                val parsedTasks = BlfParser.parseTasks(content)
                parsedTasks.forEach { 
                    taskDao.insertTask(Task(title = it, category = "Imported"))
                }
                val name = BlfParser.parseName(content)
                if (name != null) saveUserName(name)
            }
        }
    }
    
    init {
        viewModelScope.launch {
            userPreferencesRepo.blfBackupFlow.collect { content ->
                if (!content.isNullOrBlank()) {
                    withContext(Dispatchers.Default) {
                        val days = BlfParser.parseSchedule(content)
                        _scheduleDays.value = days
                    }
                }
            }
        }
    }

    fun generateBLFBackup(): String {
        val backupString = """
            # Beyond Limit Format (.blf)
            # Version 1.0

            app: Beyond Limit
            version: 1.0

            schedule:
              date: 2026-06-14
              day: Sunday

            user:
              name: ${userName.value ?: "User"}

            overview:
              wake_up: "06:00"
              sleep: "22:00"
              total_study_time: "7h 30m"
              focus_sessions: 6
              productivity_goal: "90%"

            tasks:
              pending:
                - Complete Maths Worksheet Q1–5
                - Finish Lab Activity 1
                - Revise Science
                - Read English Chapter 1

              completed: []

            focus:
              preset: 50-10
              auto_cycle: true
              total_cycles: 6
              break_after: 50m
              break_duration: 10m
              long_break_after: 3
              long_break_duration: 30m

            ai:
              generated: true
              confidence: 97%
              optimize_schedule: true
              adaptive_timer: true
        """.trimIndent()
        viewModelScope.launch {
            userPreferencesRepo.saveBlfBackup(backupString)
        }
        return backupString
    }

    // --- State ---
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _focusTimeRemaining = MutableStateFlow(50 * 60) // 50 minutes config
    val focusTimeRemaining = _focusTimeRemaining.asStateFlow()

    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive = _isFocusModeActive.asStateFlow()

    val currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    // --- Vault State ---
    private val _vaultItems = MutableStateFlow<List<VaultItem>>(
        listOf(
            VaultItem("1", "Neural Architecture", "7.2 MB", false, "pdf"),
            VaultItem("2", "Quantum Dynamics", "12 MB", false, "doc"),
            VaultItem("3", "Psychology 101", "0 items", true, "folder"),
            VaultItem("4", "Calculus III", "0 items", true, "folder")
        )
    )
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems.asStateFlow()

    fun addVaultFolder(name: String) {
        val newItem = VaultItem(java.util.UUID.randomUUID().toString(), name, "0 items", true, "folder")
        _vaultItems.value = _vaultItems.value + newItem
    }

    fun importVaultMaterial(name: String, format: String) {
        val iconType = if (format.equals("pdf", ignoreCase = true)) "pdf" else "doc"
        val newItem = VaultItem(java.util.UUID.randomUUID().toString(), name, "1.4 MB", false, iconType)
        _vaultItems.value = _vaultItems.value + newItem
    }

    fun deleteVaultItem(id: String) {
        _vaultItems.value = _vaultItems.value.filter { it.id != id }
    }

    // --- Task Actions ---
    fun addTask(title: String, category: String = "Study") {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(Task(title = title, category = category))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTaskById(task.id)
        }
    }

    // --- Focus actions ---
    fun toggleFocusMode() {
        _isFocusModeActive.value = !_isFocusModeActive.value
        if (_isFocusModeActive.value) {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        viewModelScope.launch {
            while (_isFocusModeActive.value && _focusTimeRemaining.value > 0) {
                delay(1000)
                _focusTimeRemaining.value -= 1
                if (_focusTimeRemaining.value <= 0) {
                    _isFocusModeActive.value = false
                    _focusTimeRemaining.value = 10 * 60 // 10 min break
                }
            }
        }
    }

    fun resetFocusTimer() {
        _isFocusModeActive.value = false
        _focusTimeRemaining.value = 50 * 60
    }

    // --- AI Assistant ---
    fun askAiAssistant(query: String, useHighThinking: Boolean = false) {
        if (query.isBlank()) return
        _isAiLoading.value = true
        _aiResponse.value = null

        viewModelScope.launch {
            try {
                val key = openRouterKey.value
                if (key.isNullOrEmpty()) {
                    _aiResponse.value = "Please configure your OpenRouter API Key in the Library tab."
                    _isAiLoading.value = false
                    return@launch
                }
                
                val modelToUse = if (useHighThinking) "nvidia/nemotron-3-ultra-550b-a55b:free" else "qwen/qwen3-next-80b-a3b-instruct:free"

                val request = OpenRouterRequest(
                    model = modelToUse,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = "You are Beyond Limit, a highly intelligent offline-first study and productivity AI assistant designed for students and professionals. Keep responses concise, informative, motivating, and strictly structured for easy scanning. Use lists and bold key phrases. No pleasantries."),
                        OpenRouterMessage(role = "user", content = query)
                    )
                )

                val response = RetrofitClient.service.generateContent(
                    authorization = "Bearer $key",
                    referer = "https://aistudio.google.com",
                    title = "Beyond Limit",
                    request = request
                )

                val text = response.choices?.firstOrNull()?.message?.content
                _aiResponse.value = text ?: "No response from AI."
            } catch (e: Exception) {
                _aiResponse.value = "Error: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}

class MainViewModelFactory(
    private val taskDao: TaskDao,
    private val userPreferencesRepo: UserPreferencesRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(taskDao, userPreferencesRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
