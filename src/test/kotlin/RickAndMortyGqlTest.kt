import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.RequestOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

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

    @Test
    fun `should fetch character status via GraphQL`() {
        Playwright.create().use { playwright ->

            val apiRequest = playwright.request().newContext()

            val graphQLQuery = """
                query {
                  character (id: 1) {
                    name
                    status
                  }
                }
            """.trimIndent()

            val response = apiRequest.post(
                "https://rickandmortyapi.com/graphql",
                RequestOptions.create().setData(mapOf("query" to graphQLQuery))
            )

            println("API STATUS CODE: ${response.status()}")
            assertTrue(response.status() == 200, "Сервер должен вернуть статус 200")

            val responseText = response.text()
            println("API RESPONSE TEXT: $responseText")

            assertTrue(responseText.contains("Rick Sanchez"), "В ответе должно быть имя персонажа")
            assertTrue(responseText.contains("Alive"), "Рик должен быть жив в ответе API")

            apiRequest.dispose()
        }
    }
    
    @Test
    fun `should return error for non-existing character via GraphQL`() {
        Playwright.create().use { playwright ->
            val apiRequest = playwright.request().newContext()

            val invalidQuery = """
                query {
                  charackter(id: 9999) {
                     name
                  }
                }
            """.trimIndent()

            val response = apiRequest.post(
                "https://rickandmortyapi.com/graphql",
                RequestOptions.create().setData(mapOf("query" to invalidQuery))
            )

            assertTrue(response.status() == 200 || response.status() == 400, "HTTP статус должен быть 200 или 400")
            
            val responseText = response.text()
            println("API RESPONSE: $responseText")

            assertTrue(responseText.contains("errors"), "В ответе должны быть ошибки")

            apiRequest.dispose()
        }
    }

    @Test
    fun `should return valid character info via GraphQL`() {
        Playwright.create().use { playwright -> 
        
            val apiRequest = playwright.request().newContext()

            val graphQLQuery = """
                query {
                  character(id:1) {
                     name
                     status
                  }
                }
            """.trimIndent()

            val response = apiRequest.post(
                "https://rickandmortyapi.com/graphql",
                RequestOptions.create().setData(mapOf("query" to graphQLQuery))
            )

            assertTrue(response.ok(), "HTTP статус должен быть успешным (200-299)")

            val responseText = response.text()
            println("GRAPHQL SUCCESS RESPONSE: $responseText")

            assertFalse(responseText.contains("errors"), "Ответ не должен содержать ошибок")

            assertTrue(responseText.contains("Rick Sanchez"), "В ответе должно быть имя Рика")
            assertTrue(responseText.contains("Alive"), "Статус персонажа должен быть Alive")

            apiRequest.dispose()
        }
    }

    @Test
    fun `should fetch character with graphql variables`() {
        Playwright.create().use { playwright ->
        
            val apiRequest = playwright.request().newContext()

            val graphQLQuery = """
                query GetCharacter(${'$'}id: ID!) {
                  character (id: ${'$'}id){
                    name
                    species
                  }
                }
            """.trimIndent()

            val variables = mapOf("id" to 2)

            val requestBody = mapOf(
                "query" to graphQLQuery,
                "variables" to variables
            )

            val response = apiRequest.post(
                "https://rickandmortyapi.com/graphql",
                RequestOptions.create().setData(requestBody)
            )

            assertTrue(response.status() == 200, "HTTP-статус должен быть 200")

            val responseText = response.text()
            println("GRAPHQL VARIABLES RESPONSE: $responseText")

            assertTrue(responseText.contains("Morty Smith"), "В ответе должно быть имя Morty Smith")
            assertTrue(responseText.contains("Human"), "Раса персонажа должна быть Human")

            apiRequest.dispose()
        }
    }
}