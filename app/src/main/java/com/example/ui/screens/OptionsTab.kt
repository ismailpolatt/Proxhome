package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun OptionsTab(
    viewModel: ProxmoxViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isClearingData by remember { mutableStateOf(false) }
    var clearSuccess by remember { mutableStateOf(false) }

    // Dropdown list declarations
    val themeOptions = listOf("System", "Dark", "Light")
    val timeoutOptions = listOf("2s", "5s", "10s", "15s")
    val dateOptions = listOf("System (d.MM.y)", "MM/dd/yyyy", "yyyy-MM-dd")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Match deep dashboard background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Options title
            Text(
                text = "Options",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("options_header_title")
            )

            // Block 1: Main App Configuration Options
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    // 1. Theme
                    OptionDropdownRow(
                        icon = Icons.Default.NightsStay,
                        label = "Theme",
                        currentValue = viewModel.themeSetting,
                        options = themeOptions,
                        onSelect = { viewModel.updateThemeSetting(it) },
                        testTag = "option_theme_row"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 2. Timeout
                    OptionDropdownRow(
                        icon = Icons.Default.Timer,
                        label = "Timeout",
                        currentValue = viewModel.timeoutSetting,
                        options = timeoutOptions,
                        onSelect = { viewModel.updateTimeoutSetting(it) },
                        testTag = "option_timeout_row"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 3. 24h Time
                    OptionSwitchRow(
                        icon = Icons.Default.AccessTime,
                        label = "24h Time",
                        checked = viewModel.is24hTimeSetting,
                        onCheckedChange = { viewModel.updateIs24hTimeSetting(it) },
                        testTag = "option_24h_time_switch"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 4. Date
                    OptionDropdownRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        currentValue = viewModel.dateFormatSetting,
                        options = dateOptions,
                        onSelect = { viewModel.updateDateFormatSetting(it) },
                        testTag = "option_date_row"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 5. Show Guest Node Names
                    OptionSwitchRow(
                        icon = Icons.Default.Dns,
                        label = "Show Guest Node Names",
                        subtext = "Show the node under each guest when in a cluster",
                        checked = viewModel.showGuestNodeNamesSetting,
                        onCheckedChange = { viewModel.updateShowGuestNodeNamesSetting(it) },
                        testTag = "option_guest_node_names_switch"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 6. Show Resource Counts
                    OptionSwitchRow(
                        icon = Icons.Default.Tag,
                        label = "Show Resource Counts",
                        subtext = "Displays node and guest counts on the server list",
                        checked = viewModel.showResourceCountsSetting,
                        onCheckedChange = { viewModel.updateShowResourceCountsSetting(it) },
                        testTag = "option_resource_counts_switch"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 7. Group Servers by Status
                    OptionSwitchRow(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Group Servers by Status",
                        subtext = "Separates online and offline servers into sections",
                        checked = viewModel.groupServersByStatusSetting,
                        onCheckedChange = { viewModel.updateGroupServersByStatusSetting(it) },
                        testTag = "option_group_servers_switch"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 8. Live Auto Polling
                    OptionSwitchRow(
                        icon = Icons.Default.Sync,
                        label = "Live Auto Polling",
                        subtext = "Periodically refresh cluster metrics automatically",
                        checked = viewModel.isLiveMode,
                        onCheckedChange = { viewModel.toggleLiveMode(it) },
                        testTag = "option_live_polling_switch"
                    )
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

                    // 9. Polling Interval
                    val intervalOptions = listOf("5s", "10s", "15s", "30s")
                    OptionDropdownRow(
                        icon = Icons.Default.Speed,
                        label = "Polling Interval",
                        currentValue = "${viewModel.pollingIntervalSeconds}s",
                        options = intervalOptions,
                        onSelect = { selectedValue ->
                            val seconds = selectedValue.removeSuffix("s").toIntOrNull() ?: 10
                            viewModel.setPollingInterval(seconds)
                        },
                        testTag = "option_polling_interval_row"
                    )
                }
            }

            // Block 2: App Lock Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                OptionSwitchRow(
                    icon = Icons.Default.Lock,
                    label = "App Lock",
                    subtext = "Require authentication when opening the app",
                    checked = viewModel.appLockEnabledSetting,
                    onCheckedChange = { viewModel.updateAppLockEnabledSetting(it) },
                    testTag = "option_app_lock_switch"
                )
            }

            // Block AI: AI Copilot Config Header
            Text(
                text = "AI COPILOT CONFIGURATION",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Provider Dropdown Selection
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
                                contentDescription = "AI Provider",
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Provider",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Select active LLM engine",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Box {
                            Text(
                                text = viewModel.aiProviderSetting,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            DropdownMenu(
                                expanded = expandedProvider,
                                onDismissRequest = { expandedProvider = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                aiProviders.forEach { provider ->
                                    DropdownMenuItem(
                                        text = { Text(provider, color = Color.White) },
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

                    // API Key Field (unless Ollama)
                    if (viewModel.aiProviderSetting != "Ollama") {
                        var showApiKey by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = viewModel.aiApiKeySetting,
                            onValueChange = { viewModel.updateAiApiKeySetting(it) },
                            label = { Text("API Key", color = Color(0xFF94A3B8)) },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle API Key visibility",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF020617),
                                unfocusedContainerColor = Color(0xFF020617)
                            ),
                            placeholder = {
                                if (viewModel.aiProviderSetting == "Gemini") {
                                    Text("Using default system key...", color = Color(0xFF475569))
                                } else {
                                    Text("Enter your API key", color = Color(0xFF475569))
                                }
                            },
                            supportingText = {
                                if (viewModel.aiProviderSetting == "Gemini" && viewModel.aiApiKeySetting.isBlank()) {
                                    Text("Leave empty to use built-in Gemini API Key", color = Color(0xFF10B981), fontSize = 11.sp)
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
                        label = { Text("Model Name", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        placeholder = {
                            val ph = when (viewModel.aiProviderSetting) {
                                "Gemini" -> "gemini-3.5-flash"
                                "OpenAI" -> "gpt-4o-mini"
                                "Claude" -> "claude-3-5-sonnet"
                                "Ollama" -> "llama3"
                                else -> ""
                            }
                            Text(ph, color = Color(0xFF475569))
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
                                val lbl = if (viewModel.aiProviderSetting == "Ollama") "Ollama Endpoint URL" else "Custom Base URL (Proxy)"
                                Text(lbl, color = Color(0xFF94A3B8))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF020617),
                                unfocusedContainerColor = Color(0xFF020617)
                            ),
                            placeholder = {
                                val ph = if (viewModel.aiProviderSetting == "Ollama") "http://10.0.2.2:11434" else "https://api.openai.com"
                                Text(ph, color = Color(0xFF475569))
                            },
                            supportingText = {
                                val helperText = if (viewModel.aiProviderSetting == "Ollama") {
                                    "Default: http://10.0.2.2:11434 (Android Host Machine IP)"
                                } else {
                                    "Optional: Keep empty to use official OpenAI endpoint"
                                }
                                Text(helperText, color = Color(0xFF64748B), fontSize = 11.sp)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Debug Section Header
            Text(
                text = "DEBUGGING",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Block 3: Debug Card with Clear Browser Data
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                OptionClickableRow(
                    icon = Icons.Default.Delete,
                    label = "Clear Browser Data",
                    subtext = "Clears cookies and cache from the embedded web browser",
                    onClick = { isClearingData = true },
                    testTag = "option_clear_browser_data"
                )
            }
        }

        // Cache clear progress visualizer
        if (isClearingData) {
            Dialog(onDismissRequest = { }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!clearSuccess) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6))
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Clearing Cache...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Clearing session cookies, storage configurations, and HTTP cache pools...",
                                color = Color(0xFF64748B),
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
                                contentDescription = "Success",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Browser Cache Wiped!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Successfully cleaned all embedded browser state.",
                                color = Color(0xFF64748B),
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

@Composable
fun OptionDropdownRow(
    icon: ImageVector,
    label: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentValue,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E293B))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
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

@Composable
fun OptionSwitchRow(
    icon: ImageVector,
    label: String,
    subtext: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtext != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtext,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF0F172A),
                checkedTrackColor = Color(0xFF2563EB), // Blue track like in the photo
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
fun OptionClickableRow(
    icon: ImageVector,
    label: String,
    subtext: String? = null,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF3B82F6), // Match other blue icons in list
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
