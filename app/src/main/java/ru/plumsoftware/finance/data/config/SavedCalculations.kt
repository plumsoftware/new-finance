package ru.plumsoftware.finance.data.config

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Сохранённый расчёт для «Мои расчёты» (§6.8 п.5). */
data class SavedCalculation(
    val id: Long,
    val calculator: String,
    val title: String,
    val result: String,
    val details: String,
    val savedAtMillis: Long,
)

/** Простое локальное хранилище истории расчётов (до 100 записей). */
class SavedCalculationsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("saved_calculations", Context.MODE_PRIVATE)
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<SavedCalculation>> = _items.asStateFlow()

    fun add(calculator: String, title: String, result: String, details: String) {
        val now = System.currentTimeMillis()
        val updated = (listOf(SavedCalculation(now, calculator, title, result, details, now)) + _items.value).take(100)
        persist(updated)
    }

    fun delete(id: Long) = persist(_items.value.filterNot { it.id == id })

    private fun persist(list: List<SavedCalculation>) {
        _items.value = list
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id).put("calculator", it.calculator).put("title", it.title)
                    .put("result", it.result).put("details", it.details).put("savedAt", it.savedAtMillis),
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun load(): List<SavedCalculation> = runCatching {
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SavedCalculation(
                o.getLong("id"), o.getString("calculator"), o.getString("title"),
                o.getString("result"), o.optString("details"), o.getLong("savedAt"),
            )
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val KEY = "items"
    }
}
