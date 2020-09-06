package forpdateam.ru.forpda.presentation.announce

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.ui.TemplateManager

class AnnounceTemplate(
        private val templateManager: TemplateManager
) {

    fun mapEntity(announce: Announce): Announce = announce.apply { html = mapString(announce) }

    fun mapString(announce: Announce): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_ANNOUNCE)

        template.apply {
            templateManager.fillStaticStrings(this)
            setVariableOpt("style_type", templateManager.getThemeType())
            setVariableOpt("body", announce.html)
        }

        val result = template.generateOutput()
        template.reset()
        return result
    }

}