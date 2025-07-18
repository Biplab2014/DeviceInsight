package com.app.deviceinsight.domain.usecases

import com.app.deviceinsight.data.repository.DeviceInfoRepository
import com.app.deviceinsight.domain.models.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDeviceInfoUseCase(
    private val repository: DeviceInfoRepository
) {
    
    operator fun invoke(): Flow<UiState> = flow {
        try {
            emit(UiState.Loading)
            val deviceInfo = repository.getDeviceInfo()
            emit(UiState.Success(deviceInfo))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    suspend fun refresh(): Flow<UiState> = flow {
        try {
            emit(UiState.Loading)
            val deviceInfo = repository.refreshDeviceInfo()
            emit(UiState.Success(deviceInfo))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
