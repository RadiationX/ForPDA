package forpdateam.ru.forpda.presentation.articles.detail

import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.TemplateManager

class ArticleTemplate(
        private val templateManager: TemplateManager
) {

    fun mapEntity(page: DetailsPage): DetailsPage = page.apply { html = mapString(page) }

    fun mapString(page: DetailsPage): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_NEWS)

        template.apply {
            templateManager.fillStaticStrings(this)
            setVariableOpt("style_type", templateManager.getThemeType())

            setVariableOpt("details_title", ApiUtils.htmlEncode(page.title))
            setVariableOpt("details_content", page.html)
            for (material in page.materials) {
                setVariableOpt("material_id", material.id)
                setVariableOpt("material_image", material.imageUrl)
                setVariableOpt("material_title", material.title)
                addBlockOpt("material")
            }
        }

        val result = template.generateOutput()
        template.reset()
        return result
    }

}