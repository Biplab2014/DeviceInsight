package com.app.deviceinsight.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.deviceinsight.data.collectors.DeviceInfoCollector
import com.app.deviceinsight.data.repository.DeviceInfoRepository
import com.app.deviceinsight.domain.models.UiState
import com.app.deviceinsight.domain.usecases.GetDeviceInfoUseCase
import com.app.deviceinsight.ui.components.DeviceInfoCard
import com.app.deviceinsight.ui.viewmodel.DeviceInfoViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Manual dependency injection
    val deviceInfoCollector = DeviceInfoCollector(context)
    val repository = DeviceInfoRepository(deviceInfoCollector)
    val useCase = GetDeviceInfoUseCase(repository)

    val viewModel: DeviceInfoViewModel = viewModel {
        DeviceInfoViewModel(useCase)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DeviceInsight",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshDeviceInfo() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    
                    if (uiState is UiState.Success) {
                        IconButton(
                            onClick = { shareDeviceInfo(context, uiState as UiState.Success) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading device information...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                is UiState.Error -> {
                    val errorState = uiState as UiState.Error
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDeviceInfo() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                is UiState.Success -> {
                    val successState = uiState as UiState.Success
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            // Header with device summary
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${successState.deviceInfo.deviceOverview.manufacturer} ${successState.deviceInfo.deviceOverview.model}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Android ${successState.deviceInfo.osInfo.androidVersion} (API ${successState.deviceInfo.osInfo.apiLevel})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        
                        items(sections) { section ->
                            DeviceInfoCard(
                                section = section,
                                onToggleExpanded = { viewModel.toggleSection(section.title) }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun shareDeviceInfo(context: Context, successState: UiState.Success) {
    val deviceInfo = successState.deviceInfo
    val gson = Gson()
    
    val shareText = buildString {
        appendLine("📱 Device Information Report")
        appendLine("Generated by DeviceInsight")
        appendLine("=".repeat(40))
        appendLine()
        
        appendLine("🔧 Device Overview:")
        appendLine("Manufacturer: ${deviceInfo.deviceOverview.manufacturer}")
        appendLine("Model: ${deviceInfo.deviceOverview.model}")
        appendLine("Brand: ${deviceInfo.deviceOverview.brand}")
        appendLine()
        
        appendLine("📱 OS Information:")
        appendLine("Android Version: ${deviceInfo.osInfo.androidVersion}")
        appendLine("API Level: ${deviceInfo.osInfo.apiLevel}")
        appendLine("Security Patch: ${deviceInfo.osInfo.securityPatch}")
        appendLine()
        
        appendLine("⚙️ Hardware:")
        appendLine("CPU Architecture: ${deviceInfo.hardwareInfo.cpuArchitecture}")
        appendLine("CPU Cores: ${deviceInfo.hardwareInfo.cpuCores}")
        appendLine("CPU Frequency: ${deviceInfo.hardwareInfo.cpuFrequency}")
        appendLine()
        
        appendLine("💾 Memory:")
        appendLine("Total RAM: ${formatBytes(deviceInfo.memoryInfo.totalRam)}")
        appendLine("Available RAM: ${formatBytes(deviceInfo.memoryInfo.availableRam)}")
        appendLine()
        
        appendLine("🔋 Battery:")
        appendLine("Level: ${deviceInfo.batteryInfo.level}%")
        appendLine("Status: ${deviceInfo.batteryInfo.status}")
        appendLine("Temperature: ${deviceInfo.batteryInfo.temperature}°C")
        appendLine()
        
        appendLine("📺 Display:")
        appendLine("Resolution: ${deviceInfo.displayInfo.screenResolution}")
        appendLine("Density: ${deviceInfo.displayInfo.screenDensity} dpi")
        appendLine("Refresh Rate: ${deviceInfo.displayInfo.refreshRate} Hz")
        appendLine()
        
        appendLine("🌐 Network:")
        appendLine("Network Type: ${deviceInfo.networkInfo.networkType}")
        if (deviceInfo.networkInfo.wifiConnected) {
            deviceInfo.networkInfo.wifiSSID?.let { appendLine("WiFi SSID: $it") }
            deviceInfo.networkInfo.ipAddress?.let { appendLine("IP Address: $it") }
        }
        appendLine()
        
        appendLine("📊 System:")
        appendLine("Uptime: ${deviceInfo.systemInfo.uptime}")
        appendLine("Timezone: ${deviceInfo.systemInfo.timezone}")
        appendLine()
        
        appendLine("Generated on: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
    }
    
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Device Information Report")
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Share Device Info"))
}

private fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}
