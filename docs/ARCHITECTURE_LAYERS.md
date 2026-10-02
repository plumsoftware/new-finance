# Архитектура по слоям

Документ описывает структуру проекта `finance` по слоям, их ответственность и правила взаимодействия.

## 1) Presentation Layer (`app/src/main/java/.../presentation`, `ui`)

**Что входит:**
- экраны Compose (`.../presentation/*Screen.kt`)
- `ViewModel` для экранов (`.../presentation/*ViewModel.kt`)
- дизайн-система редизайна (`.../ui/ds`): `FCard`, `InkCard`, `SectionTitle`, `ListRow`, `SettingsRow`,
  `SegmentedInk/Light`, `FChip`, `FSwitch`, кнопки, `FProgressBar`, `ProgressRing`, `DayStrip`, `BarChart`,
  `AmountKeypad`, `FBottomSheet`, `PeriodPickerSheet`, `MascotTip`, `MascotSnackbar`, `EmptyState`
- токены (`.../ui/theme/FinanceColors.kt`, `FinanceType.kt`): доступ через `FinanceTheme.colors`, пары light/dark
- форматтеры (`.../presentation/common/Fmt.kt`): `Money`, `DateFmt`, `plural()` — по ТЗ §11
- legacy-компоненты (`.../ui/components`) — только для ещё не переведённых экранов

**Ответственность:**
- отрисовка интерфейса
- обработка пользовательских действий
- хранение экранного состояния
- преобразование данных в UI-модель

**Правила:**
- пользовательские строки только из ресурсов (новые — в `strings_redesign.xml`, склонения — `plurals_redesign.xml`)
- экран не ходит напрямую в БД
- доступ к данным только через репозитории из `domain`
- цвета/типографика новых экранов — только из `FinanceTheme.colors` / `FinanceType`, не хардкодить
- глобальные сообщения — `LocalMascotSnackbar.current.show(...)` (хост в `FinanceApp`)

## 2) Domain Layer (`app/src/main/java/.../domain`)

**Что входит:**
- бизнес-модели (`.../domain/model`)
- интерфейсы репозиториев (`.../domain/repository`)
- бизнес-логика/движки (`.../domain/insights`, `.../domain/notifications`)
- чистые расчёты без Android API (покрыты `RedesignLogicTest`):
  - `domain/budget` — дневной лимит, прогноз, полоса дней (§8.1–8.2), `BudgetService`, ближайшие списания
  - `domain/analytics` — периоды, бакеты, сравнение (§8.3)
  - `domain/calculators` — формулы 7 калькуляторов (§8.5)
  - `domain/insights/KopiTips` — советы Коппи по приоритетам (§8.6)
  - `domain/achievements` — 12 достижений (§6.12)
  - `domain/receipt` — разбор QR-кода чека ФНС

**Ответственность:**
- бизнес-правила и инварианты
- контракты доступа к данным
- расчеты, не зависящие от UI/Android API

**Правила:**
- не зависит от Compose/UI
- не знает о Room/DAO напрямую

## 3) Data Layer (`app/src/main/java/.../data`)

**Что входит:**
- реализации репозиториев (`.../data/repository`)
- Room: `entity`, `dao`, `database`
- DataStore (`.../data/local/datastore`)
- mapper'ы (`.../data/mapper`)
- интеграции Firebase (`.../data/firebase`)
- отчёты и экспорт PDF/Excel/CSV (`.../data/report`, §9)
- локальные уведомления: каналы, пороги лимитов, ежедневное напоминание (`.../data/notifications`, §10)
- удалённая конфигурация калькуляторов и «Мои расчёты» (`.../data/config`)

**Ответственность:**
- чтение/запись данных
- преобразование между entity и domain model
- синхронизация разных источников данных

**Правила:**
- возвращает в верхние слои domain-модели
- не содержит UI-логики

## 4) DI Layer (`app/src/main/java/.../di`)

**Что входит:**
- `DataModule.kt`
- `PresentationModule.kt`
- `AdsModule.kt`

**Ответственность:**
- регистрация зависимостей (Koin)
- создание `ViewModel`, репозиториев, сервисов
- `InterstitialAdManager` (singleton)

**Правила:**
- зависимости задаются через конструкторы
- `ViewModel` не создаются вручную в UI

## 5) Ads Layer (`app/src/main/java/.../ads`, `ui/ads`, `AppConfig.kt`)

**Что входит:**
- идентификаторы блоков: `AppConfig` (по `BuildConfig.PLATFORM` и debug/release)
- `InterstitialAdManager`, `InterstitialPlacement`
- Compose: `InterstitialAdEffect`, `AdBannerBottomBar`, `StickyAdBanner`
- инициализация `MobileAds` в `FinanceApplication`

**Ответственность:**
- загрузка и показ межстраничной рекламы (кулдаун 3 мин между любыми interstitial)
- sticky-баннеры внизу отдельных экранов
- для RuStore — боевые `R-M-…`; для других платформ — пустые ID (реклама отключена)

**Правила:**
- ad unit ID только в `AppConfig`, не хардкодить в экранах
- межстраничная реклама — через `InterstitialAdEffect`, не дублировать `onContinue` в других эффектах
- баннеры — через `AdBannerBottomBar` в `Scaffold.bottomBar` (не на `GoalsScreen`)
- подробности: [YANDEX_ADS.md](YANDEX_ADS.md)

## 6) Navigation Layer (`app/src/main/java/.../navigation`, `ui/AppRoute.kt`, `presentation/FinanceApp.kt`)

**Что входит:**
- маршруты: `AppRoute`
- deep links: `AppDeepLinks`, `NavDeepLinks`
- `NavHost` и связка экранов: `FinanceApp`

**Ответственность:**
- единая карта экранов
- навигация внутри приложения
- обработка deeplink-переходов

**Правила:**
- маршруты должны совпадать между `AppRoute`, `NavDeepLinks`, `DEEPLINKS.md`
- все новые deeplink-роуты документируются

## 7) Widget Layer (`app/src/main/java/.../widget`, `app/src/main/res/layout|xml|drawable`)

**Что входит:**
- провайдер виджета (`QuickAddWidgetProvider`)
- layout и metadata виджета
- отдельные стили/цвета виджета

**Ответственность:**
- запуск быстрых сценариев из launcher
- deeplink-переходы в экраны приложения

## 8) Resource Layer (`app/src/main/res`)

**Что входит:**
- `values/strings.xml`, `colors.xml`, темы, drawable, layout

**Ответственность:**
- все пользовательские тексты
- централизация цветов/размеров/тем

**Правила:**
- пользовательский текст хранится в `strings.xml`
- в коде используются `stringResource(...)` или `context.getString(...)`

## 9) Текущий поток данных

1. UI вызывает действие во `ViewModel`.
2. `ViewModel` обращается к интерфейсу репозитория (`domain/repository`).
3. Реализация репозитория (`data/repository`) работает с DAO/DataStore/Firebase.
4. Результат возвращается в `ViewModel`.
5. `ViewModel` обновляет `StateFlow`, UI перерисовывается.

## 10) Чек-лист при добавлении новой фичи

- добавить модели и контракты в `domain`
- реализовать доступ к данным в `data`
- зарегистрировать зависимости в `di`
- сделать экран + `ViewModel` в `presentation`
- добавить route/deeplink в `navigation`
- добавить все пользовательские строки в `strings.xml`
- обновить документацию в `docs/` (при рекламе — [YANDEX_ADS.md](YANDEX_ADS.md))
