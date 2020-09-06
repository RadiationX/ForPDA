package forpdateam.ru.forpda.presentation.forumrules

import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.ui.TemplateManager

class ForumRulesTemplate(
        private val templateManager: TemplateManager
) {

    fun mapEntity(rules: ForumRules): ForumRules = rules.apply { html = mapString(rules) }

    fun mapString(rules: ForumRules): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_FORUM_RULES)

        template.apply {
            templateManager.fillStaticStrings(this)
            setVariableOpt("style_type", templateManager.getThemeType())
            for (item in rules.items) {
                setVariableOpt("type", if (item.isHeader) "header" else "")
                setVariableOpt("number", item.number)
                setVariableOpt("text", item.text)
                addBlockOpt("rules_item")
            }
        }

        val result = template.generateOutput()
        template.reset()
        return result
    }

}