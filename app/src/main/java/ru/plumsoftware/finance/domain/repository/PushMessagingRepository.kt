package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow

interface PushMessagingRepository {
    val fcmToken: Flow<String?>
    suspend fun saveFcmToken(token: String)
    suspend fun refreshFcmToken(): String?
}
