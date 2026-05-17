package forpdateam.ru.forpda.client

import android.util.Log
import ru.radiationx.flowpreferences.FlowPreferences
import ru.radiationx.flowpreferences.core.PreferenceValue
import ru.radiationx.flowpreferences.ext.mapping
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

class CookieStorage @Inject constructor(
    private val preferences: FlowPreferences
) {

    companion object {
        private const val TAG = "CookieStorage"
        private const val DELIMITER = "|:|"
        private const val PREF_KEY_PREFIX = "cookie_"

        const val MEMBER_ID = "member_id"
        const val PASS_HASH = "pass_hash"
        const val SESSION_ID = "session_id"
        const val ANONYMOUS = "anonymous"
        const val CF_CLEARANCE = "cf_clearance"
        const val NGX_MB = "ngx_mb"
        const val DESKVER = "deskver"

        val AUTH_COOKIES = setOf(
            MEMBER_ID,
            PASS_HASH,
            SESSION_ID,
            ANONYMOUS,
        )

        val KNOWN_COOKIES = AUTH_COOKIES + setOf(
            CF_CLEARANCE,
            NGX_MB,
            DESKVER
        )
    }

    private val staticCookies by lazy {
        val url = "https://4pda.to/".toHttpUrl()
        listOfNotNull(
            AppCookie(url, Cookie.parse(url, "$NGX_MB=1;")!!),
            AppCookie(url, Cookie.parse(url, "$DESKVER=0;")!!)
        ).associateBy { it.cookie.name }
    }

    private val cookiesPreferences by lazy {
        preferences.getAll({ it.startsWith(PREF_KEY_PREFIX) }).mapping(
            transformGet = { preferencesMap ->
                preferencesMap.mapNotNull { (key, value) ->
                    parseCookieFromEntry(key, value)?.let {
                        staticCookies[it.cookie.name] ?: it
                    }
                }
            },
            transformSet = { appCookies ->
                val result = mutableMapOf<String, PreferenceValue>()
                appCookies.forEach { cookie ->
                    if (cookie.cookie.value == "deleted") {
                        result[cookie.preferenceKey()] = PreferenceValue.Null
                    } else {
                        result[cookie.preferenceKey()] = cookie.preferenceValue()
                    }
                }
                result
            }
        )
    }

    fun observe(): StateFlow<List<AppCookie>> = cookiesPreferences

    fun getAll(): List<AppCookie> {
        return cookiesPreferences.get()
    }

    fun save(url: HttpUrl, cookies: List<Cookie>) {
        cookiesPreferences.set(cookies.map { AppCookie(url, it) })
    }

    fun removeByCookieName(cookieNames: Iterable<String>) {
        cookiesPreferences.remove(cookieNames.map { it.preferenceKey() })
    }

    private fun AppCookie.preferenceKey(): String {
        return cookie.name.preferenceKey()
    }

    private fun String.preferenceKey(): String {
        return "$PREF_KEY_PREFIX$this"
    }

    private fun AppCookie.preferenceValue(): PreferenceValue.String {
        return PreferenceValue.String("$url$DELIMITER$cookie")
    }

    private fun parseCookieFromEntry(key: String?, value: Any?): AppCookie? {
        if (key == null || value == null) {
            return null
        }
        if (!key.startsWith(PREF_KEY_PREFIX)) {
            return null
        }
        if (value !is String) {
            return null
        }
        return parseCookie(value)
    }

    private fun parseCookie(prefCookie: String): AppCookie? {
        return try {
            val fields = prefCookie.split(DELIMITER)
            val rawUrl = fields.getOrNull(0)
            val rawCookie = fields.getOrNull(1)
            requireNotNull(rawUrl) { "raw url field is null" }
            requireNotNull(rawCookie) { "raw cookie field is null" }
            require(rawUrl.isNotBlank()) { "raw url field is empty" }
            require(rawCookie.isNotBlank()) { "raw cookie field is empty" }
            val url = rawCookie.toHttpUrl()
            val cookie = Cookie.parse(url, rawCookie)
            requireNotNull(cookie) { "parsed cookie is null" }
            require(cookie.value != "deleted") { "parsed cookie is deleted" }
            AppCookie(url, cookie)
        } catch (ex: Exception) {
            Log.e(TAG, "parseCookie src='${prefCookie}'", ex)
            null
        }
    }

    data class AppCookie(
        val url: HttpUrl,
        val cookie: Cookie
    )
}