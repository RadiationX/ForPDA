package forpdateam.ru.forpda.presentation

import ru.radiationx.links.Link

/**
 * Created by radiationx on 03.02.18.
 */
interface LinkHandler {
    fun handle(inputUrl: String, args: Map<String, String?>): Boolean
    fun handle(inputUrl: String): Boolean
    fun handle(link: Link): Boolean
}