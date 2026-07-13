import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.RequestOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue // <--- Поменяли импорт здесь

class RickAndMortyGqlTest {

    @Test
    fun `should fetch Rick Sanchez from GraphQL API`() {
        // Запускаем Playwright
        Playwright.create().use { playwright ->
            // Создаем контекст для API-запросов
            val request = playwright.request().newContext()
            
            // Наш GraphQL запрос
            val query = """
                {
                  character(id: 1) {
                    name
                    status
                    species
                  }
                }
            """.trimIndent()
            
            // Отправляем POST-запрос на эндпоинт Rick and Morty
            val response = request.post(
                "https://rickandmortyapi.com/graphql", 
                RequestOptions.create().setData(mapOf("query" to query))
            )
            
            // Читаем ответ
            val responseText = response.text()
            println("RESPONSE FROM SERVER: $responseText")
            
            // Проверяем результат (в JUnit 5 первым аргументом идет условие)
            assertTrue(response.ok(), "HTTP статус должен быть 200 OK")
            assertTrue(responseText.contains("Rick Sanchez"), "В ответе должен быть Рик Санчез")
        }
    }

    @Test
    fun `should fetch Morty Smith using variables`() {
        // Запускаем Playwright
        Playwright.create().use { playwright ->
            // Создаем контекст для API-запросов
            val request = playwright.request().newContext()
            
            // Наш GraphQL запрос с переменной
            val query = """
                query GetCharacter(${'$'}characterId: ID!) {
                  character(id: ${'$'}characterId) {
                    name
                    status
                    species
                  }
                }
            """.trimIndent()
            
            // Отправляем POST-запрос с переменными
            val response = request.post(
                "https://rickandmortyapi.com/graphql", 
                RequestOptions.create().setData(mapOf(
                    "query" to query,
                    "variables" to mapOf("characterId" to 2)
                ))
            )
            
            // Читаем ответ
            val responseText = response.text()
            println("RESPONSE FROM SERVER (VARIABLES): $responseText")
            
            // Проверяем результат
            assertTrue(response.ok(), "HTTP статус должен быть 200 OK")
            assertTrue(responseText.contains("Morty Smith"), "В ответе должен быть Morty Smith")
        }
    }

    @Test
    fun `should return error for non-existent character`() {
        // Запускаем Playwright
        Playwright.create().use { playwright ->
            // Создаем контекст для API-запросов
            val request = playwright.request().newContext()
            
            // Наш GraphQL запрос для несуществующего персонажа
            val query = """
                {
                  character(id: "abc") {
                    name
                  }
                }
            """.trimIndent()
            
            // Отправляем POST-запрос
            val response = request.post(
                "https://rickandmortyapi.com/graphql", 
                RequestOptions.create().setData(mapOf("query" to query))
            )
            
            // Читаем ответ
            val responseText = response.text()
            println("RESPONSE FROM SERVER (NON-EXISTING): $responseText")
            
            // Проверяем результат (GraphQL возвращает 200 OK даже при ошибках запроса/данных)
            assertTrue(response.ok(), "HTTP статус должен быть 200 OK")
            assertTrue(responseText.contains("errors") || responseText.contains("Character not found"), "В ответе должны присутствовать ошибки")
        }
    }
}