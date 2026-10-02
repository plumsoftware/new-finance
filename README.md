# Контроль расходов

Android-приложение для учёта финансов: операции, категории, лимиты, цели, умные накопления (экоактива), аналитика, достижения.

**Package:** `ru.plumsoftware.finance`  
**Min SDK:** 26 · **Target SDK:** 37  
**Стек:** Kotlin, Jetpack Compose, Room, Koin, Navigation Compose, Firebase (опционально), Yandex Mobile Ads (RuStore).

---

## Сборка

```bash
./gradlew :app:assembleRuStoreDebug
```

- **Firebase:** положите `app/google-services.json` — иначе сборка без FCM (см. [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)).
- **Платформа магазина:** product flavors `RuStore` (PLATFORM=1), `GooglePlay` (2), `HuaweiappGallery` (3) в `app/build.gradle.kts`.  
  От этого зависят рекламные блоки и ссылка на магазин в [AppConfig.kt](app/src/main/java/ru/plumsoftware/finance/AppConfig.kt).
- **Remote Config** (при наличии Firebase): `family_mortgage_rate`, `family_mortgage_limit` — условия семейной ипотеки для калькулятора.

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

- **Редизайн (ТЗ):** `TZ/ТЗ.md`; дизайн-система — `ui/ds/`, токены — `ui/theme/FinanceColors.kt`, `FinanceType.kt`
- **Навигация:** `ui/AppRoute.kt`, `navigation/AppDeepLinks.kt`, `presentation/FinanceApp.kt`
- **Реклама:** `AppConfig.kt`, `ads/`, `ui/ads/` → [YANDEX_ADS.md](docs/YANDEX_ADS.md)
- **Push:** `data/firebase/` → [FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md), [DEEPLINKS.md](docs/DEEPLINKS.md)
