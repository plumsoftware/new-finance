package ru.plumsoftware.finance.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import ru.plumsoftware.finance.data.local.datastore.PushTokenDataStore
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository

class PushMessagingRepositoryImpl(
    private val pushTokenDataStore: PushTokenDataStore,
) : PushMessagingRepository {

    override val fcmToken: Flow<String?> = pushTokenDataStore.fcmToken

    override suspend fun saveFcmToken(token: String) {
        pushTokenDataStore.saveFcmToken(token)
    }

    override suspend fun refreshFcmToken(): String? = runCatching {
        val token = FirebaseMessaging.getInstance().token.await()
        saveFcmToken(token)
        token
    }.getOrNull()
}
