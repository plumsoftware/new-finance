# Маскоты для пустых состояний (не онбординг, не иконка)

Используются **редко**: максимум **один** маскот на экран, только когда список/данные пустые.

| Файл в `res/drawable/` | Где показывается |
|------------------------|------------------|
| `mascot_empty_transactions` | Главная — нет операций |
| `mascot_empty_smart_savings` | Экономия — нет умных покупок |
| `mascot_empty_analytics` | Аналитика — нет данных за период |

Размер в UI: **120 dp** (`Dimens.mascotEmptyState`).  
Формат: **WebP/PNG 512×512**, **прозрачный фон**.

---

## Общий стиль (как онбординг, но другая поза)

- Та же сова-коуч: flat, iOS minimal, `#E5E5EA` / `#007AFF` акценты
- **Фронтально или ¾**, эмоция под контекст (не дублировать кадры онбординга)
- Без текста на картинке
- Safe zone 10% от краёв

---

## 1. `mascot_empty_transactions`

**Контекст:** пользователь ещё не добавил расходы/доходы.

**Сцена:** сова смотрит на пустой список / листочек с «+» (не калькулятор с деньгами).

**Эмоция:** приглашающая, спокойная — «давай начнём».

**Промпт (EN):**
```
Flat 2D owl mascot, friendly coach, transparent background, 512x512.
Owl holding or pointing at empty checklist with small plus icon, inviting calm expression, light gray body #E5E5EA, iOS minimal fintech style, no money coins, no text.
```

---

## 2. `mascot_empty_smart_savings`

**Контекст:** нет умных покупок.

**Сцена:** сова рядом с **пунктирным** силуэтом термокружки или «?» — «ещё не добавили».

**Эмоция:** любопытная, лёгкая улыбка (не грустная).

**Промпт (EN):**
```
Flat 2D owl mascot, transparent background, 512x512.
Owl beside dashed-outline thermos mug #007AFF, curious friendly smile, smart purchase empty state, minimal, no text, no coins.
```

---

## 3. `mascot_empty_analytics`

**Контекст:** нет данных за выбранный период.

**Сцена:** сова с **пустым** мини-графиком (пунктир) или лупой — «пока нечего анализировать».

**Эмоция:** задумчивая, нейтральная (не паника).

**Промпт (EN):**
```
Flat 2D owl mascot, transparent background, 512x512.
Owl looking at empty dashed bar chart, thoughtful calm expression, light gray body, blue accent #007AFF on chart, iOS minimal, no text.
```

---

## Где маскот **не** показывается

- Онбординг (свои 4 файла `onboarding_mascot_*`)
- Настройки / профиль (есть данные — счета, категории)
- Заполненные списки
- Иконка приложения

## Где только **текст** без маскота

- Главная: блок «Умная экономия» пустой, но операции уже есть — одна строка подсказки
- Экономия: одна секция пустая, вторая с данными — серый текст без второго маскота
