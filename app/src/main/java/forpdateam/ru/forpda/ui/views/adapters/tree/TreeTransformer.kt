package forpdateam.ru.forpda.ui.views.adapters.tree

class TreeTransformer<K, V, T>(
    private val keyGetter: (T) -> K,
    private val valueGetter: (T) -> V,
    private val childrenGetter: (T) -> List<T>
) {

    fun transform(rootInput: T): Tree.Root<K, V> {
        val key = keyGetter.invoke(rootInput)
        val treeMap = mutableMapOf<K, Tree<K, V>>()
        val root = Tree.Root(
            key = key,
            children = traverse(treeMap, rootInput, key, 0),
            treeMap = treeMap
        )
        treeMap[key] = root
        return root
    }

    private fun traverse(treeMap: MutableMap<K, Tree<K, V>>, parentInput: T, parentKey: K, level: Int): List<Tree.Node<K, V>> {
        return childrenGetter.invoke(parentInput).map { item ->
            val key = keyGetter.invoke(item)
            Tree.Node(
                key = key,
                children = traverse(treeMap, item, key, level + 1),
                treeMap = treeMap,
                value = valueGetter.invoke(item),
                parentKey = parentKey,
                level = level
            ).also {
                treeMap[key] = it
            }
        }
    }
}