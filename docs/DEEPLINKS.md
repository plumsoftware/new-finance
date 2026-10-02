# Deep Links — Контроль расходов

Приложение поддерживает навигацию через custom URI scheme **`finance://app/`**.

Package: `ru.plumsoftware.finance`

---

## Формат

```
finance://app/{path}?{query}
```

| Компонент | Значение |
|-----------|----------|
| **scheme** | `finance` |
| **host** | `app` |
| **path** | совпадает с маршрутом Jetpack Navigation (`AppRoute`) |

---

## Все deep links

### Основные экраны (нижняя навигация)

| Экран | URI | Маршрут |
|-------|-----|---------|
| Главная | `finance://app/home` | `home` |
| Расчёты | `finance://app/tools` | `tools` |
| Аналитика | `finance://app/analytics` | `analytics` |
| Настройки | `finance://app/settings` | `settings` |

### Калькуляторы (вкладка «Расчёты»)

| Экран | URI | Маршрут |
|-------|-----|---------|
| Кредитный калькулятор | `finance://app/calculator/credit` | `calculator/credit` |
| Калькулятор вклада | `finance://app/calculator/deposit` | `calculator/deposit` |
| Калькулятор цели | `finance://app/calculator/goal` | `calculator/goal` |
| Ипотечный калькулятор | `finance://app/calculator/mortgage` | `calculator/mortgage` |
| Досрочное погашение | `finance://app/calculator/early_repay` | `calculator/early_repay` |
| Аренда или ипотека | `finance://app/calculator/rent_vs_buy` | `calculator/rent_vs_buy` |
| Накопительный счёт | `finance://app/calculator/savings_account` | `calculator/savings_account` |
| Мои расчёты | `finance://app/calculator/saved` | `calculator/saved` |

### Операции и уведомления

| Экран | URI | Маршрут |
|-------|-----|---------|
| Добавить транзакцию | `finance://app/add_transaction` | `add_transaction` |
| Новая операция + сразу скан чека | `finance://app/add_transaction?scan=true` | `add_transaction?scan=true` |
| Новая операция с развёрнутыми недавними | `finance://app/add_transaction?recent=true` | `add_transaction?recent=true` |
| Уведомления | `finance://app/notifications` | `notifications` |
| Лимиты | `finance://app/limits` | `limits` |
| Достижения | `finance://app/achievements` | `achievements` |
| История операций | `finance://app/history` | `history` |

### Умная экономия

| Экран | URI | Маршрут |
|-------|-----|---------|
| Список экопокупок | `finance://app/smart_savings` | `smart_savings` |
| Создать экопокупку | `finance://app/smart_savings/create` | `smart_savings/create?assetId=` |
| Редактировать экопокупку | `finance://app/smart_savings/create?assetId=42` | `smart_savings/create?assetId=42` |
| Детали экопокупки | `finance://app/smart_savings/detail/42` | `smart_savings/detail/42` |

### Цели

| Экран | URI | Маршрут |
|-------|-----|---------|
| Все цели | `finance://app/goals` | `goals` |
| Создать цель | `finance://app/goals/create` | `goals/create?goalId=` |
| Редактировать цель | `finance://app/goals/create?goalId=7` | `goals/create?goalId=7` |

### Настройки — подразделы

| Экран | URI | Маршрут |
|-------|-----|---------|
| Категории | `finance://app/settings/categories` | `settings/categories` |
| Редактор категории (новая) | `finance://app/settings/categories/edit?type=EXPENSE` | `settings/categories/edit?categoryId=&type=EXPENSE` |
| Редактор категории (существующая) | `finance://app/settings/categories/edit?categoryId=5&type=EXPENSE` | `settings/categories/edit?categoryId=5&type=EXPENSE` |
| Экспорт | `finance://app/settings/export` | `settings/export` |
| Импорт (выбор файла) | `finance://app/settings/import` | `settings/import` |
| Повторяющиеся | `finance://app/settings/recurring` | `settings/recurring` |
| Разрешения | `finance://app/settings/permissions` | `settings/permissions` |

### Онбординг

| Экран | URI | Маршрут |
|-------|-----|---------|
| Онбординг | `finance://app/onboarding` | `onboarding` |

> **Примечание:** если онбординг уже пройден, deep link на `onboarding` всё равно откроет экран онбординга.

---

## Параметры

### `type` (редактор категории)
Допустимые значения: `EXPENSE`, `INCOME` (имя enum `CategoryType`).

### `categoryId` (редактор категории)
Long — ID существующей категории. Если не указан, открывается создание новой категории.

### `assetId` (умная экономия)
Long — ID экопокупки для деталей или редактирования.

### `goalId` (цели)
Long — ID цели для редактирования.

---

## Тестирование через ADB

```bash
# Главная
adb shell am start -a android.intent.action.VIEW -d "finance://app/home" ru.plumsoftware.finance

# Инструменты (экран калькуляторов)
adb shell am start -a android.intent.action.VIEW -d "finance://app/tools" ru.plumsoftware.finance

# Кредитный калькулятор
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/credit" ru.plumsoftware.finance

# Калькулятор вклада
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/deposit" ru.plumsoftware.finance

# Калькулятор цели
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/goal" ru.plumsoftware.finance

# Ипотечный калькулятор
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/mortgage" ru.plumsoftware.finance

# Досрочное погашение
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/early_repay" ru.plumsoftware.finance

# Аренда или ипотека
adb shell am start -a android.intent.action.VIEW -d "finance://app/calculator/rent_vs_buy" ru.plumsoftware.finance

# Детали экопокупки (id=1)
adb shell am start -a android.intent.action.VIEW -d "finance://app/smart_savings/detail/1" ru.plumsoftware.finance

# Создать цель
adb shell am start -a android.intent.action.VIEW -d "finance://app/goals/create" ru.plumsoftware.finance
```

---

## Архитектура

```
Intent (finance://…) 
  → MainActivity.handleIntent()
  → AppDeepLinks.isAppDeepLink() 
  → pendingDeepLinkIntent 
  → FinanceApp → NavController.handleDeepLink()
  → composable с navDeepLink { uriPattern = … }
```

### Файлы

| Файл | Назначение |
|------|------------|
| `navigation/AppDeepLinks.kt` | Константы scheme/host, фабрики URI, проверка deep link, `NavDeepLinks` |
| `AndroidManifest.xml` | `<intent-filter>` для `finance://app` |
| `MainActivity.kt` | Разделение deep link и import intent |
| `FinanceApp.kt` | `deepLinks` на каждом `composable`, обработка pending intent |

---

## Import vs Deep Link

| Intent | Обработка |
|--------|-----------|
| `finance://app/…` | Deep link → Navigation |
| `content://…` / `file://…` (.owlbackup, .json) | Import → копирование в cache → `import_preview/{path}` |
| `ACTION_SEND` с файлом | Import (как выше) |

Deep links и import **не пересекаются**: scheme `finance` обрабатывается до логики импорта.

---

## Push-уведомления (FCM)

### Firebase Console → Additional options → Custom data

| Key | Value (пример) | Обязательно |
|-----|----------------|-------------|
| `title` | Текст заголовка | нет* |
| `body` | Текст сообщения | нет* |
| `deep_link` | `finance://app/calculator/credit` | **нет** |

\*Для сохранения в историю при закрытом приложении удобнее передать `title` и `body` в Custom data.

Опционально в **Android Notification Channel** укажите `finance_push`.

---

## Ярлыки приложения (App Shortcuts)

Долгое нажатие на иконку лаунчера — 4 шортката (`res/xml/shortcuts.xml`):

| Ярлык | URI |
|-------|-----|
| Новая операция | `finance://app/add_transaction` |
| Калькуляторы | `finance://app/tools` |
| Лимиты на месяц | `finance://app/limits` |
| Аналитика расходов | `finance://app/analytics` |

Открывают [MainActivity](app/src/main/java/ru/plumsoftware/finance/MainActivity.kt) через `ACTION_VIEW`; навигация — `navigateAppDeepLink`.

---

## Программное построение URI (Kotlin)

```kotlin
import ru.plumsoftware.finance.navigation.AppDeepLinks

AppDeepLinks.home()
AppDeepLinks.limits()
AppDeepLinks.achievements()
AppDeepLinks.tools()                 // Направление на экран инструментов
AppDeepLinks.creditCalc()             // Кредитный калькулятор
AppDeepLinks.depositCalc()            // Калькулятор вклада
AppDeepLinks.goalCalc()               // Калькулятор цели
AppDeepLinks.mortgageCalc()           // Ипотечный калькулятор
AppDeepLinks.earlyRepayCalc()         // Досрочное погашение
AppDeepLinks.rentVsBuyCalc()          // Аренда или ипотека
AppDeepLinks.smartSavingsDetail(assetId = 42L)
AppDeepLinks.goals()
AppDeepLinks.goalsCreate(goalId = 7L)
AppDeepLinks.categoryEdit(categoryId = 5L, type = "EXPENSE")
AppDeepLinks.importPicker()
```