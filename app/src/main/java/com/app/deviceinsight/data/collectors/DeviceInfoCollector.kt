package com.app.deviceinsight.data.collectors

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import com.app.deviceinsight.data.models.*
import java.io.File
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

class DeviceInfoCollector(
    private val context: Context
) {

    fun collectDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceOverview = collectDeviceOverview(),
            osInfo = collectOSInfo(),
            hardwareInfo = collectHardwareInfo(),
            memoryInfo = collectMemoryInfo(),
            batteryInfo = collectBatteryInfo(),
            displayInfo = collectDisplayInfo(),
            networkInfo = collectNetworkInfo(),
            sensorInfo = collectSensorInfo(),
            cameraInfo = collectCameraInfo(),
            systemInfo = collectSystemInfo()
        )
    }

    private fun collectDeviceOverview(): DeviceOverview {
        return DeviceOverview(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            board = Build.BOARD,
            bootloader = Build.BOOTLOADER,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE
        )
    }

    private fun collectOSInfo(): OSInfo {
        return OSInfo(
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "N/A"
            },
            buildId = Build.ID,
            kernelVersion = System.getProperty("os.version") ?: "Unknown",
            buildDate = SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault())
                .format(Date(Build.TIME)),
            buildType = Build.TYPE,
            buildUser = Build.USER,
            buildHost = Build.HOST
        )
    }

    private fun collectHardwareInfo(): HardwareInfo {
        return HardwareInfo(
            cpuArchitecture = System.getProperty("os.arch") ?: "Unknown",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuFrequency = getCpuFrequency(),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            supportedAbis32 = Build.SUPPORTED_32_BIT_ABIS.toList(),
            supportedAbis64 = Build.SUPPORTED_64_BIT_ABIS.toList()
        )
    }

    private fun getCpuFrequency(): String {
        return try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (file.exists()) {
                val freq = file.readText().trim().toLongOrNull()
                if (freq != null) {
                    "${freq / 1000} MHz"
                } else {
                    "Unknown"
                }
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun collectMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val internalStat = StatFs(Environment.getDataDirectory().path)
        val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
        val internalAvailable = internalStat.availableBlocksLong * internalStat.blockSizeLong

        var externalTotal: Long? = null
        var externalAvailable: Long? = null
        val hasExternal = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

        if (hasExternal) {
            try {
                val externalStat = StatFs(Environment.getExternalStorageDirectory().path)
                externalTotal = externalStat.blockCountLong * externalStat.blockSizeLong
                externalAvailable = externalStat.availableBlocksLong * externalStat.blockSizeLong
            } catch (e: Exception) {
                // External storage might not be accessible
            }
        }

        return MemoryInfo(
            totalRam = memInfo.totalMem,
            availableRam = memInfo.availMem,
            usedRam = memInfo.totalMem - memInfo.availMem,
            totalInternalStorage = internalTotal,
            availableInternalStorage = internalAvailable,
            usedInternalStorage = internalTotal - internalAvailable,
            hasExternalStorage = hasExternal,
            totalExternalStorage = externalTotal,
            availableExternalStorage = externalAvailable
        )
    }

    private fun collectBatteryInfo(): BatteryInfo {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val statusString = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }

        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val chargingSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Charging"
        }

        return BatteryInfo(
            level = batteryPct,
            status = statusString,
            health = healthString,
            temperature = temperature / 10.0f,
            voltage = voltage,
            technology = technology,
            isCharging = isCharging,
            chargingSource = chargingSource
        )
    }

    private fun collectDisplayInfo(): DisplayInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        val density = displayMetrics.density
        val densityDpi = displayMetrics.densityDpi

        val densityString = when {
            densityDpi <= 120 -> "LDPI"
            densityDpi <= 160 -> "MDPI"
            densityDpi <= 240 -> "HDPI"
            densityDpi <= 320 -> "XHDPI"
            densityDpi <= 480 -> "XXHDPI"
            densityDpi <= 640 -> "XXXHDPI"
            else -> "ULTRA_HIGH"
        }

        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            windowManager.defaultDisplay.refreshRate
        } else {
            60.0f
        }

        val widthDp = widthPixels / density
        val heightDp = heightPixels / density
        val screenSize = String.format("%.1f\"", 
            kotlin.math.sqrt((widthDp * widthDp + heightDp * heightDp).toDouble()) / 160)

        val orientation = when (context.resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> "Portrait"
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
            else -> "Unknown"
        }

        return DisplayInfo(
            screenResolution = "${widthPixels} x ${heightPixels}",
            screenDensity = densityDpi,
            screenDensityDpi = densityString,
            refreshRate = refreshRate,
            screenSize = screenSize,
            orientation = orientation
        )
    }

    @SuppressLint("MissingPermission")
    private fun collectNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiEnabled = wifiManager.isWifiEnabled
        var wifiConnected = false
        var wifiSSID: String? = null
        var ipAddress: String? = null
        var networkType = "Unknown"
        var signalStrength: Int? = null

        try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            if (networkCapabilities != null) {
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        networkType = "WiFi"
                        wifiConnected = true

                        val wifiInfo = wifiManager.connectionInfo
                        wifiSSID = wifiInfo.ssid?.replace("\"", "")

                        val ipInt = wifiInfo.ipAddress
                        ipAddress = String.format(
                            "%d.%d.%d.%d",
                            ipInt and 0xff,
                            ipInt shr 8 and 0xff,
                            ipInt shr 16 and 0xff,
                            ipInt shr 24 and 0xff
                        )

                        signalStrength = wifiInfo.rssi
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        networkType = "Mobile Data"
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        networkType = "Ethernet"
                    }
                }
            }
        } catch (e: Exception) {
            // Handle permission or other exceptions
        }

        val macAddress = try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            interfaces?.asSequence()
                ?.find { it.name.equals("wlan0", ignoreCase = true) }
                ?.hardwareAddress
                ?.joinToString(":") { "%02x".format(it) }
                ?.uppercase()
        } catch (e: Exception) {
            null
        }

        return NetworkInfo(
            wifiEnabled = wifiEnabled,
            wifiConnected = wifiConnected,
            wifiSSID = wifiSSID,
            ipAddress = ipAddress,
            macAddress = macAddress,
            networkType = networkType,
            signalStrength = signalStrength
        )
    }

    private fun collectSensorInfo(): SensorInfo {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val sensorDataList = sensors.map { sensor ->
            SensorData(
                name = sensor.name,
                type = getSensorTypeName(sensor.type),
                vendor = sensor.vendor,
                version = sensor.version,
                power = sensor.power,
                resolution = sensor.resolution,
                maximumRange = sensor.maximumRange
            )
        }

        return SensorInfo(sensors = sensorDataList)
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            Sensor.TYPE_LIGHT -> "Light"
            Sensor.TYPE_PRESSURE -> "Pressure"
            Sensor.TYPE_TEMPERATURE -> "Temperature"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_ORIENTATION -> "Orientation"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
            else -> "Unknown ($type)"
        }
    }

    @SuppressLint("MissingPermission")
    private fun collectCameraInfo(): CameraInfo {
        val cameras = mutableListOf<CameraData>()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraIds = cameraManager.cameraIdList

                for (cameraId in cameraIds) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)

                    val facing = when (characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)) {
                        android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                        android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> "Back"
                        android.hardware.camera2.CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                        else -> "Unknown"
                    }

                    val streamConfigurationMap = characteristics.get(
                        android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                    )

                    val outputSizes = streamConfigurationMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                    val resolutions = outputSizes?.map { "${it.width}x${it.height}" } ?: emptyList()

                    val maxSize = outputSizes?.maxByOrNull { it.width * it.height }
                    val megapixels = if (maxSize != null) {
                        String.format("%.1f MP", (maxSize.width * maxSize.height) / 1000000.0)
                    } else {
                        "Unknown"
                    }

                    cameras.add(
                        CameraData(
                            id = cameraId,
                            facing = facing,
                            megapixels = megapixels,
                            supportedResolutions = resolutions
                        )
                    )
                }
            } else {
                // Fallback for older Android versions
                val numberOfCameras = Camera.getNumberOfCameras()
                for (i in 0 until numberOfCameras) {
                    val cameraInfo = Camera.CameraInfo()
                    Camera.getCameraInfo(i, cameraInfo)

                    val facing = when (cameraInfo.facing) {
                        Camera.CameraInfo.CAMERA_FACING_FRONT -> "Front"
                        Camera.CameraInfo.CAMERA_FACING_BACK -> "Back"
                        else -> "Unknown"
                    }

                    cameras.add(
                        CameraData(
                            id = i.toString(),
                            facing = facing,
                            megapixels = "Unknown",
                            supportedResolutions = emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle camera access exceptions
        }

        return CameraInfo(cameras = cameras)
    }

    private fun collectSystemInfo(): SystemInfo {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val uptimeSeconds = uptimeMillis / 1000
        val uptimeHours = uptimeSeconds / 3600
        val uptimeMinutes = (uptimeSeconds % 3600) / 60
        val uptimeSecondsRemainder = uptimeSeconds % 60

        val uptime = String.format("%02d:%02d:%02d", uptimeHours, uptimeMinutes, uptimeSecondsRemainder)

        val bootTime = SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - uptimeMillis))

        val timezone = TimeZone.getDefault().displayName
        val locale = Locale.getDefault().toString()
        val javaVmVersion = System.getProperty("java.vm.version") ?: "Unknown"
        val javaVmName = System.getProperty("java.vm.name") ?: "Unknown"

        return SystemInfo(
            uptime = uptime,
            bootTime = bootTime,
            timezone = timezone,
            locale = locale,
            javaVmVersion = javaVmVersion,
            javaVmName = javaVmName
        )
    }
}
