package forpdateam.ru.forpda.entity.remote.checker

/**
 * Created by radiationx on 28.01.18.
 */
class UpdateData {
    var code: Int = 0
    var build: Int = 0
    var name: String? = null
    var date: String? = null
    val links = mutableListOf<UpdateLink>()

    val important = mutableListOf<String>()
    val added = mutableListOf<String>()
    val fixed = mutableListOf<String>()
    val changed = mutableListOf<String>()

    var patternsVersion = 0

    class UpdateLink(
            val name: String,
            val url: String,
            val type: String
    )
}