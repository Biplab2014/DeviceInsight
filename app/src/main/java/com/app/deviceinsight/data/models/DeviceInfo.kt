package com.app.deviceinsight.data.models

data class DeviceInfo(
    val deviceOverview: DeviceOverview,
    val osInfo: OSInfo,
    val hardwareInfo: HardwareInfo,
    val memoryInfo: MemoryInfo,
    val batteryInfo: BatteryInfo,
    val displayInfo: DisplayInfo,
    val networkInfo: NetworkInfo,
    val sensorInfo: SensorInfo,
    val cameraInfo: CameraInfo,
    val systemInfo: SystemInfo
)

data class DeviceOverview(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val board: String,
    val bootloader: String,
    val device: String,
    val product: String,
    val hardware: String
)

data class OSInfo(
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildId: String,
    val kernelVersion: String,
    val buildDate: String,
    val buildType: String,
    val buildUser: String,
    val buildHost: String
)

data class HardwareInfo(
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFrequency: String,
    val supportedAbis: List<String>,
    val supportedAbis32: List<String>,
    val supportedAbis64: List<String>
)

data class MemoryInfo(
    val totalRam: Long,
    val availableRam: Long,
    val usedRam: Long,
    val totalInternalStorage: Long,
    val availableInternalStorage: Long,
    val usedInternalStorage: Long,
    val hasExternalStorage: Boolean,
    val totalExternalStorage: Long?,
    val availableExternalStorage: Long?
)

data class BatteryInfo(
    val level: Int,
    val status: String,
    val health: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String,
    val isCharging: Boolean,
    val chargingSource: String
)

data class DisplayInfo(
    val screenResolution: String,
    val screenDensity: Int,
    val screenDensityDpi: String,
    val refreshRate: Float,
    val screenSize: String,
    val orientation: String
)

data class NetworkInfo(
    val wifiEnabled: Boolean,
    val wifiConnected: Boolean,
    val wifiSSID: String?,
    val ipAddress: String?,
    val macAddress: String?,
    val networkType: String,
    val signalStrength: Int?
)

data class SensorInfo(
    val sensors: List<SensorData>
)

data class SensorData(
    val name: String,
    val type: String,
    val vendor: String,
    val version: Int,
    val power: Float,
    val resolution: Float,
    val maximumRange: Float
)

data class CameraInfo(
    val cameras: List<CameraData>
)

data class CameraData(
    val id: String,
    val facing: String,
    val megapixels: String,
    val supportedResolutions: List<String>
)

data class SystemInfo(
    val uptime: String,
    val bootTime: String,
    val timezone: String,
    val locale: String,
    val javaVmVersion: String,
    val javaVmName: String
)
