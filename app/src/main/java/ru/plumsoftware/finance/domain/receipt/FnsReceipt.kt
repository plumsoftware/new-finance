package ru.plumsoftware.finance.domain.receipt

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Данные QR-кода кассового чека ФНС: `t=20260927T1315&s=990.00&fn=…&i=…&fp=…&n=1`.
 * Название магазина и позиции в QR не передаются — их можно получить только через API ФНС.
 */
data class FnsReceipt(
    val dateTime: LocalDateTime,
    val amountMinor: Long,
    val fn: String?,
    val fd: String?,
    val fp: String?,
    /** 1 — приход, 2 — возврат прихода, 3 — расход, 4 — возврат расхода. */
    val operationType: Int,
) {
    val isRefund: Boolean get() = operationType == 2 || operationType == 4

    companion object {
        private val formats = listOf(
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"),
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"),
        )

        fun parse(raw: String): FnsReceipt? {
            val params = raw.trim().split('&').mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null else part.substring(0, idx).lowercase() to part.substring(idx + 1)
            }.toMap()
            val t = params["t"] ?: return null
            val s = params["s"] ?: return null
            val dateTime = formats.firstNotNullOfOrNull { f -> runCatching { LocalDateTime.parse(t, f) }.getOrNull() }
                ?: return null
            val amount = s.replace(',', '.').toBigDecimalOrNull() ?: return null
            if (amount.signum() <= 0) return null
            return FnsReceipt(
                dateTime = dateTime,
                amountMinor = amount.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toLong(),
                fn = params["fn"],
                fd = params["i"],
                fp = params["fp"],
                operationType = params["n"]?.toIntOrNull() ?: 1,
            )
        }
    }
}
