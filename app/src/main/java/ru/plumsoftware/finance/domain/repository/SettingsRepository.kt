package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.AppSettings

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
