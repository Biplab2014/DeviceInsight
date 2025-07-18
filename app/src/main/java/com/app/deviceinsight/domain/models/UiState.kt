package com.app.deviceinsight.domain.models

import com.app.deviceinsight.data.models.DeviceInfo

sealed class UiState {
    object Loading : UiState()
    data class Success(val deviceInfo: DeviceInfo) : UiState()
    data class Error(val message: String) : UiState()
}

data class DeviceInfoSection(
    val title: String,
    val items: List<DeviceInfoItem>,
    val isExpanded: Boolean = false
)

data class DeviceInfoItem(
    val label: String,
    val value: String
)
