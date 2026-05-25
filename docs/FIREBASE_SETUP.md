# Подключение приложения к Firebase

Пакет приложения: **`ru.plumsoftware.finance`**  
Файл конфигурации: **`app/google-services.json`**

Пока файла нет — проект **собирается без Firebase**. После добавления файла включаются FCM, In-App Messaging и инициализация в `FinanceApplication`.

---

## Шаг 1. Аккаунт и консоль

1. Откройте [Firebase Console](https://console.firebase.google.com/).
2. Войдите под Google-аккаунтом (тот же, что в Android Studio, удобно).
3. Нажмите **Создать проект** (или **Add project**).

---

## Шаг 2. Создать проект Firebase

1. **Название проекта** — например `ZenFinance` или `Контроль расходов`.
2. **Google Analytics** — по желанию (для In-App Messaging удобнее **включить**).
3. Если включили Analytics — выберите или создайте аккаунт Analytics.
4. Дождитесь создания проекта → **Продолжить**.

---

## Шаг 3. Добавить Android-приложение

1. На главной странице проекта нажмите иконку **Android**.
2. Заполните поля:

   | Поле | Значение |
   |------|----------|
   | **Android package name** | `ru.plumsoftware.finance` |
   | **App nickname** | Контроль расходов (любое) |
   | **Debug signing certificate SHA-1** | опционально сейчас; нужен для Auth/Google Sign-In |

3. **Register app** / **Зарегистрировать приложение**.

> **Важно:** package name должен **точно** совпадать с `applicationId` в `app/build.gradle.kts`.

---

## Шаг 4. Скачать `google-services.json`

1. На шаге «Download config file» нажмите **Download google-services.json**.
2. Скопируйте файл в проект:

   ```
   finance/app/google-services.json
   ```

   Не в `app/src/`, а именно в **`app/google-services.json`** (корень модуля `app`).

3. В Android Studio: **File → Sync Project with Gradle Files**.

После sync в `BuildConfig` появится `FIREBASE_ENABLED = true`, подключится плагин `google-services` и зависимости Firebase.

---

## Шаг 5. Включить Cloud Messaging (FCM)

1. Firebase Console → **Build** → **Cloud Messaging**.
2. Если раздел открывается — FCM уже доступен для проекта.
3. Для тестового push позже понадобится **Server key** / настройка в Google Cloud — для клиента Android достаточно `google-services.json`.

### Проверка на устройстве

1. Соберите и запустите debug-сборку.
2. В Logcat отфильтруйте по `FCM` или смотрите логи приложения — при старте запрашивается токен (`PushMessagingRepository.refreshFcmToken()`).
3. Тестовое сообщение: Console → **Cloud Messaging** → **Send your first message** → выберите приложение → отправьте.

---

## Шаг 6. Включить In-App Messaging

1. Console → **Engage** → **In-App Messaging**.
2. Нажмите **Get started** / создайте первую кампанию (можно черновик).
3. Для отображения в приложении уже подключена библиотека `firebase-inappmessaging-display` и `InAppMessagingHandler` в `FinanceApplication`.

Кампании настраиваются в консоли; код сохраняет показы в локальную БД через impression listener.

---

## Шаг 7. SHA-1 (опционально, но полезно)

Для debug-сборки в терминале:

```bash
cd /Users/dev02/AndroidStudioProjects/finance
./gradlew :app:signingReport
```

Скопируйте **SHA-1** варианта `debug` → Firebase Console → **Project settings** → ваше Android-приложение → **Add fingerprint**.

Нужно для Google Sign-In, Dynamic Links, части API. Для простых push не обязательно.

---

## Шаг 8. Сборка release (позже)

1. Создайте release keystore.
2. Добавьте **release SHA-1** в Firebase (тот же экран настроек приложения).
3. Скачайте обновлённый `google-services.json`, если консоль предложит (обычно тот же файл).

---

## Чеклист «всё подключено»

- [ ] Файл `app/google-services.json` на месте
- [ ] Gradle Sync без ошибки `processDebugGoogleServices`
- [ ] `BuildConfig.FIREBASE_ENABLED == true` (после sync)
- [ ] Приложение запускается
- [ ] В Logcat нет краша при старте Firebase
- [ ] (Опционально) тестовый push из консоли приходит

---

## Частые ошибки

| Ошибка | Решение |
|--------|---------|
| `google-services.json is missing` | Положить файл в `app/google-services.json`, Sync |
| `No matching client found for package name` | В Firebase указан другой package — должен быть `ru.plumsoftware.finance` |
| Push не приходит | Разрешение `POST_NOTIFICATIONS` на Android 13+; приложение в фоне/на экране; правильный `applicationId` |
| In-App не показывается | Создана и **опубликована** кампания; прошло до 24 ч; приложение открыто; Analytics включён |

---

## Безопасность

- `google-services.json` содержит публичные идентификаторы — его обычно коммитят в репозиторий.
- **Не коммитьте** service account JSON с приватным ключом (это для сервера, не для Android).

---

## Отключить Firebase снова (временно)

Удалите `app/google-services.json` и сделайте **Sync** — сборка снова пойдёт без Firebase, как сейчас.
