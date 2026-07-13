package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxmoxDao {
    @Query("SELECT * FROM proxmox_servers ORDER BY createdAt DESC")
    fun getAllServers(): Flow<List<ProxmoxServer>>

    @Query("SELECT * FROM proxmox_servers WHERE id = :id")
    suspend fun getServerById(id: Int): ProxmoxServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ProxmoxServer): Long

    @Update
    suspend fun updateServer(server: ProxmoxServer)

    @Delete
    suspend fun deleteServer(server: ProxmoxServer)
}
