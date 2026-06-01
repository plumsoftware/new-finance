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
| История | `finance://app/history` | `history` |
| Аналитика | `finance://app/analytics` | `analytics` |
| Настройки | `finance://app/settings` | `settings` |

### Операции и уведомления

| Экран | URI | Маршрут |
|-------|-----|---------|
| Добавить транзакцию | `finance://app/add_transaction` | `add_transaction` |
| Уведомления | `finance://app/notifications` | `notifications` |
| Лимиты | `finance://app/limits` | `limits` |

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

> **Примечание:** если онбординг уже пройден, deep link на `onboarding` всё равно откроет экран онбординга (можно использовать для тестов).

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

# Лимиты
adb shell am start -a android.intent.action.VIEW -d "finance://app/limits" ru.plumsoftware.finance

# Детали экопокупки (id=1)
adb shell am start -a android.intent.action.VIEW -d "finance://app/smart_savings/detail/1" ru.plumsoftware.finance

# Создать цель
adb shell am start -a android.intent.action.VIEW -d "finance://app/goals/create" ru.plumsoftware.finance

# Импорт
adb shell am start -a android.intent.action.VIEW -d "finance://app/settings/import" ru.plumsoftware.finance

# Новая категория расходов
adb shell am start -a android.intent.action.VIEW -d "finance://app/settings/categories/edit?type=EXPENSE" ru.plumsoftware.finance
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
| `navigation/AppDeepLinks.kt` | Константы scheme/host, фабрики URI, проверка deep link |
| `navigation/NavDeepLinks.kt` | `navDeepLink` patterns для NavHost |
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

## Push-уведомления (рекомендация)

В payload FCM можно передавать поле `deep_link`:

```json
{
  "data": {
    "deep_link": "finance://app/limits"
  }
}
```

В `FinanceMessagingService` создайте `Intent` с `ACTION_VIEW` и этим URI — `MainActivity` обработает автоматически.

---

## Программное построение URI (Kotlin)

```kotlin
import ru.plumsoftware.finance.navigation.AppDeepLinks

AppDeepLinks.home()
AppDeepLinks.limits()
AppDeepLinks.smartSavingsDetail(assetId = 42L)
AppDeepLinks.goals()
AppDeepLinks.goalsCreate(goalId = 7L)
AppDeepLinks.categoryEdit(categoryId = 5L, type = "EXPENSE")
AppDeepLinks.importPicker()
```
