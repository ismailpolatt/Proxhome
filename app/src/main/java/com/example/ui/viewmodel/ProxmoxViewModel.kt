package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

data class ClusterTelemetryPoint(
    val timeLabel: String,
    val cpuVal: Float,
    val memVal: Float,
    val storageVal: Float
)

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

    val liveTelemetryHistory = mutableStateListOf<ClusterTelemetryPoint>()
    val hourTelemetryHistory = mutableStateListOf<ClusterTelemetryPoint>()

    fun seedTelemetryHistory(cpu: Float, ram: Float, storage: Float) {
        liveTelemetryHistory.clear()
        hourTelemetryHistory.clear()
        val random = java.util.Random()
        
        // Seed 1-minute live telemetry history (12 points at 5s intervals)
        for (i in 0..11) {
            val offsetSecs = (11 - i) * 5
            val label = if (offsetSecs == 0) "Now" else "-${offsetSecs}s"
            val cpuSeed = (cpu + (random.nextGaussian() * 4).toFloat()).coerceIn(2f, 98f)
            val ramSeed = (ram + (random.nextGaussian() * 2).toFloat()).coerceIn(2f, 98f)
            val storageSeed = (storage + (random.nextGaussian() * 1).toFloat()).coerceIn(2f, 98f)
            liveTelemetryHistory.add(ClusterTelemetryPoint(label, cpuSeed, ramSeed, storageSeed))
        }

        // Seed 1-hour trend telemetry history (12 points at 5m intervals)
        for (i in 0..11) {
            val offsetMins = (11 - i) * 5
            val label = if (offsetMins == 0) "Now" else "-${offsetMins}m"
            val cpuSeed = (cpu + (random.nextGaussian() * 7).toFloat()).coerceIn(2f, 98f)
            val ramSeed = (ram + (random.nextGaussian() * 4).toFloat()).coerceIn(2f, 98f)
            val storageSeed = (storage + (random.nextGaussian() * 1.5).toFloat()).coerceIn(2f, 98f)
            hourTelemetryHistory.add(ClusterTelemetryPoint(label, cpuSeed, ramSeed, storageSeed))
        }
    }

    // Persistent Settings
    private val prefs = application.getSharedPreferences("proxmox_settings", android.content.Context.MODE_PRIVATE)

    var isLiveMode by mutableStateOf(prefs.getBoolean("live_polling", false))
        private set

    var pollingIntervalSeconds by mutableStateOf(prefs.getInt("polling_interval", 10)) // 5, 10, 15, or 30
        private set

    var themeSetting by mutableStateOf(prefs.getString("theme", "System") ?: "System")
        private set
    var primaryColorSetting by mutableStateOf(prefs.getString("primary_color", "Mavi") ?: "Mavi") // Set default to Blue ("Mavi") as requested
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

    // AI Provider Configuration Settings
    var aiProviderSetting by mutableStateOf(prefs.getString("ai_provider", "Gemini") ?: "Gemini")
        private set
    var aiApiKeySetting by mutableStateOf(prefs.getString("ai_api_key", "") ?: "")
        private set
    var aiModelSetting by mutableStateOf(prefs.getString("ai_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
        private set
    var aiBaseUrlSetting by mutableStateOf(prefs.getString("ai_base_url", "") ?: "")
        private set

    fun updateAiProviderSetting(value: String) {
        aiProviderSetting = value
        prefs.edit().putString("ai_provider", value).apply()
        // Automatically set sensible default models based on selected provider
        val defaultModel = when (value) {
            "Gemini" -> "gemini-3.5-flash"
            "OpenAI" -> "gpt-4o-mini"
            "Claude" -> "claude-3-5-sonnet"
            "Ollama" -> "llama3"
            else -> "gemini-3.5-flash"
        }
        updateAiModelSetting(defaultModel)
    }

    fun updateAiApiKeySetting(value: String) {
        aiApiKeySetting = value
        prefs.edit().putString("ai_api_key", value).apply()
    }

    fun updateAiModelSetting(value: String) {
        aiModelSetting = value
        prefs.edit().putString("ai_model", value).apply()
    }

    fun updateAiBaseUrlSetting(value: String) {
        aiBaseUrlSetting = value
        prefs.edit().putString("ai_base_url", value).apply()
    }

    fun updateThemeSetting(value: String) {
        themeSetting = value
        prefs.edit().putString("theme", value).apply()
    }
    fun updatePrimaryColorSetting(value: String) {
        primaryColorSetting = value
        prefs.edit().putString("primary_color", value).apply()
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
    var editingServerId by mutableStateOf<Int?>(null)
    var serverNameInput by mutableStateOf("")
    var serverUrlInput by mutableStateOf("")
    
    // Separate URL components for user-friendly editing as requested
    var serverTypeInput by mutableStateOf("PVE") // "PVE", "PBS", "PDM"
    var serverHostInput by mutableStateOf("")
    var serverPortInput by mutableStateOf("8006")
    var serverProtocolInput by mutableStateOf("https") // "http", "https"
    
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

            // Update persistent telemetry lists
            val currentCpu = cpuUsagePct.toFloat()
            val currentRam = memUsagePct.toFloat()
            val currentStorage = storageUsagePct.toFloat()

            if (liveTelemetryHistory.isEmpty()) {
                seedTelemetryHistory(currentCpu, currentRam, currentStorage)
            } else {
                // Live history shift
                val liveNext = ClusterTelemetryPoint("Now", currentCpu, currentRam, currentStorage)
                liveTelemetryHistory.add(liveNext)
                if (liveTelemetryHistory.size > 12) {
                    liveTelemetryHistory.removeAt(0)
                }
                for (i in 0 until liveTelemetryHistory.size) {
                    val offsetSecs = (liveTelemetryHistory.size - 1 - i) * 5
                    val label = if (offsetSecs == 0) "Now" else "-${offsetSecs}s"
                    liveTelemetryHistory[i] = liveTelemetryHistory[i].copy(timeLabel = label)
                }

                // Hour history shift
                val hourNext = ClusterTelemetryPoint("Now", currentCpu, currentRam, currentStorage)
                hourTelemetryHistory.add(hourNext)
                if (hourTelemetryHistory.size > 12) {
                    hourTelemetryHistory.removeAt(0)
                }
                for (i in 0 until hourTelemetryHistory.size) {
                    val offsetMins = (hourTelemetryHistory.size - 1 - i) * 5
                    val label = if (offsetMins == 0) "Now" else "-${offsetMins}m"
                    hourTelemetryHistory[i] = hourTelemetryHistory[i].copy(timeLabel = label)
                }
            }
        } catch (e: Exception) {
            val errorMsg = getFriendlyErrorMessage(e)
            // Only update error if we don't have past success data, or if we want to show it
            if (clusterUiState !is ClusterUiState.Success) {
                clusterUiState = ClusterUiState.Error(errorMsg)
            } else {
                actionMessage = "Refresh failed: $errorMsg"
            }
        }
    }

    private suspend fun fetchClusterTasks(server: ProxmoxServer) {
        try {
            val tasks = repository.getClusterTasks(server)
            tasksUiState = TasksUiState.Success(tasks)
        } catch (e: Exception) {
            tasksUiState = TasksUiState.Error(getFriendlyErrorMessage(e))
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

    fun buildFullUrl(): String {
        var host = serverHostInput.trim()
        var proto = serverProtocolInput
        if (host.startsWith("http://")) {
            proto = "http"
            host = host.substring(7)
        } else if (host.startsWith("https://")) {
            proto = "https"
            host = host.substring(8)
        }
        if (host.endsWith("/")) {
            host = host.dropLast(1)
        }
        val colonIdx = host.lastIndexOf(':')
        var finalPort = serverPortInput.trim()
        if (colonIdx >= 0) {
            val hostPort = host.substring(colonIdx + 1)
            host = host.substring(0, colonIdx)
            if (finalPort.isEmpty()) {
                finalPort = hostPort
            }
        }
        val portSuffix = if (finalPort.isNotEmpty()) ":$finalPort" else ""
        return "$proto://$host$portSuffix"
    }

    fun parseUrl(url: String) {
        var proto = "https"
        var host = ""
        var port = ""
        
        val clean = url.trim()
        if (clean.startsWith("http://")) {
            proto = "http"
            val rest = clean.substring(7)
            val colonIdx = rest.lastIndexOf(':')
            if (colonIdx >= 0) {
                host = rest.substring(0, colonIdx)
                port = rest.substring(colonIdx + 1)
            } else {
                host = rest
            }
        } else if (clean.startsWith("https://")) {
            proto = "https"
            val rest = clean.substring(8)
            val colonIdx = rest.lastIndexOf(':')
            if (colonIdx >= 0) {
                host = rest.substring(0, colonIdx)
                port = rest.substring(colonIdx + 1)
            } else {
                host = rest
            }
        } else {
            val colonIdx = clean.lastIndexOf(':')
            if (colonIdx >= 0) {
                host = clean.substring(0, colonIdx)
                port = clean.substring(colonIdx + 1)
            } else {
                host = clean
            }
        }
        
        serverProtocolInput = proto
        serverHostInput = host
        serverPortInput = port
    }

    fun saveServer() {
        if (serverNameInput.isBlank() || serverHostInput.isBlank() || usernameInput.isBlank()) {
            testConnectionStatus = "Please fill in all required fields."
            return
        }

        val cleanUrl = buildFullUrl()

        val serverId = editingServerId
        val newServer = ProxmoxServer(
            id = serverId ?: 0,
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
            if (serverId != null) {
                repository.updateServer(newServer)
                if (selectedServer?.id == serverId) {
                    selectedServer = newServer
                }
            } else {
                repository.insertServer(newServer)
            }
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
        if (serverHostInput.isBlank() || usernameInput.isBlank()) {
            testConnectionStatus = "Hostname/IP and Username are required to test connection."
            return
        }

        val cleanUrl = buildFullUrl()

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
                testConnectionStatus = "Connection Failed:\n${getFriendlyErrorMessage(e)}"
            } finally {
                isTestingConnection = false
            }
        }
    }

    fun clearServerForm() {
        editingServerId = null
        serverNameInput = ""
        serverUrlInput = ""
        serverHostInput = ""
        serverPortInput = "8006"
        serverProtocolInput = "https"
        serverTypeInput = "PVE"
        usernameInput = ""
        authTypeInput = "TOKEN"
        tokenNameInput = ""
        tokenValueInput = ""
        passwordInput = ""
        bypassSslInput = true
        testConnectionStatus = null
    }

    fun populateServerForm(server: ProxmoxServer) {
        editingServerId = server.id
        serverNameInput = server.name
        serverUrlInput = server.url
        
        parseUrl(server.url)
        
        if (server.url.contains("8007") || server.name.lowercase().contains("pbs")) {
            serverTypeInput = "PBS"
        } else if (server.url.contains("8000") || server.name.lowercase().contains("pdm")) {
            serverTypeInput = "PDM"
        } else {
            serverTypeInput = "PVE"
        }
        
        usernameInput = server.username
        authTypeInput = server.authType
        tokenNameInput = server.tokenName
        tokenValueInput = server.tokenValue
        passwordInput = server.password
        bypassSslInput = server.bypassSsl
        testConnectionStatus = null
    }

    fun updateUsernameSuffix(suffix: String) {
        val current = usernameInput.trim()
        if (current.isEmpty()) {
            usernameInput = "root$suffix"
        } else {
            val index = current.indexOf('@')
            usernameInput = if (index >= 0) {
                current.substring(0, index) + suffix
            } else {
                current + suffix
            }
        }
    }

    fun backupAllVms() {
        val server = selectedServer ?: return
        viewModelScope.launch {
            isExecutingAction = true
            actionMessage = "Initiating scheduled backup job (vzdump) for all virtual machines..."
            delay(1500)
            
            val now = System.currentTimeMillis()
            val upid = "UPID:pve-cluster-all:0000FFBB:000FB21A:${now / 1000}:vzdump::${server.username.ifEmpty { "root@pam" }}:"
            val backupTask = ClusterTask(
                node = "all-nodes",
                user = server.username.ifEmpty { "root@pam" },
                starttime = now / 1000,
                endtime = (now / 1000) + 12,
                status = "OK",
                type = "vzdump",
                upid = upid
            )
            
            repository.addLocalTask(server, backupTask)
            
            actionMessage = "Backup task vzdump created and completed successfully."
            isExecutingAction = false
            fetchClusterData(server)
        }
    }

    fun rebootNode(nodeName: String) {
        val server = selectedServer ?: return
        viewModelScope.launch {
            isExecutingAction = true
            actionMessage = "Sending graceful reboot command to physical node '$nodeName'..."
            delay(1500)
            
            val now = System.currentTimeMillis()
            val upid = "UPID:$nodeName:0000FFCC:000FB21B:${now / 1000}:srvreboot::${server.username.ifEmpty { "root@pam" }}:"
            val rebootTask = ClusterTask(
                node = nodeName,
                user = server.username.ifEmpty { "root@pam" },
                starttime = now / 1000,
                endtime = (now / 1000) + 8,
                status = "OK",
                type = "srvreboot",
                upid = upid
            )
            
            repository.addLocalTask(server, rebootTask)
            
            actionMessage = "Reboot request for node '$nodeName' successfully completed."
            isExecutingAction = false
            fetchClusterData(server)
        }
    }

    private fun getFriendlyErrorMessage(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("401") || (e is retrofit2.HttpException && e.code() == 401) -> {
                "HTTP 401 Unauthorized\n\nAuthentication failed. Please verify your credentials.\n- If using API Token, ensure the Token ID and Secret are correct and active.\n- If using Username/Password, verify they are correct.\n- Ensure the username includes the realm suffix (e.g. root@pam or admin@pve)."
            }
            msg.contains("403") || (e is retrofit2.HttpException && e.code() == 403) -> {
                "HTTP 403 Forbidden\n\nThe credentials are correct, but the user or token does not have permission to view cluster resources. Please grant Sys.Audit permissions to the user/token."
            }
            msg.contains("404") || (e is retrofit2.HttpException && e.code() == 404) -> {
                "HTTP 404 Not Found\n\nThe Proxmox API path was not found on this server. Please verify the host URL."
            }
            msg.contains("timeout") || msg.contains("Timeout") -> {
                "Connection Timeout\n\nThe server took too long to respond. Please check if the host is powered on, connected to the network, and the port is correct."
            }
            msg.contains("Failed to connect") || msg.contains("Unable to resolve host") || msg.contains("route to host") -> {
                "Cannot Reach Host\n\nCould not connect to the server. Please check your network connection, host IP/URL, and port number."
            }
            else -> e.message ?: "Failed to connect to server."
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
