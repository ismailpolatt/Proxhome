package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.models.ClusterResource
import com.example.api.models.ClusterTask
import com.example.data.AppDatabase
import com.example.data.ProxmoxRepository
import com.example.data.ProxmoxServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ClusterUiState {
    object Idle : ClusterUiState
    object Loading : ClusterUiState
    data class Success(
        val nodes: List<ClusterResource>,
        val vms: List<ClusterResource>,
        val lxcs: List<ClusterResource>,
        val storages: List<ClusterResource>,
        val totalCpuCores: Double,
        val cpuUsagePct: Double,
        val totalMemory: Long,
        val memUsagePct: Double,
        val totalStorage: Long,
        val storageUsagePct: Double
    ) : ClusterUiState
    data class Error(val message: String) : ClusterUiState
}

sealed interface TasksUiState {
    object Loading : TasksUiState
    data class Success(val tasks: List<ClusterTask>) : TasksUiState
    data class Error(val message: String) : TasksUiState
}

class ProxmoxViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProxmoxRepository

    val servers: StateFlow<List<ProxmoxServer>>

    var selectedServer by mutableStateOf<ProxmoxServer?>(null)
        private set

    var clusterUiState by mutableStateOf<ClusterUiState>(ClusterUiState.Idle)
        private set

    var tasksUiState by mutableStateOf<TasksUiState>(TasksUiState.Loading)
        private set

    // Persistent Settings
    private val prefs = application.getSharedPreferences("proxmox_settings", android.content.Context.MODE_PRIVATE)

    var isLiveMode by mutableStateOf(prefs.getBoolean("live_polling", false))
        private set

    var pollingIntervalSeconds by mutableStateOf(prefs.getInt("polling_interval", 10)) // 5, 10, 15, or 30
        private set

    var themeSetting by mutableStateOf(prefs.getString("theme", "System") ?: "System")
        private set
    var timeoutSetting by mutableStateOf(prefs.getString("timeout", "5s") ?: "5s")
        private set
    var is24hTimeSetting by mutableStateOf(prefs.getBoolean("24h_time", true))
        private set
    var dateFormatSetting by mutableStateOf(prefs.getString("date_format", "System (d.MM.y)") ?: "System (d.MM.y)")
        private set
    var showGuestNodeNamesSetting by mutableStateOf(prefs.getBoolean("show_guest_node_names", false))
        private set
    var showResourceCountsSetting by mutableStateOf(prefs.getBoolean("show_resource_counts", true))
        private set
    var groupServersByStatusSetting by mutableStateOf(prefs.getBoolean("group_servers_by_status", true))
        private set
    var appLockEnabledSetting by mutableStateOf(prefs.getBoolean("app_lock_enabled", false))
        private set

    fun updateThemeSetting(value: String) {
        themeSetting = value
        prefs.edit().putString("theme", value).apply()
    }
    fun updateTimeoutSetting(value: String) {
        timeoutSetting = value
        prefs.edit().putString("timeout", value).apply()
    }
    fun updateIs24hTimeSetting(value: Boolean) {
        is24hTimeSetting = value
        prefs.edit().putBoolean("24h_time", value).apply()
    }
    fun updateDateFormatSetting(value: String) {
        dateFormatSetting = value
        prefs.edit().putString("date_format", value).apply()
    }
    fun updateShowGuestNodeNamesSetting(value: Boolean) {
        showGuestNodeNamesSetting = value
        prefs.edit().putBoolean("show_guest_node_names", value).apply()
    }
    fun updateShowResourceCountsSetting(value: Boolean) {
        showResourceCountsSetting = value
        prefs.edit().putBoolean("show_resource_counts", value).apply()
    }
    fun updateGroupServersByStatusSetting(value: Boolean) {
        groupServersByStatusSetting = value
        prefs.edit().putBoolean("group_servers_by_status", value).apply()
    }
    fun updateAppLockEnabledSetting(value: Boolean) {
        appLockEnabledSetting = value
        prefs.edit().putBoolean("app_lock_enabled", value).apply()
    }

    fun toggleLiveMode(enabled: Boolean) {
        isLiveMode = enabled
        prefs.edit().putBoolean("live_polling", enabled).apply()
        selectedServer?.let { startPolling(it) }
    }

    fun setPollingInterval(seconds: Int) {
        pollingIntervalSeconds = seconds
        prefs.edit().putInt("polling_interval", seconds).apply()
        selectedServer?.let { startPolling(it) }
    }

    // Active polling job
    private var pollingJob: Job? = null

    // Message notification channel for toasts/snackbar actions
    var actionMessage by mutableStateOf<String?>(null)
    var isExecutingAction by mutableStateOf(false)

    // Form states for adding/editing server
    var serverNameInput by mutableStateOf("")
    var serverUrlInput by mutableStateOf("")
    var usernameInput by mutableStateOf("")
    var authTypeInput by mutableStateOf("TOKEN") // "TOKEN" or "PASSWORD"
    var tokenNameInput by mutableStateOf("")
    var tokenValueInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var bypassSslInput by mutableStateOf(true)

    // Server test status
    var testConnectionStatus by mutableStateOf<String?>(null)
    var isTestingConnection by mutableStateOf(false)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProxmoxRepository(database.proxmoxDao())
        servers = repository.allServers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Automatically set up demo connection on first launch if servers is empty
        viewModelScope.launch {
            delay(500) // let database load
            if (servers.value.isEmpty()) {
                addDemoServer()
            }
        }
    }

    private suspend fun addDemoServer() {
        val demoServer = ProxmoxServer(
            name = "Demo Cluster (Simulated)",
            url = "https://127.0.0.1:8006",
            username = "root@pam",
            authType = "TOKEN",
            tokenName = "demo-token",
            tokenValue = "demo-secret",
            isDemo = true
        )
        repository.insertServer(demoServer)
    }

    fun selectServer(server: ProxmoxServer?) {
        selectedServer = server
        if (server == null) {
            clusterUiState = ClusterUiState.Idle
            pollingJob?.cancel()
        } else {
            if (server.isDemo) {
                repository.resetDemoState()
            }
            startPolling(server)
        }
    }

    private fun startPolling(server: ProxmoxServer) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            if (clusterUiState !is ClusterUiState.Success) {
                clusterUiState = ClusterUiState.Loading
            }
            fetchClusterData(server)
            fetchClusterTasks(server)
            
            while (isLiveMode) {
                delay(pollingIntervalSeconds * 1000L)
                fetchClusterData(server)
                fetchClusterTasks(server)
            }
        }
    }

    suspend fun fetchClusterData(server: ProxmoxServer) {
        try {
            val resources = repository.getClusterResources(server)
            
            val nodes = resources.filter { it.type == "node" }
            val vms = resources.filter { it.type == "qemu" }
            val lxcs = resources.filter { it.type == "lxc" }
            val storages = resources.filter { it.type == "storage" }

            // Aggregate metrics
            // Nodes aggregated CPU & Memory
            val onlineNodes = nodes.filter { it.status == "online" }
            
            var totalCpuCores = 0.0
            var aggregatedCpuUsedCores = 0.0
            onlineNodes.forEach {
                val maxCpu = it.maxcpu ?: 0.0
                val cpuUsed = it.cpu ?: 0.0
                totalCpuCores += maxCpu
                aggregatedCpuUsedCores += maxCpu * cpuUsed
            }
            val cpuUsagePct = if (totalCpuCores > 0) (aggregatedCpuUsedCores / totalCpuCores) * 100 else 0.0

            var totalMemory = 0L
            var usedMemory = 0L
            onlineNodes.forEach {
                totalMemory += it.maxmem ?: 0L
                usedMemory += it.mem ?: 0L
            }
            val memUsagePct = if (totalMemory > 0) (usedMemory.toDouble() / totalMemory.toDouble()) * 100 else 0.0

            // Storage aggregation
            var totalStorage = 0L
            var usedStorage = 0L
            storages.forEach {
                totalStorage += it.maxdisk ?: 0L
                usedStorage += it.disk ?: 0L
            }
            val storageUsagePct = if (totalStorage > 0) (usedStorage.toDouble() / totalStorage.toDouble()) * 100 else 0.0

            clusterUiState = ClusterUiState.Success(
                nodes = nodes,
                vms = vms,
                lxcs = lxcs,
                storages = storages,
                totalCpuCores = totalCpuCores,
                cpuUsagePct = cpuUsagePct,
                totalMemory = totalMemory,
                memUsagePct = memUsagePct,
                totalStorage = totalStorage,
                storageUsagePct = storageUsagePct
            )
        } catch (e: Exception) {
            // Only update error if we don't have past success data, or if we want to show it
            if (clusterUiState !is ClusterUiState.Success) {
                clusterUiState = ClusterUiState.Error(e.message ?: "Failed to connect to server.")
            } else {
                actionMessage = "Refresh failed: ${e.message}"
            }
        }
    }

    private suspend fun fetchClusterTasks(server: ProxmoxServer) {
        try {
            val tasks = repository.getClusterTasks(server)
            tasksUiState = TasksUiState.Success(tasks)
        } catch (e: Exception) {
            tasksUiState = TasksUiState.Error(e.message ?: "Failed to fetch task logs.")
        }
    }

    fun executeAction(
        node: String,
        vmid: Int,
        type: String, // "qemu" or "lxc"
        action: String // "start", "shutdown", "reboot", "stop"
    ) {
        val server = selectedServer ?: return
        viewModelScope.launch {
            isExecutingAction = true
            actionMessage = "Executing $action on ID $vmid..."
            try {
                val success = repository.executeAction(server, node, vmid, type, action)
                if (success) {
                    actionMessage = "Successfully requested $action on ID $vmid."
                    // Instantly trigger refresh to reflect changes
                    fetchClusterData(server)
                    fetchClusterTasks(server)
                } else {
                    actionMessage = "Failed to request $action on ID $vmid."
                }
            } catch (e: Exception) {
                actionMessage = "Error: ${e.message}"
            } finally {
                isExecutingAction = false
            }
        }
    }

    fun deployLxcScript(
        node: String,
        vmid: Int,
        name: String,
        cores: Int,
        ramMb: Int,
        diskGb: Int,
        scriptName: String,
        onSuccess: () -> Unit
    ) {
        val server = selectedServer ?: return
        viewModelScope.launch {
            try {
                val success = repository.deployLxcScript(server, node, vmid, name, cores, ramMb, diskGb, scriptName)
                if (success) {
                    actionMessage = "LXC container created for $name on node $node."
                    fetchClusterData(server)
                    fetchClusterTasks(server)
                    onSuccess()
                }
            } catch (e: Exception) {
                actionMessage = "Deployment error: ${e.message}"
            }
        }
    }

    fun saveServer() {
        if (serverNameInput.isBlank() || serverUrlInput.isBlank() || usernameInput.isBlank()) {
            testConnectionStatus = "Please fill in all required fields."
            return
        }

        // Clean up URL
        var cleanUrl = serverUrlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        val newServer = ProxmoxServer(
            name = serverNameInput.trim(),
            url = cleanUrl,
            username = usernameInput.trim(),
            authType = authTypeInput,
            tokenName = tokenNameInput.trim(),
            tokenValue = tokenValueInput.trim(),
            password = passwordInput.trim(),
            bypassSsl = bypassSslInput
        )

        viewModelScope.launch {
            repository.insertServer(newServer)
            clearServerForm()
        }
    }

    fun deleteServer(server: ProxmoxServer) {
        viewModelScope.launch {
            if (selectedServer?.id == server.id) {
                selectServer(null)
            }
            repository.deleteServer(server)
        }
    }

    fun testConnection() {
        if (serverUrlInput.isBlank() || usernameInput.isBlank()) {
            testConnectionStatus = "URL and Username are required to test connection."
            return
        }

        var cleanUrl = serverUrlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        val tempServer = ProxmoxServer(
            name = "Test",
            url = cleanUrl,
            username = usernameInput.trim(),
            authType = authTypeInput,
            tokenName = tokenNameInput.trim(),
            tokenValue = tokenValueInput.trim(),
            password = passwordInput.trim(),
            bypassSsl = bypassSslInput
        )

        viewModelScope.launch {
            isTestingConnection = true
            testConnectionStatus = "Connecting..."
            try {
                // Fetch resource as a test
                val res = repository.getClusterResources(tempServer)
                testConnectionStatus = "Success! Connected. Found ${res.size} cluster resources."
            } catch (e: Exception) {
                testConnectionStatus = "Connection Failed: ${e.message}"
            } finally {
                isTestingConnection = false
            }
        }
    }

    fun clearServerForm() {
        serverNameInput = ""
        serverUrlInput = ""
        usernameInput = ""
        authTypeInput = "TOKEN"
        tokenNameInput = ""
        tokenValueInput = ""
        passwordInput = ""
        bypassSslInput = true
        testConnectionStatus = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
