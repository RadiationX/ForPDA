package forpdateam.ru.forpda.presentation

/**
 * Created by radiationx on 03.02.18.
 */
interface ILinkHandler {
    fun handle(inputUrl: String?, router: TabRouter?, args: Map<String, String>): Boolean
    fun handle(inputUrl: String?, router: TabRouter?): Boolean
    fun findScreen(url: String): String?
}