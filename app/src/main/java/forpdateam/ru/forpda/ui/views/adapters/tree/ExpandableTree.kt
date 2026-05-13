package forpdateam.ru.forpda.ui.views.adapters.tree

class ExpandableTree<K, V>(
) {

    private var root: Tree.Root<K, V>? = null
    private var expandedKeys = emptySet<K>()

    fun setRoot(root: Tree.Root<K,V>){
        this.root = root
    }

    fun setExpanded(key: K, state: Boolean) {
        if (state) {
            expandedKeys += key
        } else {
            expandedKeys -= key
        }
    }

    fun <R> transformSequence(transform: (Tree.Node<K, V>, Boolean) -> R): Sequence<R> {
        val root = this.root ?: return emptySequence()
        val keysToExpand = mutableSetOf<K>()
        expandedKeys.toSet().forEach { expandedKey ->
            root[expandedKey]?.sequenceToRoot()?.forEach {
                keysToExpand.add(it.key)
            }
        }
        return root.toSequence()
            .filter { it.isVisible(keysToExpand) }
            .map { node ->
                val expanded = node.key in keysToExpand
                transform.invoke(node, expanded)
            }
    }

    private fun Tree.Node<K, V>.isVisible(nodesIdsToExpand: Set<K>): Boolean {
        return when (parent) {
            is Tree.Node -> key in nodesIdsToExpand || parentKey in nodesIdsToExpand
            is Tree.Root -> true
        }
    }
}