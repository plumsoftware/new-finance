# Архитектура по слоям

Документ описывает структуру проекта `finance` по слоям, их ответственность и правила взаимодействия.

## 1) Presentation Layer (`app/src/main/java/.../presentation`, `ui`)

**Что входит:**
- экраны Compose (`.../presentation/*Screen.kt`)
- `ViewModel` для экранов (`.../presentation/*ViewModel.kt`)
- UI-компоненты (`.../ui/components`, `.../ui/theme`)

**Ответственность:**
- отрисовка интерфейса
- обработка пользовательских действий
- хранение экранного состояния
- преобразование данных в UI-модель

**Правила:**
- пользовательские строки только из `strings.xml`
- экран не ходит напрямую в БД
- доступ к данным только через репозитории из `domain`

## 2) Domain Layer (`app/src/main/java/.../domain`)

**Что входит:**
- бизнес-модели (`.../domain/model`)
- интерфейсы репозиториев (`.../domain/repository`)
- бизнес-логика/движки (`.../domain/insights`, `.../domain/notifications`)

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

**Ответственность:**
- регистрация зависимостей (Koin)
- создание `ViewModel`, репозиториев, сервисов

**Правила:**
- зависимости задаются через конструкторы
- `ViewModel` не создаются вручную в UI

## 5) Navigation Layer (`app/src/main/java/.../navigation`, `ui/AppRoute.kt`, `presentation/FinanceApp.kt`)

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

## 6) Widget Layer (`app/src/main/java/.../widget`, `app/src/main/res/layout|xml|drawable`)

**Что входит:**
- провайдер виджета (`QuickAddWidgetProvider`)
- layout и metadata виджета
- отдельные стили/цвета виджета

**Ответственность:**
- запуск быстрых сценариев из launcher
- deeplink-переходы в экраны приложения

## 7) Resource Layer (`app/src/main/res`)

**Что входит:**
- `values/strings.xml`, `colors.xml`, темы, drawable, layout

**Ответственность:**
- все пользовательские тексты
- централизация цветов/размеров/тем

**Правила:**
- пользовательский текст хранится в `strings.xml`
- в коде используются `stringResource(...)` или `context.getString(...)`

## 8) Текущий поток данных

1. UI вызывает действие во `ViewModel`.
2. `ViewModel` обращается к интерфейсу репозитория (`domain/repository`).
3. Реализация репозитория (`data/repository`) работает с DAO/DataStore/Firebase.
4. Результат возвращается в `ViewModel`.
5. `ViewModel` обновляет `StateFlow`, UI перерисовывается.

## 9) Чек-лист при добавлении новой фичи

- добавить модели и контракты в `domain`
- реализовать доступ к данным в `data`
- зарегистрировать зависимости в `di`
- сделать экран + `ViewModel` в `presentation`
- добавить route/deeplink в `navigation`
- добавить все пользовательские строки в `strings.xml`
- обновить документацию в `docs/`
