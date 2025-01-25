package forpdateam.ru.forpda.model.data.remote.api.regex.parser

/**
 * Created by radiationx on 13.08.17.
 */
open class Node {
    @JvmField
    val nodes: ArrayList<Node> = ArrayList()
    val elements: ArrayList<Node> = ArrayList()
    @JvmField
    val attributes: LinkedHashMap<String, String> = LinkedHashMap()
    @JvmField
    var name: String? = null
    @JvmField
    var text: String? = null

    constructor()

    constructor(name: String?) {
        this.name = name
    }

    fun addNode(node: Node) {
        nodes.add(node)
    }

    fun addElement(node: Node) {
        elements.add(node)
    }

    override fun toString(): String {
        return name!!
    }


    fun putAttribute(name: String, value: String) {
        attributes[name] = value
    }

    fun getAttribute(attr: String): String? {
        return attributes[attr]
    }

    companion object {
        const val NODE_DOCUMENT: String = "#document"
        const val NODE_TEXT: String = "#text"
        const val NODE_COMMENT: String = "#comment"
    }
}
