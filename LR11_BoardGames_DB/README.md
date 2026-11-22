# ЛР11 — Java-клієнт БД настільних ігор

## Функції
- перегляд таблиці `board_games`;
- додавання запису;
- редагування вибраного запису;
- видалення запису з підтвердженням;
- фільтрація ігор, ціна яких менша за задану;
- автоматичне створення SQLite БД і таблиці з первинним ключем та обмеженнями.

## Схема таблиці
`board_games(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, game_count INTEGER NOT NULL CHECK(game_count > 0), price REAL NOT NULL CHECK(price >= 0), game_type TEXT NOT NULL CHECK(game_type IN ('Шахи','Монополія')), extra TEXT NOT NULL)`

## Запуск у VS Code
Потрібні JDK 17+ та Maven.
1. Відкрийте папку проєкту.
2. У терміналі виконайте `mvn clean package`.
3. Запустіть `ua.lr11.Main` з IDE, або виконайте:
   `mvn exec:java -Dexec.mainClass=ua.lr11.Main`

SQLite-файл `board_games.db` буде створено в поточній робочій папці під час першого запуску. Якщо таблиця порожня, програма додає кілька демонстраційних записів.
