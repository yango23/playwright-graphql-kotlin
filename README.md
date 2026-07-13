# Playwright + Kotlin + GraphQL API Testing

Демонстрационный проект по автоматизации тестирования GraphQL API с использованием фреймворка **Playwright** и языка **Kotlin** на базе сборщика **Gradle**.

## 🛠 Стек технологий
* **Language:** Kotlin 1.9.x
* **Test Framework:** JUnit 5
* **Automation Engine:** Playwright (Java/Kotlin Client)
* **Build System:** Gradle (Kotlin DSL)
* **Target API:** [The Rick and Morty GraphQL API](https://rickandmortyapi.com/graphql)

## 📂 Структура тестов
В проекте реализованы 3 базовых сценария для работы с GraphQL:
1. `should fetch Rick Sanchez from GraphQL API` — Базовый POST-запрос (Query) на получение персонажа по ID.
2. `should fetch Morty Smith using variables` — Запрос с использованием динамических GraphQL-переменных (`variables`).
3. `should return error for non-existent character` — Негативный тест на валидацию типов (передача некорректного ID) и проверку структуры `errors`.

## 🚀 Запуск тестов
Для запуска тестов из терминала выполните:
```bash
./gradlew test --info --rerun-tasks
```
