package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.datastore.SettingsDataStore
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = settingsDataStore.settings

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        settingsDataStore.update(transform)
    }
}
