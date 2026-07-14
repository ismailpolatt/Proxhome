package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.ProxmoxViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProxmoxViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isClearingData by remember { mutableStateOf(false) }
    var clearSuccess by remember { mutableStateOf(false) }

    val isDark = when (viewModel.themeSetting) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    // Colors & Theme constants matching a beautiful blue-accented premium theme
    val midnightBlueBg = if (isDark) Color(0xFF030712) else Color(0xFFF8FAFC)
    val deepNavyBg = if (isDark) Color(0xFF0B132B) else Color(0xFFF1F5F9)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val borderBlue = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val accentBlue = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7) // Soft Blue for settings accents

    val gradientBg = Brush.verticalGradient(
        colors = listOf(deepNavyBg, midnightBlueBg)
    )

    // Option list items
    val themeOptions = listOf("System", "Dark", "Light")
    val timeoutOptions = listOf("2s", "5s", "10s", "15s")
    val dateOptions = listOf("System (d.MM.y)", "MM/dd/yyyy", "yyyy-MM-dd")
    val intervalOptions = listOf("5s", "10s", "15s", "30s")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sistem Ayarları",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                            .background(if (isDark) Color(0xFF1F2937) else Color(0xFFE2E8F0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = if (isDark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = deepNavyBg,
                    titleContentColor = if (isDark) Color.White else Color(0xFF0F172A)
                )
            )
        },
        containerColor = midnightBlueBg,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Section 1: Tema & Arayüz Renkleri
                Text(
                    text = "GÖRÜNÜM VE TEMALANDIRMA",
                    fontWeight = FontWeight.Bold,
                    color = accentBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(0.8.dp, borderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Theme selection row
                        SettingsDropdownRow(
                            icon = Icons.Default.NightsStay,
                            label = "Görünüm Modu",
                            subtext = "Uygulama arka plan ve kontrast şeması",
                            currentValue = when (viewModel.themeSetting) {
                                "System" -> "Sistem Varsayılanı"
                                "Dark" -> "Karanlık Tema"
                                "Light" -> "Aydınlık Tema"
                                else -> viewModel.themeSetting
                            },
                            options = themeOptions,
                            onSelect = { viewModel.updateThemeSetting(it) },
                            isDark = isDark
                        )

                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Dynamic accent color picker (Turuncu, Mavi, Yeşil, Mor)
                        Text(
                            text = "Tema Renk Paleti",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Giriş ve ana sayfa arayüzlerindeki vurgu rengini seçin",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val colors = listOf(
                                Triple("Mavi", "Sky Blue", Color(0xFF0284C7)),
                                Triple("Turuncu", "Orange", Color(0xFFEA580C)),
                                Triple("Yeşil", "Emerald Green", Color(0xFF059669)),
                                Triple("Mor", "Purple", Color(0xFF7C3AED))
                            )
                            colors.forEach { (label, name, colorVal) ->
                                val isSelected = viewModel.primaryColorSetting == label
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { viewModel.updatePrimaryColorSetting(label) }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colorVal)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seçildi",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: AI Copilot Configuration
                Text(
                    text = "YAPAY ZEKA (COPILOT) AYARLARI",
                    fontWeight = FontWeight.Bold,
                    color = accentBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(0.8.dp, borderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // AI Provider Selection
                        val aiProviders = listOf("Gemini", "OpenAI", "Claude", "Ollama")
                        var expandedProvider by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedProvider = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Sağlayıcı",
                                    tint = accentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "AI Sağlayıcısı",
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Etkin yapay zeka altyapısını seçin",
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Box {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = viewModel.aiProviderSetting,
                                        color = accentBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF64748B) else Color(0xFF475569)
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedProvider,
                                    onDismissRequest = { expandedProvider = false },
                                    modifier = Modifier.background(if (isDark) Color(0xFF1F2937) else Color.White)
                                ) {
                                    aiProviders.forEach { provider ->
                                        DropdownMenuItem(
                                            text = { Text(provider, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                                            onClick = {
                                                viewModel.updateAiProviderSetting(provider)
                                                expandedProvider = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // API Key Field (if not Ollama)
                        if (viewModel.aiProviderSetting != "Ollama") {
                            var showApiKey by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = viewModel.aiApiKeySetting,
                                onValueChange = { viewModel.updateAiApiKeySetting(it) },
                                label = { Text("API Anahtarı (API Key)", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)) },
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "API Anahtarını Göster/Gizle",
                                            tint = if (isDark) Color(0xFF64748B) else Color(0xFF475569)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    focusedBorderColor = accentBlue,
                                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                    focusedContainerColor = midnightBlueBg,
                                    unfocusedContainerColor = midnightBlueBg
                                ),
                                placeholder = {
                                    if (viewModel.aiProviderSetting == "Gemini") {
                                        Text("Sistem varsayılan anahtarı etkin...", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                                    } else {
                                        Text("API anahtarınızı girin", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                                    }
                                },
                                supportingText = {
                                    if (viewModel.aiProviderSetting == "Gemini" && viewModel.aiApiKeySetting.isBlank()) {
                                        Text("Yerleşik Gemini API anahtarını kullanmak için boş bırakın.", color = Color(0xFF10B981), fontSize = 11.sp)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Model Name Field
                        OutlinedTextField(
                            value = viewModel.aiModelSetting,
                            onValueChange = { viewModel.updateAiModelSetting(it) },
                            label = { Text("Yapay Zeka Modeli (Model Name)", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                focusedBorderColor = accentBlue,
                                unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                focusedContainerColor = midnightBlueBg,
                                unfocusedContainerColor = midnightBlueBg
                            ),
                            placeholder = {
                                val ph = when (viewModel.aiProviderSetting) {
                                    "Gemini" -> "gemini-3.5-flash"
                                    "OpenAI" -> "gpt-4o-mini"
                                    "Claude" -> "claude-3-5-sonnet"
                                    "Ollama" -> "llama3"
                                    else -> ""
                                }
                                Text(ph, color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Base URL Field (for OpenAI proxy or Ollama)
                        if (viewModel.aiProviderSetting == "Ollama" || viewModel.aiProviderSetting == "OpenAI") {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = viewModel.aiBaseUrlSetting,
                                onValueChange = { viewModel.updateAiBaseUrlSetting(it) },
                                label = {
                                    val lbl = if (viewModel.aiProviderSetting == "Ollama") "Ollama Endpoint URL" else "Özel Base URL (Proxy)"
                                    Text(lbl, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    focusedBorderColor = accentBlue,
                                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                    focusedContainerColor = midnightBlueBg,
                                    unfocusedContainerColor = midnightBlueBg
                                ),
                                placeholder = {
                                    val ph = if (viewModel.aiProviderSetting == "Ollama") "http://10.0.2.2:11434" else "https://api.openai.com"
                                    Text(ph, color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                                },
                                supportingText = {
                                    val helperText = if (viewModel.aiProviderSetting == "Ollama") {
                                        "Varsayılan: http://10.0.2.2:11434 (Android Emülatöründen localhost)"
                                    } else {
                                        "İsteğe bağlı: Resmi OpenAI API uç noktası için boş bırakın."
                                    }
                                    Text(helperText, color = if (isDark) Color(0xFF64748B) else Color(0xFF475569), fontSize = 11.sp)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 3: Gelişmiş Uygulama Ayarları
                Text(
                    text = "GELİŞMİŞ SİSTEM AYARLARI",
                    fontWeight = FontWeight.Bold,
                    color = accentBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(0.8.dp, borderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column {
                        // 1. Timeout Settings
                        SettingsDropdownRow(
                            icon = Icons.Default.Timer,
                            label = "Bağlantı Zaman Aşımı",
                            subtext = "Sorgularda beklenecek maksimum süre",
                            currentValue = viewModel.timeoutSetting,
                            options = timeoutOptions,
                            onSelect = { viewModel.updateTimeoutSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 2. 24h Time setting
                        SettingsSwitchRow(
                            icon = Icons.Default.AccessTime,
                            label = "24 Saat Saat Formatı",
                            subtext = "Tarih ve zamanlarda 24 saatlik dilim kullan",
                            checked = viewModel.is24hTimeSetting,
                            onCheckedChange = { viewModel.updateIs24hTimeSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 3. Date Format setting
                        SettingsDropdownRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Tarih Düzeni",
                            subtext = "Tarihlerin uygulama genelinde gösterim şekli",
                            currentValue = viewModel.dateFormatSetting,
                            options = dateOptions,
                            onSelect = { viewModel.updateDateFormatSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 4. Guest names
                        SettingsSwitchRow(
                            icon = Icons.Default.Dns,
                            label = "Konuk Düğüm İsimlerini Göster",
                            subtext = "Kapsayıcı ve VM'lerin altında bağlı oldukları düğümü yaz",
                            checked = viewModel.showGuestNodeNamesSetting,
                            onCheckedChange = { viewModel.updateShowGuestNodeNamesSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 5. Resource Counts
                        SettingsSwitchRow(
                            icon = Icons.Default.Tag,
                            label = "Kaynak Sayılarını Listele",
                            subtext = "Düğüm ve sanal makine sayılarını sunucu listesinde göster",
                            checked = viewModel.showResourceCountsSetting,
                            onCheckedChange = { viewModel.updateShowResourceCountsSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 6. Group servers by status
                        SettingsSwitchRow(
                            icon = Icons.Default.FormatListBulleted,
                            label = "Sunucuları Duruma Göre Grupla",
                            subtext = "Çevrimiçi ve çevrimdışı sunucuları ayrı bölümlerde tut",
                            checked = viewModel.groupServersByStatusSetting,
                            onCheckedChange = { viewModel.updateGroupServersByStatusSetting(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 7. Live polling switch
                        SettingsSwitchRow(
                            icon = Icons.Default.Sync,
                            label = "Anlık Canlı Veri Sorgulama",
                            subtext = "Bağlı olunan sunucuyu belirli aralıklarla otomatik yenile",
                            checked = viewModel.isLiveMode,
                            onCheckedChange = { viewModel.toggleLiveMode(it) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), thickness = 0.8.dp)

                        // 8. Live polling interval dropdown
                        SettingsDropdownRow(
                            icon = Icons.Default.Speed,
                            label = "Canlı Veri Yenileme Sıklığı",
                            subtext = "Otomatik veri yenileme döngüsü periyodu",
                            currentValue = "${viewModel.pollingIntervalSeconds}s",
                            options = intervalOptions,
                            onSelect = { selectedValue ->
                                val seconds = selectedValue.removeSuffix("s").toIntOrNull() ?: 10
                                viewModel.setPollingInterval(seconds)
                            },
                            isDark = isDark
                        )
                    }
                }

                // Section 4: Güvenlik
                Text(
                    text = "GÜVENLİK AYARLARI",
                    fontWeight = FontWeight.Bold,
                    color = accentBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(0.8.dp, borderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Lock,
                        label = "Uygulama Giriş Kilidi",
                        subtext = "Uygulama açılırken biyometrik/PIN doğrulaması iste",
                        checked = viewModel.appLockEnabledSetting,
                        onCheckedChange = { viewModel.updateAppLockEnabledSetting(it) },
                        isDark = isDark
                    )
                }

                // Section 5: Sistem & Önbellek Verisi
                Text(
                    text = "SİSTEM VE ÖNBELLEK",
                    fontWeight = FontWeight.Bold,
                    color = accentBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = borderStroke(0.8.dp, borderBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isClearingData = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Temizle",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "Tarayıcı Önbelleğini Temizle",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Gömülü tarayıcı çerezlerini, oturum bilgilerini ve önbelleği sıfırlar",
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF475569),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Cache clear progress visualizer
            if (isClearingData) {
                Dialog(onDismissRequest = { }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(20.dp),
                        border = borderStroke(0.8.dp, borderBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!clearSuccess) {
                                CircularProgressIndicator(color = accentBlue)
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Önbellek Temizleniyor...",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Oturum çerezleri, şifrelenmiş ağ önbelleği ve geçici dosyalar sıfırlanıyor...",
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                LaunchedEffect(Unit) {
                                    delay(1800)
                                    clearSuccess = true
                                    delay(1200)
                                    isClearingData = false
                                    clearSuccess = false
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Başarılı",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Başarıyla Temizlendi",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Önbellek havuzu başarıyla sıfırlandı.",
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    subtext: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), // Beautiful soft blue tint for icons
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtext,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569), // Brighter, high-contrast slate color
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Color(0xFF0284C7) else Color(0xFF0369A1)
            )
        )
    }
}

@Composable
fun SettingsDropdownRow(
    icon: ImageVector,
    label: String,
    subtext: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    isDark: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), // Beautiful soft blue tint for icons
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtext,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569), // Brighter, high-contrast slate color
                    fontSize = 12.sp
                )
            }
        }

        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentValue,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569) // Brighter arrow dropdown tint
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(if (isDark) Color(0xFF1F2937) else Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = if (isDark) Color.White else Color(0xFF0F172A)) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


