package com.app.deviceinsight.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.deviceinsight.data.models.*
import com.app.deviceinsight.domain.models.DeviceInfoItem
import com.app.deviceinsight.domain.models.DeviceInfoSection
import com.app.deviceinsight.domain.models.UiState
import com.app.deviceinsight.domain.usecases.GetDeviceInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceInfoViewModel(
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _sections = MutableStateFlow<List<DeviceInfoSection>>(emptyList())
    val sections: StateFlow<List<DeviceInfoSection>> = _sections.asStateFlow()

    init {
        loadDeviceInfo()
    }

    fun loadDeviceInfo() {
        viewModelScope.launch {
            getDeviceInfoUseCase().collect { state ->
                _uiState.value = state
                if (state is UiState.Success) {
                    _sections.value = createSections(state.deviceInfo)
                }
            }
        }
    }

    fun refreshDeviceInfo() {
        viewModelScope.launch {
            getDeviceInfoUseCase.refresh().collect { state ->
                _uiState.value = state
                if (state is UiState.Success) {
                    _sections.value = createSections(state.deviceInfo)
                }
            }
        }
    }

    fun toggleSection(sectionTitle: String) {
        _sections.value = _sections.value.map { section ->
            if (section.title == sectionTitle) {
                section.copy(isExpanded = !section.isExpanded)
            } else {
                section
            }
        }
    }

    private fun createSections(deviceInfo: DeviceInfo): List<DeviceInfoSection> {
        return listOf(
            createDeviceOverviewSection(deviceInfo.deviceOverview),
            createOSInfoSection(deviceInfo.osInfo),
            createHardwareInfoSection(deviceInfo.hardwareInfo),
            createMemoryInfoSection(deviceInfo.memoryInfo),
            createBatteryInfoSection(deviceInfo.batteryInfo),
            createDisplayInfoSection(deviceInfo.displayInfo),
            createNetworkInfoSection(deviceInfo.networkInfo),
            createSensorInfoSection(deviceInfo.sensorInfo),
            createCameraInfoSection(deviceInfo.cameraInfo),
            createSystemInfoSection(deviceInfo.systemInfo)
        )
    }

    private fun createDeviceOverviewSection(overview: DeviceOverview): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Device Overview",
            items = listOf(
                DeviceInfoItem("Manufacturer", overview.manufacturer),
                DeviceInfoItem("Model", overview.model),
                DeviceInfoItem("Brand", overview.brand),
                DeviceInfoItem("Board", overview.board),
                DeviceInfoItem("Bootloader", overview.bootloader),
                DeviceInfoItem("Device", overview.device),
                DeviceInfoItem("Product", overview.product),
                DeviceInfoItem("Hardware", overview.hardware)
            ),
            isExpanded = true
        )
    }

    private fun createOSInfoSection(osInfo: OSInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "OS & Software Info",
            items = listOf(
                DeviceInfoItem("Android Version", osInfo.androidVersion),
                DeviceInfoItem("API Level", osInfo.apiLevel.toString()),
                DeviceInfoItem("Security Patch", osInfo.securityPatch),
                DeviceInfoItem("Build ID", osInfo.buildId),
                DeviceInfoItem("Kernel Version", osInfo.kernelVersion),
                DeviceInfoItem("Build Date", osInfo.buildDate),
                DeviceInfoItem("Build Type", osInfo.buildType),
                DeviceInfoItem("Build User", osInfo.buildUser),
                DeviceInfoItem("Build Host", osInfo.buildHost)
            )
        )
    }

    private fun createHardwareInfoSection(hardwareInfo: HardwareInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "CPU & Hardware",
            items = listOf(
                DeviceInfoItem("CPU Architecture", hardwareInfo.cpuArchitecture),
                DeviceInfoItem("CPU Cores", hardwareInfo.cpuCores.toString()),
                DeviceInfoItem("CPU Frequency", hardwareInfo.cpuFrequency),
                DeviceInfoItem("Supported ABIs", hardwareInfo.supportedAbis.joinToString(", ")),
                DeviceInfoItem("32-bit ABIs", hardwareInfo.supportedAbis32.joinToString(", ")),
                DeviceInfoItem("64-bit ABIs", hardwareInfo.supportedAbis64.joinToString(", "))
            )
        )
    }

    private fun createMemoryInfoSection(memoryInfo: MemoryInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Memory & Storage",
            items = buildList {
                add(DeviceInfoItem("Total RAM", formatBytes(memoryInfo.totalRam)))
                add(DeviceInfoItem("Available RAM", formatBytes(memoryInfo.availableRam)))
                add(DeviceInfoItem("Used RAM", formatBytes(memoryInfo.usedRam)))
                add(DeviceInfoItem("Total Internal Storage", formatBytes(memoryInfo.totalInternalStorage)))
                add(DeviceInfoItem("Available Internal Storage", formatBytes(memoryInfo.availableInternalStorage)))
                add(DeviceInfoItem("Used Internal Storage", formatBytes(memoryInfo.usedInternalStorage)))
                add(DeviceInfoItem("Has External Storage", if (memoryInfo.hasExternalStorage) "Yes" else "No"))
                if (memoryInfo.hasExternalStorage && memoryInfo.totalExternalStorage != null) {
                    add(DeviceInfoItem("Total External Storage", formatBytes(memoryInfo.totalExternalStorage)))
                    if (memoryInfo.availableExternalStorage != null) {
                        add(DeviceInfoItem("Available External Storage", formatBytes(memoryInfo.availableExternalStorage)))
                    }
                }
            }
        )
    }

    private fun createBatteryInfoSection(batteryInfo: BatteryInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Battery Info",
            items = listOf(
                DeviceInfoItem("Battery Level", "${batteryInfo.level}%"),
                DeviceInfoItem("Status", batteryInfo.status),
                DeviceInfoItem("Health", batteryInfo.health),
                DeviceInfoItem("Temperature", "${batteryInfo.temperature}°C"),
                DeviceInfoItem("Voltage", "${batteryInfo.voltage} mV"),
                DeviceInfoItem("Technology", batteryInfo.technology),
                DeviceInfoItem("Is Charging", if (batteryInfo.isCharging) "Yes" else "No"),
                DeviceInfoItem("Charging Source", batteryInfo.chargingSource)
            )
        )
    }

    private fun createDisplayInfoSection(displayInfo: DisplayInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Display Info",
            items = listOf(
                DeviceInfoItem("Screen Resolution", displayInfo.screenResolution),
                DeviceInfoItem("Screen Density", "${displayInfo.screenDensity} dpi"),
                DeviceInfoItem("Density Category", displayInfo.screenDensityDpi),
                DeviceInfoItem("Refresh Rate", "${displayInfo.refreshRate} Hz"),
                DeviceInfoItem("Screen Size", displayInfo.screenSize),
                DeviceInfoItem("Orientation", displayInfo.orientation)
            )
        )
    }

    private fun createNetworkInfoSection(networkInfo: NetworkInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Network Info",
            items = buildList {
                add(DeviceInfoItem("WiFi Enabled", if (networkInfo.wifiEnabled) "Yes" else "No"))
                add(DeviceInfoItem("WiFi Connected", if (networkInfo.wifiConnected) "Yes" else "No"))
                networkInfo.wifiSSID?.let { add(DeviceInfoItem("WiFi SSID", it)) }
                networkInfo.ipAddress?.let { add(DeviceInfoItem("IP Address", it)) }
                networkInfo.macAddress?.let { add(DeviceInfoItem("MAC Address", it)) }
                add(DeviceInfoItem("Network Type", networkInfo.networkType))
                networkInfo.signalStrength?.let { add(DeviceInfoItem("Signal Strength", "$it dBm")) }
            }
        )
    }

    private fun createSensorInfoSection(sensorInfo: SensorInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Sensors (${sensorInfo.sensors.size})",
            items = sensorInfo.sensors.mapIndexed { index, sensor ->
                DeviceInfoItem("${index + 1}. ${sensor.name}", "${sensor.type} - ${sensor.vendor}")
            }
        )
    }

    private fun createCameraInfoSection(cameraInfo: CameraInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "Camera Info (${cameraInfo.cameras.size})",
            items = cameraInfo.cameras.mapIndexed { index, camera ->
                DeviceInfoItem("Camera ${index + 1}", "${camera.facing} - ${camera.megapixels}")
            }
        )
    }

    private fun createSystemInfoSection(systemInfo: SystemInfo): DeviceInfoSection {
        return DeviceInfoSection(
            title = "System Info",
            items = listOf(
                DeviceInfoItem("Uptime", systemInfo.uptime),
                DeviceInfoItem("Boot Time", systemInfo.bootTime),
                DeviceInfoItem("Timezone", systemInfo.timezone),
                DeviceInfoItem("Locale", systemInfo.locale),
                DeviceInfoItem("Java VM Version", systemInfo.javaVmVersion),
                DeviceInfoItem("Java VM Name", systemInfo.javaVmName)
            )
        )
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
}
