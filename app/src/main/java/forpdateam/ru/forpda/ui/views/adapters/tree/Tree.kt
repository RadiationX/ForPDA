package forpdateam.ru.forpda.ui.views.adapters.tree


sealed class Tree<K, V>(
    open val key: K,
    open val children: List<Node<K, V>>,
    private val treeMap: Map<K, Tree<K, V>>
) {

    operator fun get(key: K): Tree<K, V>? {
        return treeMap[key]
    }

    class Root<K, V>(
        override val key: K,
        override val children: List<Node<K, V>>,
        treeMap: Map<K, Tree<K, V>>
    ) : Tree<K, V>(key, children, treeMap)

    class Node<K, V>(
        override val key: K,
        override val children: List<Node<K, V>>,
        private val treeMap: Map<K, Tree<K, V>>,
        val level: Int,
        val parentKey: K,
        val value: V
    ) : Tree<K, V>(key, children, treeMap) {

        val parent: Tree<K, V>
            get() = requireNotNull(get(parentKey)) {
                "Orphan is found"
            }
    }
}


fun <K, V> Tree<K, V>.toSequence(): Sequence<Tree.Node<K, V>> {
    if (children.isEmpty()) {
        return emptySequence()
    }
    return sequence {
        children.forEach {
            yieldAll(it.toSequence())
        }
    }
}

fun <K, V> Tree<K, V>.sequenceToRoot(): Sequence<Tree<K, V>> {
    return sequence {
        var currentNode: Tree<K, V>? = this@sequenceToRoot
        while (currentNode != null) {
            yield(currentNode)
            currentNode = when (currentNode) {
                is Tree.Root -> null
                is Tree.Node -> currentNode.parent
            }
        }
    }
}