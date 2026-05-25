package ru.plumsoftware.finance.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pushTokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "push_tokens",
)

class PushTokenDataStore(
    private val context: Context,
) {
    private val dataStore = context.pushTokenDataStore

    val fcmToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.FCM_TOKEN]
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { prefs ->
            prefs[Keys.FCM_TOKEN] = token
        }
    }

    private object Keys {
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
}
