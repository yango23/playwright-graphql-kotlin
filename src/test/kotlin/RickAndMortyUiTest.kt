import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState // Добавили импорт
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Paths

class RickAndMortyUiTest {

    private fun getContextOptions() = Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")

    // Вынесем общую логику блокировки мусора на странице, чтобы тесты не висли
    private fun blockAdsAndAnalytics(page: Page) {
        page.route("**/*") { route ->
            val url = route.request().url()
            if (url.contains("google-analytics") || url.contains("adengine") || url.contains("ads") || url.contains("beacon")) {
                route.abort()
            } else {
                route.resume()
            }
        }
    }

    @Test
    fun `should open wiki and check title`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)
            
            // Ждем только базовый DOM, не дожидаясь картинок и рекламы
            page.navigate("https://rickandmorty.fandom.com/wiki/Rick_and_Morty_Wiki", 
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))
            
            val title = page.title()
            println("PAGE TITLE: $title")
            
            assertTrue(title.contains("Rick and Morty"), "Заголовок вкладки должен быть корректным")
            
            browser.close()
        }
    }

    @Test
    fun `should verify main page elements`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)
            
            page.navigate("https://rickandmorty.fandom.com/wiki/Rick_and_Morty_Wiki", 
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))
            
            // Используем селекторы, которые дождутся появления элементов
            val searchInput = page.locator("input.search-app__input").first()
            searchInput.waitFor() // Явно подождем появления элемента на экране
            assertTrue(searchInput.isVisible(), "Строка поиска должна быть видимой")
            
            val pageHeader = page.locator("h1#firstHeading")
            pageHeader.waitFor()
            assertTrue(pageHeader.isVisible(), "Основной заголовок страницы должен быть видимым")
            
            page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("main_page_screenshot.png")))
            println("Скриншот успешно сохранен в файл main_page_screenshot.png")
            
            browser.close()
        }
    }
}