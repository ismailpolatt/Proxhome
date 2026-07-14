package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
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
    var showAdvancedOptions by remember { mutableStateOf(false) }
    
    var isShowingSettings by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val darkSlateBg = Color(0xFF0F172A)
    val obsidianBg = Color(0xFF020617)
    val proxmoxOrange = when (viewModel.primaryColorSetting) {
        "Mavi" -> Color(0xFF0284C7) // Sky Blue accent
        "Yeşil" -> Color(0xFF059669) // Emerald Green accent
        "Mor" -> Color(0xFF7C3AED) // Purple/Violet accent
        else -> Color(0xFFEA580C) // "Turuncu" (Orange - standard Proxmox style)
    }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(darkSlateBg, obsidianBg)
    )

    // Automatically set form state if we are currently editing
    LaunchedEffect(viewModel.editingServerId) {
        if (viewModel.editingServerId != null) {
            isAddingServer = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        if (isShowingSettings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { isShowingSettings = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding()
            ) {
                if (!isAddingServer) {
                // SERVER LIST MODE HEADER (As requested: Logo + Title left, Refresh + Settings right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Custom puzzle-cross like icon matching logo
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(proxmoxOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "ProxMan Logo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ProxMan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Refresh Button
                        IconButton(
                            onClick = {
                                // Re-trigger data sync or list refresh if applicable
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings Button (Opens OptionsTab in beautiful Dialog)
                        IconButton(
                            onClick = { isShowingSettings = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Subtitle: Çevrimici Sunucular
                Text(
                    text = "Çevrimici Sunucular",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                )

                // Server list display
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
                                text = "Sunucu Bulunamadı",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "İzlemek ve yönetmek için API Token veya kimlik bilgileriyle yeni bir sunucu ekleyin.",
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    val showCounts = viewModel.showResourceCountsSetting
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .testTag("server_list")
                    ) {
                        items(servers) { server ->
                            ServerItemCard(
                                server = server,
                                onConnect = { onServerSelected(server) },
                                onEdit = {
                                    viewModel.populateServerForm(server)
                                    isAddingServer = true
                                },
                                onDelete = { viewModel.deleteServer(server) },
                                showCounts = showCounts,
                                accentColor = proxmoxOrange
                            )
                        }
                    }
                }

                // Add Server Bottom Button (Orange, styled exactly like Screenshot 2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.clearServerForm()
                            isAddingServer = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = proxmoxOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("add_server_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0x33FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ekle",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Yeni Sunucu Ekle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

            } else {
                // ADD / EDIT SERVER SCREEN (Styled exactly like Screenshot 1)
                
                // Top Custom Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button [ < Geri ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                viewModel.clearServerForm()
                                isAddingServer = false
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Geri", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    // Title
                    Text(
                        text = if (viewModel.editingServerId != null) "Sunucuyu Düzenle" else "Yeni Sunucu Ekle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )

                    // Help button [ ? ]
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Text("?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Add/Edit Form Fields Scrollable List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .testTag("add_server_form"),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // SECTION 1: Sunucu Türü (Server Type Selector: PVE on top, PBS & PDM row on bottom)
                    item {
                        Column {
                            Text(
                                text = "Sunucu Türü",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                            ) {
                                // PVE Button (Full Width Top)
                                val isPve = viewModel.serverTypeInput == "PVE"
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isPve) proxmoxOrange else Color.Transparent)
                                        .clickable {
                                            viewModel.serverTypeInput = "PVE"
                                            if (viewModel.serverPortInput == "8007" || viewModel.serverPortInput == "8000" || viewModel.serverPortInput.isEmpty()) {
                                                viewModel.serverPortInput = "8006"
                                            }
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Computer,
                                            contentDescription = "PVE",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("PVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                                // Row of PBS and PDM
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // PBS Button
                                    val isPbs = viewModel.serverTypeInput == "PBS"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isPbs) proxmoxOrange else Color.Transparent)
                                            .clickable {
                                                viewModel.serverTypeInput = "PBS"
                                                if (viewModel.serverPortInput == "8006" || viewModel.serverPortInput == "8000" || viewModel.serverPortInput.isEmpty()) {
                                                    viewModel.serverPortInput = "8007"
                                                }
                                            }
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Inbox,
                                                contentDescription = "PBS",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("PBS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(48.dp)
                                            .background(Color(0xFF334155))
                                    )

                                    // PDM Button
                                    val isPdm = viewModel.serverTypeInput == "PDM"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isPdm) proxmoxOrange else Color.Transparent)
                                            .clickable {
                                                viewModel.serverTypeInput = "PDM"
                                                if (viewModel.serverPortInput == "8006" || viewModel.serverPortInput == "8007" || viewModel.serverPortInput.isEmpty()) {
                                                    viewModel.serverPortInput = "8000"
                                                }
                                            }
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "PDM",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("PDM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                            
                            // Sub-label depending on choice
                            Text(
                                text = when (viewModel.serverTypeInput) {
                                    "PBS" -> "Yedekleme Sunucusu için"
                                    "PDM" -> "Veri Merkezi Yönetimi için"
                                    else -> "Sanal Ortam Sunucusu için"
                                },
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // SECTION 2: Kimlik Doğrulama Yöntemi (Standard/Password vs Token/OpenID style)
                    item {
                        Column {
                            Text(
                                text = "Kimlik Doğrulama Yöntemi",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                            ) {
                                // Standard/Password method
                                val isStandard = viewModel.authTypeInput == "PASSWORD"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isStandard) proxmoxOrange else Color.Transparent)
                                        .clickable { viewModel.authTypeInput = "PASSWORD" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = "Standart",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Standart", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(44.dp)
                                        .background(Color(0xFF334155))
                                )

                                // Token/OpenID alternative method
                                val isToken = viewModel.authTypeInput == "TOKEN"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isToken) proxmoxOrange else Color.Transparent)
                                        .clickable { viewModel.authTypeInput = "TOKEN" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "API Token",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("API Token", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }

                            Text(
                                text = if (viewModel.authTypeInput == "PASSWORD") "Kullanıcı adı ve şifre ile doğrulama kullan" else "Proxmox API Token ID ve Secret kullan",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // SECTION 3: Görünen Ad (Friendly Profile Name)
                    item {
                        Column {
                            Text(
                                text = "Görünen Ad",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.serverNameInput,
                                onValueChange = { viewModel.serverNameInput = it },
                                placeholder = { Text("Bu cihaz için bir isim girin", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedBorderColor = proxmoxOrange,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_name_input")
                            )
                        }
                    }

                    // SECTION 4: Ana Bilgisayar Adı (Hostname/IP) + Protocol Switcher (http vs https)
                    item {
                        Column {
                            Text(
                                text = "Ana Bilgisayar Adı",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.serverHostInput,
                                    onValueChange = { viewModel.serverHostInput = it },
                                    placeholder = { Text("Hostname veya IP girin", color = Color(0xFF64748B)) },
                                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedBorderColor = proxmoxOrange,
                                        unfocusedBorderColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("server_host_input")
                                )

                                // Protocol Switcher Group
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                ) {
                                    listOf("https", "http").forEach { proto ->
                                        val isSel = viewModel.serverProtocolInput == proto
                                        Box(
                                            modifier = Modifier
                                                .clickable { viewModel.serverProtocolInput = proto }
                                                .background(if (isSel) Color(0xFF334155) else Color.Transparent)
                                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = proto,
                                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Güvenli HTTPS bağlantısı kullanılıyor (önerilen)",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // SECTION 5: Bağlantı Noktası (Port)
                    item {
                        Column {
                            Text(
                                text = "Bağlantı Noktası (Port)",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.serverPortInput,
                                onValueChange = { viewModel.serverPortInput = it },
                                placeholder = { Text("8006", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedBorderColor = proxmoxOrange,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_port_input")
                            )
                            Text(
                                text = "Varsayılan port: ${if (viewModel.serverTypeInput == "PBS") "8007" else if (viewModel.serverTypeInput == "PDM") "8000" else "8006"}",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // SECTION 6: Kullanıcı Adı + Suffix Buttons (@pam, @pve, @pbs)
                    item {
                        Column {
                            Text(
                                text = "Kullanıcı Adı",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.usernameInput,
                                    onValueChange = { viewModel.usernameInput = it },
                                    placeholder = { Text("Kullanıcı adı girin", color = Color(0xFF64748B)) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedBorderColor = proxmoxOrange,
                                        unfocusedBorderColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("server_user_input")
                                )

                                // Realm Suffix Quick Selectors
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                ) {
                                    val suffixes = when (viewModel.serverTypeInput) {
                                        "PBS" -> listOf("@pam", "@pbs")
                                        "PVE" -> listOf("@pam", "@pve")
                                        else -> listOf("@pam", "@pve", "@pbs")
                                    }
                                    suffixes.forEach { suffix ->
                                        val isSelected = viewModel.usernameInput.endsWith(suffix)
                                        Box(
                                            modifier = Modifier
                                                .clickable { viewModel.updateUsernameSuffix(suffix) }
                                                .background(if (isSelected) Color(0xFF334155) else Color.Transparent)
                                                .padding(horizontal = 10.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = suffix,
                                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Kullanıcılar için @pam, Linux kullanıcıları için @pam veya @pve/@pbs kullanın",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // SECTION 7: Password / Token Secret
                    item {
                        Column {
                            Text(
                                text = if (viewModel.authTypeInput == "TOKEN") "Token Değeri / Secret" else "Şifre",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = if (viewModel.authTypeInput == "TOKEN") viewModel.tokenValueInput else viewModel.passwordInput,
                                onValueChange = {
                                    if (viewModel.authTypeInput == "TOKEN") {
                                        viewModel.tokenValueInput = it
                                    } else {
                                        viewModel.passwordInput = it
                                    }
                                },
                                placeholder = { Text("Şifre veya token girin", color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle visibility",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedBorderColor = proxmoxOrange,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_password_input")
                            )
                        }
                    }

                    // SECTION 8: Gelişmiş Seçenekler Dropdown
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAdvancedOptions = !showAdvancedOptions }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Gelişmiş Seçenekler",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Gelişmiş Seçenekler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Icon(
                                    imageVector = if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Advanced",
                                    tint = Color.White
                                )
                            }

                            if (showAdvancedOptions) {
                                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Bypass SSL Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("SSL Doğrulamasını Atla", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text(
                                                "Kendi kendine imzalı sertifikalar için gereklidir",
                                                color = Color(0xFF64748B),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Switch(
                                            checked = viewModel.bypassSslInput,
                                            onCheckedChange = { viewModel.bypassSslInput = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = proxmoxOrange
                                            )
                                        )
                                    }

                                    if (viewModel.authTypeInput == "TOKEN") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Token Name field
                                        Text(
                                            text = "Token İsmi (Token ID)",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        OutlinedTextField(
                                            value = viewModel.tokenNameInput,
                                            onValueChange = { viewModel.tokenNameInput = it },
                                            placeholder = { Text("Örn: android-token", color = Color(0xFF64748B)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color(0xFF020617),
                                                unfocusedContainerColor = Color(0xFF020617),
                                                focusedBorderColor = proxmoxOrange,
                                                unfocusedBorderColor = Color(0xFF334155)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Test connection status feedback banner
                    viewModel.testConnectionStatus?.let { status ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (status.startsWith("Success") || status.startsWith("Success!")) Color(0x1A10B981) else Color(0x1AEF4444)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = SolidColor(if (status.startsWith("Success") || status.startsWith("Success!")) Color(0xFF10B981) else Color(0xFFEF4444))
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (status.startsWith("Success") || status.startsWith("Success!")) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = "Status Icon",
                                        tint = if (status.startsWith("Success") || status.startsWith("Success!")) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = status,
                                        color = if (status.startsWith("Success") || status.startsWith("Success!")) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Test Link / Connection trigger
                    item {
                        OutlinedButton(
                            onClick = { viewModel.testConnection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF38BDF8)
                            ),
                            border = borderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            if (viewModel.isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF38BDF8))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bağlantıyı Test Et", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Save Profile & Cancel Section
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 32.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (viewModel.serverNameInput.isNotBlank() && viewModel.serverHostInput.isNotBlank() && viewModel.usernameInput.isNotBlank()) {
                                        viewModel.saveServer()
                                        isAddingServer = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = proxmoxOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                            ) {
                                Text(
                                    text = if (viewModel.editingServerId != null) "Değişiklikleri Kaydet" else "Sunucu Ekle",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.clearServerForm()
                                    isAddingServer = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF94A3B8)
                                )
                            ) {
                                Text("Vazgeç", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}



    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = proxmoxOrange)
                ) {
                    Text("Anladım", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Bağlantı Yardımı", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• PVE: Sanal makineleri ve konteynerleri izlemek için standart Proxmox ana makinenizi bağlayın.", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                    Text("• PBS: Proxmox Yedekleme Sunucunuzun durumunu ve depolama alanını takip etmek için kullanın.", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                    Text("• PDM: Proxmox Datacenter Manager / Mail gateway entegrasyonu için kullanın.", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                    Text("• Standart Giriş: root@pam, admin@pve gibi kimlik suffixlerini sonuna ekleyerek kullanın.", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                }
            },
            containerColor = Color(0xFF0F172A),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun ServerItemCard(
    server: ProxmoxServer,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showCounts: Boolean = true,
    accentColor: Color = Color(0xFFEA580C),
    modifier: Modifier = Modifier
) {
    val isPbs = server.url.contains("8007") || server.name.lowercase().contains("pbs")
    val isPdm = server.url.contains("8000") || server.name.lowercase().contains("pdm")
    val isDemo = server.isDemo

    val serverTypeLabel = if (isPbs) "PBS" else if (isPdm) "PDM" else "PVE"
    val serverStatusLabel = "Çevrimiçi"
    val serverStatusColor = Color(0xFF10B981) // Emerald Green

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isDemo) Color(0xFF10B981) else Color(0xFF1E293B))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onConnect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Name + Action Icons (Edit / Delete) + Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Demo tag or Server Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDemo) Color(0xFF047857) else Color(0xFF1E3A8A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDemo) "DEMO" else serverTypeLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF38BDF8), // Light Blue
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Profile",
                            tint = Color(0xFFEF4444), // Soft Red
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Server details (URL, User, Environment Type, etc. exactly as Screenshot 2)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Connection address / port
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = server.url.removePrefix("https://").removePrefix("http://"),
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Logged in user
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (server.authType == "TOKEN") "${server.username}!${server.tokenName}" else server.username,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }

                // Environment description (Sanal Ortam or Yedekleme)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPbs) Icons.Default.CloudUpload else Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPbs) "Yedekleme Sunucusu" else "Sanal Ortam",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }

                // Node indicator with custom status dot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPbs) "pbs" else "pve",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(serverStatusColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connect button area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Online/Çevrimiçi tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(serverStatusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = serverStatusLabel,
                        color = serverStatusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Connect Row button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Bağlan",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Bağlan",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Simple helper line stroke to avoid any API discrepancies
@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
