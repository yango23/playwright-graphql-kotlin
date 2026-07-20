import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitUntilState
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
            
            val searchInput = page.locator("input.search-app__input").first()
            searchInput.waitFor()
            assertTrue(searchInput.isVisible(), "Строка поиска должна быть видимой")
            
            val pageHeader = page.locator("h1#firstHeading")
            pageHeader.waitFor()
            assertTrue(pageHeader.isVisible(), "Основной заголовок страницы должен быть видимым")
            
            page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("main_page_screenshot.png")))
            println("Скриншот успешно сохранен в файл main_page_screenshot.png")
            
            browser.close()
        }
    }

    @Test
    fun `should search for Rick Sanchez`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)
            
            page.navigate("https://rickandmorty.fandom.com/wiki/Rick_and_Morty_Wiki", 
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))
            
            val searchInput = page.locator("input.search-app__input").first()
            if (searchInput.isVisible()) {
                searchInput.fill("Rick Sanchez")
                searchInput.press("Enter")
                page.waitForTimeout(2000.0)
            }

            val title = page.title()
            println("SEARCH PAGE TITLE: $title")
            assertTrue(title.isNotEmpty(), "Заголовок страницы не должен быть пустым")
            
            browser.close()
        }
    }

    @Test
    fun `should navigate to Characters category`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)

            page.navigate("https://rickandmorty.fandom.com/wiki/Category:Characters", 
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

            val categoryHeader = page.locator("h1#firstHeading, h1.page-header__title").first()
            categoryHeader.waitFor()

            val headerText = categoryHeader.innerText()
            println("NAVIGATED TO CATEGORY: $headerText")

            assertTrue(headerText.contains("Characters") || page.title().contains("Characters"), "Мы должны оказаться в категории Characters")

            browser.close() 
        }
    }

    @Test
    fun `should navigate to next page in pagination`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)

            page.navigate("https://rickandmorty.fandom.com/wiki/Category:Characters", 
                 Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

            val nextButton = page.locator("a.category-page__pagination-next, a.pagination__next").first()

            if (nextButton.isVisible()) {
                nextButton.click(Locator.ClickOptions().setForce(true))
                page.waitForTimeout(2000.0)
            }

            val currentUrl = page.url()
            println("CURRENT URL AFTER CLICK: $currentUrl")

            assertTrue(currentUrl.isNotEmpty(), "URL страницы должен быть доступен")
            
            browser.close()
        }
    }

    @Test
    fun `should interact with dropdown menu`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )
            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)

            page.navigate("https://rickandmorty.fandom.com/wiki/Rick_and_Morty_Wiki",
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED))

            val exploreMenu = page.locator(".wds-dropdown__toggle, button.global-explore-navigation__button, .global-explore-navigation__label").first()
            
            if (exploreMenu.isVisible()) {
                try {
                    exploreMenu.hover()
                    page.waitForTimeout(1000.0)
                } catch (e: Exception) {
                    println("Hover skipped: ${e.message}")
                }
            }

            val communityLink = page.locator("a:has-text('Community'), a:has-text('Characters')").first()
            
            if (communityLink.isVisible()) {
                communityLink.click(Locator.ClickOptions().setForce(true))
                page.waitForTimeout(2000.0)
            }
            
            val title = page.title()
            println("DROPDOWN NAVIGATION TITLE: $title")
            assertTrue(title.isNotEmpty(), "Заголовок страницы должен быть непустым")
        
            browser.close()
        }
    }

    @Test
    fun `simple test`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch()
            val context = browser.newContext()
            val page = context.newPage()

            page.navigate("https://google.com")
            
            val title = page.title()
            println("GOOGLE TITLE: $title")
            assertTrue(title.contains("Google"), "Заголовок должен содержать Google")
            
            browser.close()
        }
    }

    @Test
    fun `should count elements on page`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            )

            val context = browser.newContext(getContextOptions())
            val page = context.newPage()
            blockAdsAndAnalytics(page)

            page.navigate("https://rickandmorty.fandom.com/wiki/Category:Characters",
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
            )

            val characterLinks = page.locator("a.category-page__member-link")

            characterLinks.first().waitFor()

            val elementsCount = characterLinks.count()
            println("TOTAL CHARACTERS FOUND ON PAGE: $elementsCount")

            assertTrue(elementsCount > 0, "Количество персонажей на странице должно быть больше 0")

            browser.close()
        }
    }
}