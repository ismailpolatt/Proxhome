package com.example.api

import com.example.api.models.ApiResponse
import com.example.api.models.ClusterResource
import com.example.api.models.ClusterTask
import com.example.api.models.LoginResponse
import retrofit2.http.*

interface ProxmoxApi {

    @FormUrlEncoded
    @POST("access/ticket")
    suspend fun getTicket(
        @Field("username") username: String,
        @Field("password") password: String
    ): ApiResponse<LoginResponse>

    @GET("cluster/resources")
    suspend fun getClusterResources(
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<List<ClusterResource>>

    @GET("cluster/tasks")
    suspend fun getClusterTasks(
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<List<ClusterTask>>

    // VM qemu Status Actions
    @POST("nodes/{node}/qemu/{vmid}/status/start")
    suspend fun startVm(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/qemu/{vmid}/status/shutdown")
    suspend fun shutdownVm(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/qemu/{vmid}/status/reboot")
    suspend fun rebootVm(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/qemu/{vmid}/status/stop")
    suspend fun stopVm(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    // LXC Status Actions
    @POST("nodes/{node}/lxc/{vmid}/status/start")
    suspend fun startLxc(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/lxc/{vmid}/status/shutdown")
    suspend fun shutdownLxc(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/lxc/{vmid}/status/reboot")
    suspend fun rebootLxc(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>

    @POST("nodes/{node}/lxc/{vmid}/status/stop")
    suspend fun stopLxc(
        @Path("node") node: String,
        @Path("vmid") vmid: Int,
        @Header("Authorization") token: String? = null,
        @Header("CSRFPreventionToken") csrfToken: String? = null,
        @Header("Cookie") ticketCookie: String? = null
    ): ApiResponse<String>
}
