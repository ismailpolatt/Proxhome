package com.example.api.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val data: T
)

@JsonClass(generateAdapter = true)
data class ClusterResource(
    val id: String, // e.g., "node/pve", "qemu/100", "lxc/201"
    val type: String, // "node", "qemu", "lxc", "storage"
    val node: String,
    val vmid: Int? = null,
    val name: String? = null,
    val status: String? = null,
    val cpu: Double? = null,
    val maxcpu: Double? = null,
    val mem: Long? = null,
    val maxmem: Long? = null,
    val disk: Long? = null,
    val maxdisk: Long? = null,
    val uptime: Long? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val ticket: String,
    val CSRFPreventionToken: String,
    val username: String
)

@JsonClass(generateAdapter = true)
data class ClusterTask(
    val node: String,
    val user: String,
    val starttime: Long,
    val endtime: Long? = null,
    val status: String? = null,
    val type: String, // "qstart", "qstop", "vzstart", etc.
    val upid: String
)
