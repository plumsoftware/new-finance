package ru.plumsoftware.finance.domain.model

data class AppSettings(
    val defaultCurrencyCode: String = "RUB",
    val selectedAccountId: Long = 1L,
    val onboardingCompleted: Boolean = false,
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val permissionsPromptHidden: Boolean = false,
    val initialBalancePromptCompleted: Boolean = false,
    /** Общий бюджет месяца (§8.1). `null` — сумма лимитов категорий или средние расходы за 3 месяца. */
    val monthlyBudgetMinor: Long? = null,
    /** Скрывать суммы при запуске (§6.13). */
    val hideAmountsOnLaunch: Boolean = false,
    /** Напоминание о записи (§6.13, §10). */
    val reminderEnabled: Boolean = true,
    val reminderMinuteOfDay: Int = 21 * 60,
    /** Уведомления о лимитах 80% и 100%. */
    val limitNotificationsEnabled: Boolean = true,
    /** Уведомления о регулярных платежах за день до списания. */
    val recurringNotificationsEnabled: Boolean = true,
    /** Категории, исключённые из Аналитики (§6.4 п.5). */
    val analyticsExcludedCategoryIds: Set<Long> = emptySet(),
    /** Эпоха-день, до которого скрыт совет Коппи на Главной (§6.1.4). */
    val kopiTipHiddenEpochDay: Long = -1L,
    /** Последний нейтральный совет и эпоха-день его показа (§8.6 п.6). */
    val lastNeutralTipIndex: Int = -1,
    val lastNeutralTipEpochDay: Long = -1L,
    /** Эпоха-день, отмеченный «Сегодня без трат» (§8.7). */
    val noSpendEpochDays: Set<Long> = emptySet(),
    /** Количество выгруженных отчётов и сканов чеков — для достижений (§6.12). */
    val reportsExported: Int = 0,
    val receiptsScanned: Int = 0,
    /** Подсказки, уже показанные при первом заходе на экран (например, «свайп для удаления»). */
    val seenHints: Set<String> = emptySet(),
)
