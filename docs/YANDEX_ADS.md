# Реклама Yandex Mobile Ads (RuStore)

Монетизация через [Рекламную сеть Яндекса](https://ads.yandex.com/) и **Yandex Mobile Ads SDK 8**.  
В проекте используются **межстраничная реклама** (interstitial) и **адаптивные sticky-баннеры** внизу экрана.

Официальная документация:

- [Быстрый старт Android](https://ads.yandex.com/helpcenter/ru/dev/android/quick-start)
- [Межстраничная реклама](https://ads.yandex.com/helpcenter/ru/dev/android/interstitial)
- [Адаптивный sticky-баннер](https://ads.yandex.com/helpcenter/ru/dev/android/adaptive-sticky-banner)
- [Демоблоки для тестирования](https://ads.yandex.com/helpcenter/ru/dev/android/demo-blocks)
- [Миграция на SDK 8](https://ads.yandex.com/helpcenter/ru/dev/android/release/8-0-0-migration)

---

## Платформы и `AppConfig`

Идентификаторы рекламных блоков задаются в `app/src/main/java/ru/plumsoftware/finance/AppConfig.kt`.

| `BuildConfig.PLATFORM` | Магазин | Реклама |
|------------------------|---------|---------|
| `1` | RuStore | боевые `R-M-…` в release, демо в debug |
| `2` | Google Play | пустые строки — реклама не показывается |
| `3` | Huawei App Gallery | пустые строки — реклама не показывается |

Значение `PLATFORM` задаётся в `app/build.gradle.kts` → `buildConfigField("int", "PLATFORM", "1")`.

При пустом `adUnitId` баннеры не рисуются, межстраничная реклама сразу вызывает `onContinue` без показа.

---

## Таблица рекламных блоков (RuStore)

| Назначение | Поле в `AppConfig` | Ad unit ID (release) |
|------------|-------------------|----------------------|
| Баннер — категории | `bannerCategories` | `R-M-19374501-1` |
| Баннер — лимиты | `bannerLimits` | `R-M-19374501-3` |
| Баннер — повторяющиеся операции | `bannerRecurring` | `R-M-19374501-4` |
| Межстраничная — после операции | `interstitialAfterCreateTransaction` | `R-M-19374501-5` |
| Межстраничная — после цели | `interstitialAfterCreateGoal` | `R-M-19374501-6` |
| Межстраничная — после экоактива | `interstitialAfterCreateSmartSavings` | `R-M-19374501-7` |

### Debug-сборка

В `BuildConfig.DEBUG` для RuStore подставляются демоблоки:

| Тип | Demo ad unit |
|-----|----------------|
| Межстраничная | `demo-interstitial-yandex` |
| Баннер (sticky) | `demo-banner-yandex` |

Перед публикацией в RuStore проверьте release-сборку с реальными `R-M-…`.

### Интервал межстраничной рекламы

Общий кулдаун для **всех** межстраничных показов: **3 минуты**.

```kotlin
AppConfig.INTERSTITIAL_COOLDOWN_MS // 3 * 60 * 1000L
```

Если с последнего показа прошло меньше 3 минут, `tryShow` не показывает рекламу и сразу выполняет продолжение сценария (закрытие экрана и т.д.).

---

## Зависимость и инициализация

### Gradle

`gradle/libs.versions.toml`:

```toml
yandexMobileAds = "8.0.0"
yandex-mobileads = { group = "com.yandex.android", name = "mobileads", version.ref = "yandexMobileAds" }
```

`app/build.gradle.kts`:

```kotlin
implementation(libs.yandex.mobileads)
```

Репозитории `google()` и `mavenCentral()` уже указаны в `settings.gradle.kts`.

### Инициализация SDK

`FinanceApplication.onCreate()`:

```kotlin
MobileAds.initialize(this) {
    getKoin().get<InterstitialAdManager>()
        .preload(InterstitialPlacement.TRANSACTION.adUnitId())
}
```

Koin-модуль: `di/AdsModule.kt` → подключается в `modules(dataModule, presentationModule, adsModule)`.

---

## Структура кода

```
app/src/main/java/ru/plumsoftware/finance/
├── AppConfig.kt                    # ad unit ID по платформе
├── ads/
│   ├── InterstitialPlacement.kt    # TRANSACTION | GOAL | SMART_SAVINGS
│   └── InterstitialAdManager.kt    # загрузка, кулдаун, показ
├── di/
│   └── AdsModule.kt
└── ui/ads/
    ├── InterstitialAdEffect.kt     # Compose: показ после успешного сохранения
    ├── AdBannerBottomBar.kt        # Обёртка: max 70dp + navigationBarsPadding
    └── StickyAdBanner.kt           # BannerAdView через AndroidView

app/src/main/res/drawable/
├── ic_shortcut_*.xml               # (ярлыки лаунчера, не реклама)
└── ic_notification.xml             # иконка push

app/src/main/res/xml/
└── shortcuts.xml
```

---

## Межстраничная реклама (interstitial)

### Где показывается

| Событие | Экран | `InterstitialPlacement` |
|---------|-------|-------------------------|
| Успешное сохранение операции | `AddTransactionScreen` | `TRANSACTION` |
| Успешное создание/сохранение цели | `CreateGoalScreen` | `GOAL` |
| Успешное создание экоактива | `CreateSmartSavingsScreen` | `SMART_SAVINGS` |

### Как подключено в UI

На экране используется `InterstitialAdEffect`:

```kotlin
InterstitialAdEffect(
    placement = InterstitialPlacement.TRANSACTION,
    trigger = state.saved,           // true после успешного сохранения
    onContinue = onBack,             // закрыть экран / вернуться назад
)
```

Поведение:

1. При `trigger == true` вызывается `InterstitialAdManager.tryShow`.
2. При открытии экрана создаётся `LaunchedEffect(placement)` → `preload` для нужного блока.
3. Если реклама показана — `onContinue` после `onAdDismissed` или ошибки показа.
4. Если реклама не загружена / кулдаун / пустой ad unit — `onContinue` сразу.

**Важно:** не вызывайте `onContinue` параллельно в другом `LaunchedEffect` на тот же `trigger` — иначе экран закроется до рекламы.

### Логика `InterstitialAdManager`

- Один `InterstitialAdLoader` на приложение.
- В памяти хранится **одно** загруженное объявление (последний успешный `preload`).
- Перед показом проверяются: непустой ad unit, кулдаун 3 мин, совпадение `loadedAdUnitId` с запрошенным placement.
- После показа — снова `preload` того же блока.

---

## Sticky-баннеры

### Где показываются

| Экран | `AppConfig` | Расположение |
|-------|-------------|--------------|
| `LimitsScreen` | `bannerLimits` | `Scaffold.bottomBar` → `AdBannerBottomBar` |
| `CategoriesScreen` | `bannerCategories` | `Scaffold.bottomBar` → `AdBannerBottomBar` |
| `RecurringScreen` | `bannerRecurring` | `Scaffold.bottomBar` → `AdBannerBottomBar` |

### Как подключено

```kotlin
Scaffold(
    bottomBar = {
        AdBannerBottomBar(adUnitId = AppConfig.bannerLimits)
    },
) { padding ->
    // контент
}
```

`AdBannerBottomBar`:

- обёртка над `StickyAdBanner` (`BannerAdView` через `AndroidView`);
- высота объявления: не больше **70dp** (`AdBannerMaxHeight`);
- `navigationBarsPadding()` — только отступ над системной жестовой зоной;
- на **экране целей** (`GoalsScreen`) баннер **не показывается** (есть нижняя навигация).

`StickyAdBanner` — низкоуровневый загрузчик; в UI используйте `AdBannerBottomBar`.

---

## Тестирование

### 1. Debug на устройстве

Соберите **debug**-вариант (`PLATFORM = 1`). Должны подставиться `demo-interstitial-yandex` и `demo-banner-yandex`.

### 2. Logcat

```bash
adb logcat -v brief '*:S YandexAds'
```

Успешная интеграция межстраничной рекламы (пример из документации Яндекса):

```
I/YandexAds: [Integration] Ad type interstitial was integrated successfully
```

### 3. Release перед публикацией

- Соберите **release** (не debug).
- Убедитесь, что в логах/интерфейсе РСЯ используются блоки `R-M-19374501-*`, а не demo.
- Пройдите сценарии: сохранение операции, цели, экоактива; откройте экраны с баннерами.

### 4. Частые причины «реклама не видна»

| Симптом | Причина |
|---------|---------|
| Нет баннера / interstitial | `PLATFORM != 1` → пустые ad unit |
| Interstitial не после сохранения | Объявление ещё не загрузилось — повторите действие через несколько секунд |
| Interstitial не чаще 3 мин | Работает кулдаун `INTERSTITIAL_COOLDOWN_MS` |
| Пустой баннер | Ошибка загрузки — смотрите Logcat `YandexAds`; не перезагружайте в цикле из `onAdFailedToLoad` |

---

## Добавить новый рекламный блок

### Новый sticky-баннер

1. Создайте блок в [интерфейсе РСЯ](https://partner.yandex.ru/) → скопируйте `R-M-…`.
2. Добавьте константу в `AppConfig.RuStore` и публичное поле `banner… = adUnit(…)`.
3. На экране: `AdBannerBottomBar(adUnitId = AppConfig.banner…)` в `Scaffold.bottomBar`.
4. Обновите этот файл (таблица блоков).

### Новая межстраничная точка

1. Ad unit в `AppConfig` + значение в `RuStore` / demo.
2. Добавьте вариант в `InterstitialPlacement` и ветку в `adUnitId()`.
3. На экране после успешного действия: `InterstitialAdEffect(placement = …, trigger = …, onContinue = …)`.
4. Уберите дублирующий `LaunchedEffect`, который вызывает `onContinue` на тот же `trigger`.
5. Обновите этот файл.

### Другой магазин (Google Play)

В `AppConfig.adUnit()` добавьте ветку для `BuildConfig.PLATFORM == 2` с нужными ID (сейчас `""`).

---

## Регистрация приложения в РСЯ

1. [Рекламная сеть Яндекса](https://partner.yandex.ru/) — войти / зарегистрироваться.
2. Добавить приложение с package name **`ru.plumsoftware.finance`** (как в `applicationId`).
3. Создать рекламные блоки нужных форматов (баннер, interstitial).
4. Прописать ID в `AppConfig.kt` (секция `RuStore`).
5. При необходимости настроить `app-ads.txt` — [FAQ Яндекса](https://ads.yandex.com/helpcenter/ru/support/faq/app-ads-txt).

---

## Связанные документы

| Документ | Содержание |
|----------|------------|
| [ARCHITECTURE_LAYERS.md](ARCHITECTURE_LAYERS.md) | Слой рекламы в архитектуре |
| [DEEPLINKS.md](DEEPLINKS.md) | Deep links и push (отдельно от РСЯ) |
| [FIREBASE_SETUP.md](FIREBASE_SETUP.md) | Push / In-App (Firebase) |
