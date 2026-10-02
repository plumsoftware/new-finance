# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Язык

Отвечать пользователю только по-русски. Комментарии в коде и документация в `docs/` — тоже на русском.

## Проект

«Контроль расходов» — Android-приложение учёта финансов (Kotlin, Jetpack Compose, Material3, Room, Koin, Navigation Compose, DataStore, WorkManager, Vico, Yandex Mobile Ads, Firebase опционально). Один модуль `:app`, пакет `ru.plumsoftware.finance`. Актуальные SDK/версии — в `app/build.gradle.kts` и `gradle/libs.versions.toml` (README в этой части устарел).

## Команды

Сборка разделена на product flavors измерения `distribution`: `RuStore` (PLATFORM=1), `GooglePlay` (2), `HuaweiappGallery` (3). Поэтому задачи именуются с flavor'ом:

```bash
./gradlew :app:assembleRuStoreDebug
./gradlew :app:testRuStoreDebugUnitTest
./gradlew :app:testRuStoreDebugUnitTest --tests "ru.plumsoftware.finance.ExampleUnitTest"
./gradlew :app:connectedRuStoreDebugAndroidTest   # нужен эмулятор/устройство
./gradlew :app:connectedRuStoreDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ru.plumsoftware.finance.data.local.database.MigrationTest
./gradlew :app:lintRuStoreDebug
```

- Firebase/FCM включается, только если существует `app/google-services.json` (тогда применяется плагин google-services и `BuildConfig.FIREBASE_ENABLED = true`). Зависимости Firebase подключены всегда.
- `BuildConfig.PLATFORM` определяет ID рекламных блоков и ссылку на магазин в `AppConfig.kt`; реклама реально работает только для RuStore, в debug — demo-блоки.

## Архитектура

Подробно — `docs/ARCHITECTURE_LAYERS.md` (обязателен к прочтению при добавлении фич). Кратко:

- `presentation/<feature>/` — Compose-экраны `*Screen.kt` + `*ViewModel.kt` со `StateFlow`. Экран не ходит в БД, только ViewModel → интерфейс репозитория.
- `ui/ds/` — дизайн-система редизайна (карточки, сегменты, чипы, переключатель, клавиатура суммы, графики, нижние панели, `MascotSnackbar`). Токены — `FinanceTheme.colors` (`ui/theme/FinanceColors.kt`, пары light/dark), типографика — `FinanceType`. Форматирование денег/дат/склонений — `presentation/common/Fmt.kt` (`Money`, `DateFmt`, `plural`). Новые экраны собирать из `ui/ds`, не из legacy `ui/components`.
- `domain/` — модели, интерфейсы репозиториев, бизнес-логика; без Compose и Room. Чистые расчёты ТЗ §8 (бюджет/прогноз, аналитика, калькуляторы, советы Коппи, достижения, QR чека) лежат в `domain/budget|analytics|calculators|insights|achievements|receipt` и покрыты `app/src/test/.../RedesignLogicTest.kt`.
- `data/` — реализации репозиториев, Room (`local/entity|dao|database`), DataStore (`AppSettings` — все пользовательские флаги, включая бюджет месяца и исключённые категории Аналитики), мапперы, Firebase, WorkManager-воркеры (`work/`, `notifications/DailyNotificationsWorker`), бэкап `.owlbackup` (`backup/`), отчёты PDF/Excel/CSV (`report/`), Remote Config и «Мои расчёты» (`config/`).
- Глобальный снекбар Коппи: `LocalMascotSnackbar.current.show(text, Kopi.HAPPY)`; хост — в `FinanceApp`.
- `di/` — Koin-модули `DataModule`, `PresentationModule`, `AdsModule`. Новые ViewModel/репозитории регистрировать здесь; ViewModel не создавать вручную.
- Навигация: маршруты в `ui/AppRoute.kt`, deep links `finance://app/…` в `navigation/` (`AppDeepLinks`, `NavDeepLinks`), `NavHost` в `presentation/FinanceApp.kt`. Маршруты должны совпадать между `AppRoute`, `NavDeepLinks` и `docs/DEEPLINKS.md`.
- Реклама: ID только в `AppConfig`; interstitial — через `InterstitialAdEffect` (кулдаун 3 мин, `InterstitialAdManager` — singleton); баннеры — через `AdBannerBottomBar` в `Scaffold.bottomBar`. См. `docs/YANDEX_ADS.md`.
- Виджет быстрого добавления: `widget/QuickAddWidgetProvider` + ресурсы в `res/layout|xml`.

## Правила

- Все пользовательские строки — в `res/values/strings.xml` (`stringResource` / `context.getString`).
- Новые строки — в `res/values*/strings_redesign.xml` (en в `values`, ru в `values-ru`), склонения — `plurals_redesign.xml` через `pluralStringResource`. Имена не должны пересекаться со `strings.xml`.
- Room: `FinanceDatabase` (`exportSchema = false`). При изменении схемы — поднять `version`, добавить миграцию в `DatabaseMigrations.kt`, зарегистрировать в `addMigrations(...)` и по возможности покрыть `MigrationTest` (androidTest). Деньги хранятся в минорных единицах (`amountMinor`).
- При добавлении фичи обновлять соответствующий документ в `docs/`.
- Маскот приложения — сова **Коппи** (именно так, см. `docs/MASCOT_STYLE_GUIDE.md`).

## ТЗ на редизайн

`TZ/ТЗ.md` — техническое задание на редизайн (дизайн-токены, исправления, экраны). Рядом: `TZ/prototype/` (кликабельный HTML-прототип), `TZ/icons/` (SVG, Android VectorDrawable, PNG), `TZ/mascot/`.
