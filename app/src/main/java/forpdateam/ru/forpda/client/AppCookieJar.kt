package forpdateam.ru.forpda.client

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject

class AppCookieJar @Inject constructor(
    private val cookieStorage: CookieStorage
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStorage.save(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val external = !url.host.endsWith("4pda", ignoreCase = true)
        val allCookies = cookieStorage.getAll().map { it.cookie }
        return if (external) {
            allCookies.filterNot { it.name !in CookieStorage.KNOWN_COOKIES }
        } else {
            allCookies
        }
    }
}