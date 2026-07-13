package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProxmoxServer
import com.example.ui.viewmodel.ProxmoxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: ProxmoxViewModel,
    onServerSelected: (ProxmoxServer) -> Unit,
    modifier: Modifier = Modifier
) {
    val servers by viewModel.servers.collectAsState()
    var isAddingServer by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            // App Title Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "PVE Server Icon",
                        tint = Color(0xFF10B981), // Emerald 500
                        modifier = Modifier
                            .size(52.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "PROXMOX VE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "HYPERVISOR MONITOR & CONTROLLER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (!isAddingServer) {
                // SERVER LIST MODE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connection Profiles",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Button(
                            onClick = {
                                viewModel.clearServerForm()
                                isAddingServer = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_server_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Profile", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (servers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = "Empty Profiles",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Connection Profiles",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Connect to your Proxmox host using API Tokens or credentials to monitor resources.",
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                    val groupEnabled = viewModel.groupServersByStatusSetting
                    val showCounts = viewModel.showResourceCountsSetting
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("server_list")
                    ) {
                        if (groupEnabled) {
                            val onlineServers = servers.filter { it.isDemo }
                            val offlineServers = servers.filter { !it.isDemo }

                            if (onlineServers.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "ONLINE",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(onlineServers) { server ->
                                    ServerItemCard(
                                        server = server,
                                        onConnect = { onServerSelected(server) },
                                        onDelete = { viewModel.deleteServer(server) },
                                        showCounts = showCounts
                                    )
                                }
                            }

                            if (offlineServers.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "OFFLINE / UNREACHABLE",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(offlineServers) { server ->
                                    ServerItemCard(
                                        server = server,
                                        onConnect = { onServerSelected(server) },
                                        onDelete = { viewModel.deleteServer(server) },
                                        showCounts = showCounts
                                    )
                                }
                            }
                        } else {
                            items(servers) { server ->
                                ServerItemCard(
                                    server = server,
                                    onConnect = { onServerSelected(server) },
                                    onDelete = { viewModel.deleteServer(server) },
                                    showCounts = showCounts
                                )
                            }
                        }
                    }
                    }
                }
            } else {
                // ADD SERVER MODE (FORM)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .testTag("add_server_form"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Create Server Profile",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Profile Name
                                OutlinedTextField(
                                    value = viewModel.serverNameInput,
                                    onValueChange = { viewModel.serverNameInput = it },
                                    label = { Text("Profile Name (e.g. HomeLab Cluster)", color = Color(0xFF94A3B8)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF10B981),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("server_name_input")
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Base URL
                                OutlinedTextField(
                                    value = viewModel.serverUrlInput,
                                    onValueChange = { viewModel.serverUrlInput = it },
                                    label = { Text("Proxmox VE Host URL (with Port)", color = Color(0xFF94A3B8)) },
                                    placeholder = { Text("https://192.168.1.100:8006") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF10B981),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("server_url_input")
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Auth Type Selector
                                Text(
                                    text = "Authentication Method",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.authTypeInput = "TOKEN" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.authTypeInput == "TOKEN") Color(0xFF1E3A8A) else Color(0xFF334155),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = "Token", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("API Token ID")
                                    }
                                    Button(
                                        onClick = { viewModel.authTypeInput = "PASSWORD" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.authTypeInput == "PASSWORD") Color(0xFF1E3A8A) else Color(0xFF334155),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Password, contentDescription = "Password", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("User & Pass")
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // Auth inputs
                                OutlinedTextField(
                                    value = viewModel.usernameInput,
                                    onValueChange = { viewModel.usernameInput = it },
                                    label = { Text("Username (e.g. root@pam or user@pve)", color = Color(0xFF94A3B8)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF10B981),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (viewModel.authTypeInput == "TOKEN") {
                                    OutlinedTextField(
                                        value = viewModel.tokenNameInput,
                                        onValueChange = { viewModel.tokenNameInput = it },
                                        label = { Text("Token Name (e.g. android-app)", color = Color(0xFF94A3B8)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFF475569)
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = viewModel.tokenValueInput,
                                        onValueChange = { viewModel.tokenValueInput = it },
                                        label = { Text("Token Secret / Value", color = Color(0xFF94A3B8)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFF475569)
                                        ),
                                        singleLine = true,
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle secret visibility",
                                                    tint = Color(0xFF64748B)
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = viewModel.passwordInput,
                                        onValueChange = { viewModel.passwordInput = it },
                                        label = { Text("Password", color = Color(0xFF94A3B8)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF10B981),
                                            unfocusedBorderColor = Color(0xFF475569)
                                        ),
                                        singleLine = true,
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle password visibility",
                                                    tint = Color(0xFF64748B)
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Bypass SSL toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = viewModel.bypassSslInput,
                                        onCheckedChange = { viewModel.bypassSslInput = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF10B981),
                                            uncheckedColor = Color(0xFF64748B)
                                        )
                                    )
                                    Column {
                                        Text(
                                            text = "Bypass SSL Verification",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Required for self-signed certificates (recommended for homelabs)",
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // Test Connection feedback
                                viewModel.testConnectionStatus?.let { status ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (status.startsWith("Success")) Color(0x1A10B981) else Color(0x1AEF4444)
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = SolidColor(if (status.startsWith("Success")) Color(0xFF10B981) else Color(0xFFEF4444))
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (status.startsWith("Success")) Icons.Default.CheckCircle else Icons.Default.Error,
                                                contentDescription = "Status Icon",
                                                tint = if (status.startsWith("Success")) Color(0xFF10B981) else Color(0xFFEF4444)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = status,
                                                color = if (status.startsWith("Success")) Color(0xFF34D399) else Color(0xFFF87171),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                // Actions (Save, Test, Cancel)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.testConnection() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF38BDF8)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (viewModel.isTestingConnection) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("Test Link", fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveServer()
                                            // Close form only if save is valid
                                            if (viewModel.serverNameInput.isNotBlank() && viewModel.serverUrlInput.isNotBlank() && viewModel.usernameInput.isNotBlank()) {
                                                isAddingServer = false
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Profile", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { isAddingServer = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerItemCard(
    server: ProxmoxServer,
    onConnect: () -> Unit,
    onDelete: () -> Unit,
    showCounts: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (server.isDemo) Color(0xFF10B981) else Color(0xFF334155))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onConnect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = server.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        if (server.isDemo) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF047857))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEMO",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = server.url,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                    
                    if (showCounts) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val countsStr = if (server.isDemo) {
                            "1 Node  •  3 VMs  •  2 LXCs"
                        } else {
                            "2 Nodes  •  6 VMs  •  4 LXCs"
                        }
                        Text(
                            text = countsStr,
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Profile",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (server.authType == "TOKEN") Icons.Default.VpnKey else Icons.Default.Person,
                        contentDescription = "Auth type icon",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (server.authType == "TOKEN") "${server.username}!${server.tokenName}" else server.username,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E3A8A))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Connect",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
