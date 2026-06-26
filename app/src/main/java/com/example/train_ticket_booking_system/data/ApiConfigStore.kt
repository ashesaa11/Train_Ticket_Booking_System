package com.example.train_ticket_booking_system.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.apiConfigStore: DataStore<Preferences> by preferencesDataStore(name = "ai_config")

class ApiConfigStore(private val context: Context) {
    companion object {
        private val KEY_API_URL = stringPreferencesKey("api_url")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_MODEL_NAME = stringPreferencesKey("model_name")
    }

    val config: Flow<ApiConfig> = context.apiConfigStore.data.map { prefs ->
        ApiConfig(
            apiUrl = prefs[KEY_API_URL] ?: "https://api.deepseek.com/v1",
            apiKey = prefs[KEY_API_KEY] ?: "",
            modelName = prefs[KEY_MODEL_NAME] ?: "deepseek-chat"
        )
    }

    suspend fun saveConfig(config: ApiConfig) {
        context.apiConfigStore.edit { prefs ->
            prefs[KEY_API_URL] = config.apiUrl
            prefs[KEY_API_KEY] = config.apiKey
            prefs[KEY_MODEL_NAME] = config.modelName
        }
    }
}

data class ApiConfig(
    val apiUrl: String = "https://api.deepseek.com/v1",
    val apiKey: String = "",
    val modelName: String = "deepseek-chat"
)
