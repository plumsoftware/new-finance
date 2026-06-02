# Контроль расходов

Android-приложение для учёта финансов: операции, категории, лимиты, цели, умные накопления (экоактива), аналитика, достижения.

**Package:** `ru.plumsoftware.finance`  
**Min SDK:** 24 · **Target SDK:** 36  
**Стек:** Kotlin, Jetpack Compose, Room, Koin, Navigation Compose, Firebase (опционально), Yandex Mobile Ads (RuStore).

---

## Сборка

```bash
./gradlew :app:assembleDebug
```

- **Firebase:** положите `app/google-services.json` — иначе сборка без FCM (см. [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)).
- **Платформа магазина:** `app/build.gradle.kts` → `buildConfigField("int", "PLATFORM", "1")`  
  `1` = RuStore, `2` = Google Play, `3` = Huawei. От этого зависят рекламные блоки в [AppConfig.kt](app/src/main/java/ru/plumsoftware/finance/AppConfig.kt).

---

## Документация (`docs/`)

| Файл | Описание |
|------|----------|
| [ARCHITECTURE_LAYERS.md](docs/ARCHITECTURE_LAYERS.md) | Слои приложения, правила зависимостей |
| [DEEPLINKS.md](docs/DEEPLINKS.md) | URI `finance://app/…`, ярлыки, push deep links |
| [YANDEX_ADS.md](docs/YANDEX_ADS.md) | Реклама РСЯ: баннеры, interstitial, `AppConfig`, тестирование |
| [FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) | Подключение Firebase / FCM |
| [IMPORT_EXPORT_BACKUP.md](docs/IMPORT_EXPORT_BACKUP.md) | Резервное копирование `.owlbackup` |
| [ONBOARDING_MASCOT_ASSETS.md](docs/ONBOARDING_MASCOT_ASSETS.md) | Онбординг и ассеты совы |
| [MASCOT_STYLE_GUIDE.md](docs/MASCOT_STYLE_GUIDE.md) | Стиль маскота Коппи |
| [MASCOT_EMPTY_STATES.md](docs/MASCOT_EMPTY_STATES.md) | Пустые состояния с маскотом |

---

## Быстрые ссылки по фичам

- **Навигация:** `ui/AppRoute.kt`, `navigation/AppDeepLinks.kt`, `presentation/FinanceApp.kt`
- **Реклама:** `AppConfig.kt`, `ads/`, `ui/ads/` → [YANDEX_ADS.md](docs/YANDEX_ADS.md)
- **Push:** `data/firebase/` → [FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md), [DEEPLINKS.md](docs/DEEPLINKS.md)
