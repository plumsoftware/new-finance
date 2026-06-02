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
| Достижения | `finance://app/achievements` | `achievements` |

### Умная экономия

| Экран | URI | Маршрут |
|-------|-----|---------|
| Список экопокупок | `finance://app/smart_savings` | `smart_savings` |
| Создать экопокупку | `finance://app/smart_savings/create` | `smart_savings/create?assetId=` |
| Редактировать экопокупку | `finance://app/smart_savings/create?assetId=42` | `smart_savings/create?assetId=42` |
| Детали экопокупки | `finance://app/smart_savings/detail/42` | `smart_savings/detail/42` |

### Достижения и геймификация

| Экран | URI | Маршрут |
|-------|-----|---------|
| Достижения | `finance://app/achievements` | `achievements` |

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

# Достижения
adb shell am start -a android.intent.action.VIEW -d "finance://app/achievements" ru.plumsoftware.finance

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

## Push-уведомления (FCM)

### Firebase Console → Additional options → Custom data

| Key | Value (пример) | Обязательно |
|-----|----------------|-------------|
| `title` | Текст заголовка | нет* |
| `body` | Текст сообщения | нет* |
| `deep_link` | `finance://app/achievements` | **нет** |

\*Для сохранения в историю при закрытом приложении удобнее передать `title` и `body` в Custom data.

Опционально в **Android Notification Channel** укажите `finance_push` (как в приложении).

**Иконка в шторке:** при закрытом приложении пуш с блоком *Notification* в консоли показывает **система FCM** — иконка задаётся в `AndroidManifest.xml` (`default_notification_icon` → `ic_notification`, колокольчик). Data-only пуши рисует приложение через `NotificationDisplayHelper` с той же иконкой.

Шаг **Notification** (заголовок и текст) заполняйте как обычно. Поле `deep_link` в Custom data **не обязательно**: без него пуш сохраняется и открывается как обычное уведомление (главный экран / история), кнопка **«Перейти»** в шторке и в списке **не показывается**.

Если `deep_link` задан и URI валидный (`finance://app/…`), при тапе откроется нужный экран; в шторке и в истории появится **«Перейти»**.

### Сохранение в историю при закрытом приложении

`FinanceMessagingService` запускает `PushNotificationPersistService`, который пишет пуш в Room и показывает локальное уведомление.

**Важно:** если в консоли Firebase заполнен только блок **Notification** (без data), Android в фоне показывает системный пуш **без** вызова `onMessageReceived` — запись в БД появится после **тапа** (через `MainActivity`). Чтобы сохранять сразу при доставке, отправляйте **data** (в Custom data достаточно `title` и `body`; `deep_link` — только если нужен переход на экран).

Также включите **Работа в фоне** (исключение из оптимизации батареи) в Настройки → Разрешения.

### REST API / data-only

В payload FCM можно передавать поле `deep_link`:

```json
{
  "data": {
    "deep_link": "finance://app/limits",
    "title": "Лимит",
    "body": "Проверьте траты по категории"
  }
}
```

```json
{
  "data": {
    "deep_link": "finance://app/achievements",
    "title": "Новое достижение!",
    "body": "Откройте экран достижений"
  }
}
```

`MainActivity` → `FinanceApp` вызывает `NavController.navigateAppDeepLink(intent)` (не `handleDeepLink`). В истории уведомлений — `navigateNotificationDeepLink`. Кнопка «Назад» везде использует `popBackStackOrHome()` (если стек пуст — переход на главную).

---

## Программное построение URI (Kotlin)

```kotlin
import ru.plumsoftware.finance.navigation.AppDeepLinks

AppDeepLinks.home()
AppDeepLinks.limits()
AppDeepLinks.achievements()
AppDeepLinks.smartSavingsDetail(assetId = 42L)
AppDeepLinks.goals()
AppDeepLinks.goalsCreate(goalId = 7L)
AppDeepLinks.categoryEdit(categoryId = 5L, type = "EXPENSE")
AppDeepLinks.importPicker()
```
