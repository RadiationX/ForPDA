package forpdateam.ru.forpda.model.data.remote.api.regex.parser

/**
 * Created by radiationx on 13.08.17.
 */
class Document : Node(NODE_DOCUMENT) {
    @JvmField
    var docType: String = "html"

    companion object {
        const val DOCTYPE_TAG: String = "!DOCTYPE"
    }
}
