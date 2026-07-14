package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.api.AiClient
import com.example.ui.viewmodel.ClusterUiState
import com.example.ui.viewmodel.ProxmoxViewModel
import com.example.ui.viewmodel.TasksUiState
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotDialog(
    viewModel: ProxmoxViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Local chat state
    val messages = remember {
        mutableStateListOf(
            ChatMessage("ai", "Merhaba! Ben Proxmox AI Asistanınız. Sunucu durumunuzu analiz edebilir, komut tavsiyeleri verebilir veya teknik sorularınızı yanıtlayabilirim.\n\nAşağıdaki hızlı işlemleri kullanabilir veya kendi sorunuzu yazabilirsiniz!")
        )
    }

    var textInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Scroll to bottom helper
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Make it full-width on mobile
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Copilot",
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Proxmox AI Copilot",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                messages.clear()
                                messages.add(ChatMessage("ai", "Sohbet geçmişi temizlendi. Nasıl yardımcı olabilirim?"))
                            }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color(0xFF94A3B8))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A),
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF020617)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Engine Status bar
                val currentEngine = viewModel.aiProviderSetting
                val currentModel = viewModel.aiModelSetting
                val usingDefaultGemini = currentEngine == "Gemini" && viewModel.aiApiKeySetting.isBlank()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Engine: $currentEngine ($currentModel)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (usingDefaultGemini) {
                            Text(
                                text = "Sistem Anahtarı",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0x2210B981), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Chat bubble list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(message = msg)
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF10B981),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Düşünüyor...",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick prompts panel
                if (!isGenerating && messages.size <= 2) {
                    QuickPromptsRow(
                        viewModel = viewModel,
                        onPromptSelected = { promptText ->
                            messages.add(ChatMessage("user", promptText))
                            isGenerating = true
                            coroutineScope.launch {
                                val systemPrompt = "Sen Proxmox VE sistem yönetimi konusunda uzman bir yapay zekasın. Kullanıcıya net, doğrudan, teknik olarak doğru ve faydalı Türkçe yanıtlar ver. Gereksiz giriş-gelişme cümleleri kurma, doğrudan çözüme odaklan."
                                val response = AiClient.generateResponse(
                                    provider = viewModel.aiProviderSetting,
                                    apiKey = viewModel.aiApiKeySetting,
                                    model = viewModel.aiModelSetting,
                                    baseUrl = viewModel.aiBaseUrlSetting,
                                    prompt = promptText,
                                    systemInstruction = systemPrompt
                                )
                                messages.add(ChatMessage("ai", response))
                                isGenerating = false
                            }
                        }
                    )
                }

                // Chat Input Bar
                Surface(
                    color = Color(0xFF0F172A),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Asistana bir soru sorun...", color = Color(0xFF64748B)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF020617),
                                unfocusedContainerColor = Color(0xFF020617),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input"),
                            maxLines = 4,
                            enabled = !isGenerating
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val userMsg = textInput.trim()
                                    textInput = ""
                                    messages.add(ChatMessage("user", userMsg))
                                    isGenerating = true
                                    coroutineScope.launch {
                                        val systemPrompt = "Sen Proxmox VE sistem yönetimi konusunda uzman bir yapay zekasın. Kullanıcıya net, doğrudan, teknik olarak doğru ve faydalı Türkçe yanıtlar ver. Gereksiz giriş-gelişme cümleleri kurma, doğrudan çözüme odaklan."
                                        val response = AiClient.generateResponse(
                                            provider = viewModel.aiProviderSetting,
                                            apiKey = viewModel.aiApiKeySetting,
                                            model = viewModel.aiModelSetting,
                                            baseUrl = viewModel.aiBaseUrlSetting,
                                            prompt = userMsg,
                                            systemInstruction = systemPrompt
                                        )
                                        messages.add(ChatMessage("ai", response))
                                        isGenerating = false
                                    }
                                }
                            },
                            enabled = textInput.isNotBlank() && !isGenerating,
                            modifier = Modifier
                                .background(
                                    color = if (textInput.isNotBlank() && !isGenerating) Color(0xFF10B981) else Color(0x2210B981),
                                    shape = RoundedCornerShape(50)
                                )
                                .testTag("ai_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isAi = message.sender == "ai"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isAi) Color(0xFF0F172A) else Color(0xFF1E3A8A)
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isAi) 0.dp else 12.dp,
                bottomEnd = if (isAi) 12.dp else 0.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isAi) Icons.Default.AutoAwesome else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isAi) Color(0xFF10B981) else Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAi) "AI Copilot" else "Siz",
                        fontWeight = FontWeight.Bold,
                        color = if (isAi) Color(0xFF10B981) else Color(0xFF93C5FD),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun QuickPromptsRow(
    viewModel: ProxmoxViewModel,
    onPromptSelected: (String) -> Unit
) {
    val clusterState = viewModel.clusterUiState
    val tasksState = viewModel.tasksUiState

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "HIZLI EYLEMLER",
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Option 1: Analyze cluster health (only active if cluster success)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .weight(1f)
                    .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .clickable {
                        var prompt = "Proxmox sunucu durumumu analiz edip bir durum özeti çıkarır mısın?"
                        if (clusterState is ClusterUiState.Success) {
                            prompt = """
                                Lütfen aşağıdaki Proxmox VE cluster metriklerimi analiz et:
                                - CPU Kullanımı: %${String.format("%.1f", clusterState.cpuUsagePct)} (Toplam Çekirdek: ${clusterState.totalCpuCores})
                                - Bellek Kullanımı: %${String.format("%.1f", clusterState.memUsagePct)} (Toplam: ${clusterState.totalMemory / (1024 * 1024 * 1024)} GB)
                                - Depolama Kullanımı: %${String.format("%.1f", clusterState.storageUsagePct)} (Toplam: ${clusterState.totalStorage / (1024 * 1024 * 1024)} GB)
                                - Aktif VM (Sanal Makine): ${clusterState.vms.size}
                                - Aktif LXC (Konteyner): ${clusterState.lxcs.size}
                                - Toplam Depolama Havuzu (Storage): ${clusterState.storages.size}

                                Sistem genel durumu nasıldır? Herhangi bir darboğaz görüyor musun? Çözüm veya optimizasyon önerilerin var mı? Yanıtı Türkçe ver.
                            """.trimIndent()
                        }
                        onPromptSelected(prompt)
                    },
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sistemi Analiz Et", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Metrikleri ve sağlığı yorumlar", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }

            // Option 2: Recommend CLI Cheat Sheet
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .weight(1f)
                    .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .clickable {
                        onPromptSelected("Proxmox VE'de en çok kullanılan CLI komutlarını (qm, pct, pvesh gibi) ve ne işe yaradıklarını gösteren kısa bir komut kılavuzu hazırlar mısın?")
                    },
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Komut Kılavuzu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("qm & pct terminal cheatsheet", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }
        }

        // Option 3: Analyze last task
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .clickable {
                    var prompt = "Proxmox sunucu görev günlüklerimi (tasks) kontrol edip olası hataları nasıl giderebileceğimi anlatır mısın?"
                    if (tasksState is TasksUiState.Success && tasksState.tasks.isNotEmpty()) {
                        val lastTask = tasksState.tasks.first()
                        prompt = """
                            Proxmox VE kümesinde en son gerçekleştirilen görev günlüğünü analiz etmeni istiyorum:
                            - İşlem Türü: ${lastTask.type}
                            - Gerçekleştiren Kullanıcı: ${lastTask.user}
                            - Sunucu/Node: ${lastTask.node}
                            - Durum (Status): ${lastTask.status ?: "Devam ediyor"}
                            - UPID: ${lastTask.upid}

                            Bu işlem ne anlama gelmektedir? Durum değeri göz önüne alındığında başarılı olmuş mudur? Eğer hata içeriyorsa bunu gidermek için hangi adımları izlemeliyim? Türkçe olarak açıkla.
                        """.trimIndent()
                    }
                    onPromptSelected(prompt)
                },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Son Sunucu Görevini İncele", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("En son yapılan loglanmış görevin başarısını ve detaylarını analiz eder", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }
        }
    }
}
