package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxmox_servers")
data class ProxmoxServer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val username: String,
    val tokenName: String = "",
    val tokenValue: String = "",
    val password: String = "",
    val authType: String, // "TOKEN" or "PASSWORD"
    val bypassSsl: Boolean = true,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
