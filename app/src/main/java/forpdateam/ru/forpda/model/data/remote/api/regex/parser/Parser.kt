package forpdateam.ru.forpda.model.data.remote.api.regex.parser

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.08.17.
 */
object Parser {
    private const val S_TAG = 1
    private const val S_ATTRS = 2
    private const val S_TEXT = 3
    private const val CLOSING = 4
    private const val TAG = 5
    private const val ATTRS = 6
    private const val TEXT = 7

    /*
     * GROUPS
     *
     * script/style/textarea/etc:
     * 1. Tag name
     * 2. Attributes
     * 3. Inner text
     *
     * basic:
     * 4. Close tag symbol "/"
     * 5. Tag name
     * 6. Attributes
     * 7. Text
     *
     * if no groups - comment
     * */
    //private final static Pattern NON_CLOSING_TAGS = Pattern.compile("!DOCTYPE|colgroup|command|keygen|source|embed|input|param|track|area|link|meta|col|img|wbr|br|hr", Pattern.CASE_INSENSITIVE);
    private val mainPattern by lazy {
        Pattern.compile(
            "\\<(?:(?:(script|style|textarea)(?:([^\\>]+))?\\>)([\\s\\S]*?)(?:\\<\\/\\1)|([\\/])?(!?[\\w]*)(?:([^\\>]+))?\\/?)\\>(?:([^<]+))?",
            Pattern.CASE_INSENSITIVE
        )
    }

    private val attributePattern by lazy {
        Pattern.compile(
            "([^ \"']*?)\\s*?=\\s*?([\"'])([\\s\\S]*?)\\2",
            Pattern.CASE_INSENSITIVE
        )
    }

    private val uTags: Array<String> = arrayOf(
        "!doctype",
        "area",
        "br",
        "col",
        "colgroup",
        "command",
        "embed",
        "hr",
        "img",
        "input",
        "keygen",
        "link",
        "meta",
        "param",
        "source",
        "track",
        "wbr"
    )

    private fun containsInUTag(tag: String?): Boolean {
        for (uTag in uTags) if (uTag.equals(tag, ignoreCase = true)) return true
        return false
    }

    private fun getMatcher(m: Matcher?, p: Pattern, s: String): Matcher {
        return if (m == null) p.matcher(s) else m.reset(s)
    }

    fun parse(html: String): Document {
        val openedNodes = ArrayList<Node?>()
        val root = Document()

        openedNodes.add(root)
        var lastOpened: Node?

        val matcher = mainPattern!!.matcher(html)
        var attrMatcher: Matcher? = null
        var nodesAdd = 0
        var nodesClose = 0
        while (matcher.find()) {
            lastOpened = openedNodes[openedNodes.size - 1]
            val node = Node()


            var special = false
            var tagName = matcher.group(TAG)
            if (tagName == null) {
                special = true
            }


            val openAction = matcher.group(CLOSING) == null


            if (openAction) {
                if (special) {
                    tagName = matcher.group(S_TAG)
                    special = tagName != null
                }
                val attrs = matcher.group(if (special) S_ATTRS else ATTRS)
                val text = matcher.group(if (special) S_TEXT else TEXT)
                // Log.d("PARSER", "Open last= " + lastOpened + "; new= " + tagName + "; text= '" + text + "'");
                var addToOpened = true
                if (tagName == null) {
                    if (text == null) {
                        node.name = Node.NODE_COMMENT
                        node.text = matcher.group()
                    }
                    addToOpened = false
                } else {
                    node.name = tagName

                    if (attrs != null) {
                        attrMatcher = getMatcher(
                            attrMatcher,
                            attributePattern!!, attrs
                        )
                        while (attrMatcher.find()) {
                            node.putAttribute(attrMatcher.group(1), attrMatcher.group(3))
                        }
                    }

                    //ncMatcher = getMatcher(ncMatcher, NON_CLOSING_TAGS, tagName);
                    if (containsInUTag(tagName)) {
                        if (tagName.equals(Document.DOCTYPE_TAG, ignoreCase = true)) {
                            root.docType = attrs!!
                        }
                        addToOpened = false
                    }
                    if (text != null) {
                        if (special) {
                            addToOpened = false
                        }
                        val textNode = Node(Node.NODE_TEXT)
                        textNode.text = text
                        node.addNode(textNode)
                        nodesAdd++
                    }
                }

                lastOpened!!.addNode(node)
                //Log.d("PARSER", "ADD? = " + addToOpened);
                nodesAdd++
                if (addToOpened) {
                    openedNodes.add(node)
                }
            } else {
                //Log.e("PARSER", "Close last = " + lastOpened);
                openedNodes.remove(lastOpened)
                nodesClose++
            }
        }
        openedNodes.remove(root)

        Log.d("SUKA", "FINAL OPENED " + openedNodes.size + " : " + nodesAdd + " : " + nodesClose)

        return root
    }

    private fun isNotElement(node: Node): Boolean {
        return node.name == null || node.name == Node.NODE_TEXT || node.name == Node.NODE_COMMENT
    }

    private fun isTextNode(node: Node): Boolean {
        return node.name == Node.NODE_TEXT
    }

    private fun getHtml(document: Document, node: Node, matcher: Matcher?): String {
        val resultHtml = StringBuilder()
        val onlyText = isNotElement(node)

        if (onlyText) {
            resultHtml.append(node.text)
        } else {
            resultHtml.append("<").append(node.name)
            for ((key, value) in node.attributes) {
                resultHtml.append(" ").append(key).append("=\"").append(value).append("\"")
            }
            resultHtml.append(">")
        }

        if (!onlyText) {
            for (child in node.nodes) {
                val s = getHtml(document, child, matcher)
                resultHtml.append(s)
            }
        }


        if (!onlyText) {
            if (!containsInUTag(node.name)) {
                resultHtml.append("</").append(node.name).append(">")
            }
        }


        return resultHtml.toString()
    }

    fun getHtml(node: Node, onlyInner: Boolean): String? {
        if (isNotElement(node)) {
            return node.text
        }
        val resultHtml = StringBuilder()

        if (!onlyInner) {
            resultHtml.append("<").append(node.name)
            for ((key, value) in node.attributes) {
                resultHtml.append(" ").append(key).append("=\"").append(value).append("\"")
            }
            resultHtml.append(">")
        }


        for (child in node.nodes) {
            val s = getHtml(child, false)
            resultHtml.append(s)
        }


        if (!onlyInner) {
            if (!containsInUTag(node.name)) {
                resultHtml.append("</").append(node.name).append(">")
            }
        }


        return resultHtml.toString()
    }


    fun findNode(node: Node, tag: String?, attr: String?, value: String): Node? {
        if (isNotElement(node)) {
            return null
        }
        if (node.name.equals(tag, ignoreCase = true)) {
            if (attr == null) {
                return node
            }
            val attrValue = node.attributes[attr]
            if (attrValue != null && attrValue.contains(value)) {
                return node
            }
        }
        var result: Node? = null
        for (child in node.nodes) {
            result = findNode(child, tag, attr, value)
            if (result != null) break
        }
        return result
    }

    fun findChildNodes(node: Node, tag: String?, attr: String?, value: String): ArrayList<Node> {
        val result = ArrayList<Node>()
        if (isNotElement(node)) {
            return result
        }

        for (child in node.nodes) {
            if (isNotElement(child)) continue
            if (child.name.equals(tag, ignoreCase = true)) {
                if (attr == null) {
                    result.add(child)
                    continue
                }
                val attrValue = child.attributes[attr]
                if (attrValue != null && attrValue.contains(value)) {
                    result.add(child)
                }
            }
        }
        return result
    }

    fun ownText(node: Node): String {
        val stringBuilder = StringBuilder()

        for (child in node.nodes) {
            if (isTextNode(child)) {
                stringBuilder.append(child.text)
            }
        }
        return stringBuilder.toString()
    }
}

