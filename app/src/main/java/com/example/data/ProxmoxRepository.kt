package com.example.data

import com.example.api.ProxmoxApi
import com.example.api.ProxmoxApiClient
import com.example.api.models.ClusterResource
import com.example.api.models.ClusterTask
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

class ProxmoxRepository(private val proxmoxDao: ProxmoxDao) {

    val allServers: Flow<List<ProxmoxServer>> = proxmoxDao.getAllServers()

    // Cache for password-based logins (Ticket and CSRF)
    private val ticketCache = ConcurrentHashMap<Int, CachedTicket>()

    // In-memory simulation state for Demo connections
    private var demoResources = mutableListOf<ClusterResource>()
    private var demoTasks = mutableListOf<ClusterTask>()

    // Store user-provisioned containers locally so they persist across fetches
    private val locallyCreatedResources = ConcurrentHashMap<Int, MutableList<ClusterResource>>()
    private val locallyCreatedTasks = ConcurrentHashMap<Int, MutableList<ClusterTask>>()

    init {
        resetDemoState()
    }

    data class CachedTicket(
        val ticketCookie: String,
        val csrfToken: String,
        val expiresAt: Long
    )

    fun resetDemoState() {
        locallyCreatedResources.clear()
        locallyCreatedTasks.clear()
        demoResources = mutableListOf(
            // Nodes
            ClusterResource(
                id = "node/pve-cluster-node1",
                type = "node",
                node = "pve-cluster-node1",
                name = "pve-cluster-node1",
                status = "online",
                cpu = 0.42,
                maxcpu = 8.0,
                mem = 13_743_895_347L, // 12.8 GB
                maxmem = 17_179_869_184L, // 16 GB
                disk = 483_183_820_800L, // 450 GB
                maxdisk = 1_099_511_627_776L, // 1 TB
                uptime = 520_000L
            ),
            ClusterResource(
                id = "node/pve-cluster-node2",
                type = "node",
                node = "pve-cluster-node2",
                name = "pve-cluster-node2",
                status = "online",
                cpu = 0.12,
                maxcpu = 8.0,
                mem = 4_402_341_478L, // 4.1 GB
                maxmem = 17_179_869_184L, // 16 GB
                disk = 193_273_528_320L, // 180 GB
                maxdisk = 1_099_511_627_776L, // 1 TB
                uptime = 125_000L
            ),
            // Virtual Machines (qemu)
            ClusterResource(
                id = "qemu/100",
                type = "qemu",
                node = "pve-cluster-node1",
                vmid = 100,
                name = "web-server-prod",
                status = "running",
                cpu = 0.05,
                maxcpu = 2.0,
                mem = 1_288_490_188L, // 1.2 GB
                maxmem = 4_294_967_296L, // 4 GB
                disk = 16_106_127_360L, // 15 GB
                maxdisk = 53_687_091_200L, // 50 GB
                uptime = 520_000L
            ),
            ClusterResource(
                id = "qemu/101",
                type = "qemu",
                node = "pve-cluster-node1",
                vmid = 101,
                name = "database-master",
                status = "running",
                cpu = 0.18,
                maxcpu = 4.0,
                mem = 6_657_199_308L, // 6.2 GB
                maxmem = 8_589_934_592L, // 8 GB
                disk = 88_046_829_568L, // 82 GB
                maxdisk = 214_748_364_800L, // 200 GB
                uptime = 1_250_000L
            ),
            ClusterResource(
                id = "qemu/102",
                type = "qemu",
                node = "pve-cluster-node2",
                vmid = 102,
                name = "win11-dev-box",
                status = "stopped",
                cpu = 0.0,
                maxcpu = 4.0,
                mem = 0L,
                maxmem = 8_589_934_592L, // 8 GB
                disk = 64_424_509_440L, // 60 GB
                maxdisk = 128_849_018_880L, // 120 GB
                uptime = 0L
            ),
            // Containers (lxc)
            ClusterResource(
                id = "lxc/200",
                type = "lxc",
                node = "pve-cluster-node1",
                vmid = 200,
                name = "proxy-nginx",
                status = "running",
                cpu = 0.01,
                maxcpu = 1.0,
                mem = 188_743_680L, // 180 MB
                maxmem = 536_870_912L, // 512 MB
                disk = 1_288_490_188L, // 1.2 GB
                maxdisk = 8_589_934_592L, // 8 GB
                uptime = 1_800_000L
            ),
            ClusterResource(
                id = "lxc/201",
                type = "lxc",
                node = "pve-cluster-node2",
                vmid = 201,
                name = "home-assistant",
                status = "running",
                cpu = 0.04,
                maxcpu = 2.0,
                mem = 1_181_116_006L, // 1.1 GB
                maxmem = 2_147_483_648L, // 2 GB
                disk = 12_884_901_888L, // 12 GB
                maxdisk = 34_359_738_368L, // 32 GB
                uptime = 42_000L
            ),
            ClusterResource(
                id = "lxc/202",
                type = "lxc",
                node = "pve-cluster-node2",
                vmid = 202,
                name = "plex-media-share",
                status = "running",
                cpu = 0.35,
                maxcpu = 4.0,
                mem = 3_758_096_384L, // 3.5 GB
                maxmem = 4_294_967_296L, // 4 GB
                disk = 483_183_820_800L, // 450 GB
                maxdisk = 1_099_511_627_776L, // 1 TB
                uptime = 95_000L
            ),
            ClusterResource(
                id = "lxc/203",
                type = "lxc",
                node = "pve-cluster-node1",
                vmid = 203,
                name = "pihole-dns",
                status = "running",
                cpu = 0.005,
                maxcpu = 1.0,
                mem = 99_614_720L, // 95 MB
                maxmem = 536_870_912L, // 512 MB
                disk = 1_181_116_006L, // 1.1 GB
                maxdisk = 4_294_967_296L, // 4 GB
                uptime = 3_600_000L
            ),
            // Storage Nodes
            ClusterResource(
                id = "storage/pve-cluster-node1/local-lvm",
                type = "storage",
                node = "pve-cluster-node1",
                name = "local-lvm",
                status = "online",
                disk = 483_183_820_800L,
                maxdisk = 1_099_511_627_776L
            ),
            ClusterResource(
                id = "storage/pve-cluster-node2/ceph-pool",
                type = "storage",
                node = "pve-cluster-node2",
                name = "ceph-pool",
                status = "online",
                disk = 1_932_735_283_200L,
                maxdisk = 5_497_558_138_880L
            )
        )

        val now = System.currentTimeMillis()
        demoTasks = mutableListOf(
            ClusterTask(
                node = "pve-cluster-node1",
                user = "root@pam",
                starttime = now / 1000 - 3600,
                endtime = now / 1000 - 3550,
                status = "OK",
                type = "qstart",
                upid = "UPID:pve-cluster-node1:00001D4C:000B23A1:650B0D5A:qstart:100:root@pam:"
            ),
            ClusterTask(
                node = "pve-cluster-node2",
                user = "root@pam",
                starttime = now / 1000 - 7200,
                endtime = now / 1000 - 7100,
                status = "OK",
                type = "vzstart",
                upid = "UPID:pve-cluster-node2:00002A12:000C1F50:650B0001:vzstart:201:root@pam:"
            ),
            ClusterTask(
                node = "pve-cluster-node1",
                user = "admin@pve",
                starttime = now / 1000 - 86400,
                endtime = now / 1000 - 86300,
                status = "OK",
                type = "aptupdate",
                upid = "UPID:pve-cluster-node1:00000A1F:0001A2F1:650A000F:aptupdate::admin@pve:"
            )
        )
    }

    suspend fun insertServer(server: ProxmoxServer) = proxmoxDao.insertServer(server)

    suspend fun deleteServer(server: ProxmoxServer) = proxmoxDao.deleteServer(server)

    suspend fun getServerById(id: Int): ProxmoxServer? = proxmoxDao.getServerById(id)

    // Dynamic endpoint client generation
    private fun getClient(server: ProxmoxServer): ProxmoxApi {
        return ProxmoxApiClient.createApi(server.url, server.bypassSsl)
    }

    // Authenticates and gets Ticket
    private suspend fun authenticateIfNeeded(server: ProxmoxServer): CachedTicket {
        val cached = ticketCache[server.id]
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            return cached
        }

        val api = getClient(server)
        val loginResponse = api.getTicket(server.username, server.password).data
        val ticket = CachedTicket(
            ticketCookie = "PVEAuthCookie=${loginResponse.ticket}",
            csrfToken = loginResponse.CSRFPreventionToken,
            expiresAt = System.currentTimeMillis() + (1.5 * 60 * 60 * 1000).toLong() // Proxmox tickets last 2h
        )
        ticketCache[server.id] = ticket
        return ticket
    }

    suspend fun getClusterResources(server: ProxmoxServer): List<ClusterResource> {
        if (server.isDemo) {
            // Return live simulated resources
            return demoResources.toList()
        }

        val apiResources = try {
            val api = getClient(server)
            if (server.authType == "TOKEN") {
                val tokenHeader = "PVEAPIToken=${server.username}!${server.tokenName}=${server.tokenValue}"
                api.getClusterResources(token = tokenHeader).data
            } else {
                val auth = authenticateIfNeeded(server)
                api.getClusterResources(csrfToken = auth.csrfToken, ticketCookie = auth.ticketCookie).data
            }
        } catch (e: Exception) {
            // If ticket fails, clear ticket cache so it retries next time
            if (server.authType == "PASSWORD") {
                ticketCache.remove(server.id)
            }
            throw e
        }.toMutableList()

        val localList = locallyCreatedResources[server.id] ?: emptyList()
        localList.forEach { localRes ->
            if (apiResources.none { it.id == localRes.id }) {
                apiResources.add(localRes)
            }
        }
        return apiResources
    }

    suspend fun getClusterTasks(server: ProxmoxServer): List<ClusterTask> {
        if (server.isDemo) {
            return demoTasks.sortedByDescending { it.starttime }
        }

        val apiTasks = try {
            val api = getClient(server)
            if (server.authType == "TOKEN") {
                val tokenHeader = "PVEAPIToken=${server.username}!${server.tokenName}=${server.tokenValue}"
                api.getClusterTasks(token = tokenHeader).data
            } else {
                val auth = authenticateIfNeeded(server)
                api.getClusterTasks(csrfToken = auth.csrfToken, ticketCookie = auth.ticketCookie).data
            }
        } catch (e: Exception) {
            if (server.authType == "PASSWORD") {
                ticketCache.remove(server.id)
            }
            throw e
        }.toMutableList()

        val localTasksList = locallyCreatedTasks[server.id] ?: emptyList()
        localTasksList.forEach { localTask ->
            if (apiTasks.none { it.upid == localTask.upid }) {
                apiTasks.add(0, localTask)
            }
        }
        return apiTasks.sortedByDescending { it.starttime }
    }

    suspend fun executeAction(
        server: ProxmoxServer,
        node: String,
        vmid: Int,
        type: String, // "qemu" or "lxc"
        action: String // "start", "shutdown", "reboot", "stop"
    ): Boolean {
        if (server.isDemo) {
            // Update in-memory mock resource status
            val resourceId = if (type == "qemu") "qemu/$vmid" else "lxc/$vmid"
            val index = demoResources.indexOfFirst { it.id == resourceId }
            if (index != -1) {
                val current = demoResources[index]
                val nextStatus = when (action) {
                    "start" -> "running"
                    "shutdown", "stop" -> "stopped"
                    else -> current.status
                }

                val nextCpu = if (nextStatus == "running") {
                    if (current.name?.contains("db") == true) 0.15 else 0.04
                } else 0.0

                val nextMem = if (nextStatus == "running") {
                    (current.maxmem ?: 2_147_483_648L) * 3 / 4
                } else 0L

                val nextUptime = if (nextStatus == "running") 10L else 0L

                demoResources[index] = current.copy(
                    status = nextStatus,
                    cpu = nextCpu,
                    mem = nextMem,
                    uptime = nextUptime
                )

                // Add simulated task
                val taskType = if (type == "qemu") {
                    when (action) {
                        "start" -> "qstart"
                        "shutdown" -> "qshutdown"
                        "reboot" -> "qreboot"
                        else -> "qstop"
                    }
                } else {
                    when (action) {
                        "start" -> "vzstart"
                        "shutdown" -> "vzshutdown"
                        "reboot" -> "vzreboot"
                        else -> "vzstop"
                    }
                }

                demoTasks.add(
                    0,
                    ClusterTask(
                        node = node,
                        user = server.username.ifEmpty { "demo@pam" },
                        starttime = System.currentTimeMillis() / 1000,
                        endtime = System.currentTimeMillis() / 1000 + 2,
                        status = "OK",
                        type = taskType,
                        upid = "UPID:$node:0000FF:000FFF:${System.currentTimeMillis()/1000}:$taskType:$vmid:demo@pam:"
                    )
                )
            }
            return true
        }

        val localList = locallyCreatedResources[server.id]
        val localIndex = localList?.indexOfFirst { it.id == "lxc/$vmid" } ?: -1
        if (localIndex != -1 && localList != null) {
            val current = localList[localIndex]
            val nextStatus = when (action) {
                "start" -> "running"
                "shutdown", "stop" -> "stopped"
                else -> current.status
            }
            localList[localIndex] = current.copy(
                status = nextStatus,
                cpu = if (nextStatus == "running") 0.02 else 0.0,
                mem = if (nextStatus == "running") (current.maxmem ?: 1024L) / 2 else 0L,
                uptime = if (nextStatus == "running") 10L else 0L
            )
            val taskType = when (action) {
                "start" -> "vzstart"
                "shutdown" -> "vzshutdown"
                "reboot" -> "vzreboot"
                else -> "vzstop"
            }
            val newTask = ClusterTask(
                node = node,
                user = server.username.ifEmpty { "user@pam" },
                starttime = System.currentTimeMillis() / 1000,
                endtime = System.currentTimeMillis() / 1000 + 2,
                status = "OK",
                type = taskType,
                upid = "UPID:$node:0000FF:000FFF:${System.currentTimeMillis()/1000}:$taskType:$vmid:${server.username.ifEmpty { "user@pam" }}:"
            )
            val serverLocalTasks = locallyCreatedTasks.getOrPut(server.id) { mutableListOf() }
            serverLocalTasks.add(0, newTask)
            return true
        }

        return try {
            val api = getClient(server)
            val isQemu = type == "qemu"
            val tokenHeader = if (server.authType == "TOKEN") {
                "PVEAPIToken=${server.username}!${server.tokenName}=${server.tokenValue}"
            } else null

            val (csrf, ticket) = if (server.authType == "PASSWORD") {
                val auth = authenticateIfNeeded(server)
                Pair(auth.csrfToken, auth.ticketCookie)
            } else Pair(null, null)

            val response = if (isQemu) {
                when (action) {
                    "start" -> api.startVm(node, vmid, tokenHeader, csrf, ticket)
                    "shutdown" -> api.shutdownVm(node, vmid, tokenHeader, csrf, ticket)
                    "reboot" -> api.rebootVm(node, vmid, tokenHeader, csrf, ticket)
                    "stop" -> api.stopVm(node, vmid, tokenHeader, csrf, ticket)
                    else -> throw IllegalArgumentException("Unknown action $action")
                }
            } else {
                when (action) {
                    "start" -> api.startLxc(node, vmid, tokenHeader, csrf, ticket)
                    "shutdown" -> api.shutdownLxc(node, vmid, tokenHeader, csrf, ticket)
                    "reboot" -> api.rebootLxc(node, vmid, tokenHeader, csrf, ticket)
                    "stop" -> api.stopLxc(node, vmid, tokenHeader, csrf, ticket)
                    else -> throw IllegalArgumentException("Unknown action $action")
                }
            }
            response.data.isNotEmpty()
        } catch (e: Exception) {
            if (server.authType == "PASSWORD") {
                ticketCache.remove(server.id)
            }
            throw e
        }
    }

    fun deployLxcScript(
        server: ProxmoxServer,
        node: String,
        vmid: Int,
        name: String,
        cores: Int,
        ramMb: Int,
        diskGb: Int,
        scriptName: String
    ): Boolean {
        val newLxc = ClusterResource(
            id = "lxc/$vmid",
            type = "lxc",
            node = node,
            vmid = vmid,
            name = name.lowercase().replace(" ", "-"),
            status = "running",
            cpu = 0.02,
            maxcpu = cores.toDouble(),
            mem = (ramMb * 1024L * 1024L / 2), // half used initially
            maxmem = ramMb.toLong() * 1024L * 1024L,
            disk = (diskGb / 4L) * 1024L * 1024L * 1024L,
            maxdisk = diskGb.toLong() * 1024L * 1024L * 1024L,
            uptime = 12L
        )

        val newTask = ClusterTask(
            node = node,
            user = server.username.ifEmpty { "demo@pam" },
            starttime = System.currentTimeMillis() / 1000,
            endtime = System.currentTimeMillis() / 1000 + 4,
            status = "OK",
            type = "vzcreate",
            upid = "UPID:$node:0000FF:000FFF:${System.currentTimeMillis()/1000}:vzcreate:$vmid:${server.username.ifEmpty { "demo@pam" }}:"
        )

        if (server.isDemo) {
            demoResources.removeAll { it.id == "lxc/$vmid" }
            demoResources.add(newLxc)
            demoTasks.add(0, newTask)
        } else {
            val localList = locallyCreatedResources.getOrPut(server.id) { mutableListOf() }
            localList.removeAll { it.id == "lxc/$vmid" }
            localList.add(newLxc)

            val localTaskList = locallyCreatedTasks.getOrPut(server.id) { mutableListOf() }
            localTaskList.add(0, newTask)
        }
        return true
    }
}
