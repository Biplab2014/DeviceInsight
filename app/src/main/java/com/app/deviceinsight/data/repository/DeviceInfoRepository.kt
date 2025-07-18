package com.app.deviceinsight.data.repository

import com.app.deviceinsight.data.collectors.DeviceInfoCollector
import com.app.deviceinsight.data.models.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceInfoRepository(
    private val deviceInfoCollector: DeviceInfoCollector
) {
    
    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        deviceInfoCollector.collectDeviceInfo()
    }
    
    suspend fun refreshDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        deviceInfoCollector.collectDeviceInfo()
    }
}
