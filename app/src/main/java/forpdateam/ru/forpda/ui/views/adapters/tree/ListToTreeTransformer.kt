package forpdateam.ru.forpda.ui.views.adapters.tree


class ListToTreeTransformer<K, V>(
    private val keyGetter: (V) -> K,
    private val parentKeyGetter: (V) -> K,
) {

    private val transformer = TreeTransformer<K, V, BuilderTree<K, V>>(
        keyGetter = { it.key },
        valueGetter = { (it as BuilderTree.Node).value },
        childrenGetter = { it.childBuilders }
    )

    fun transform(list: List<V>, rootKey: K): Tree.Root<K, V> {
        val parents = mutableMapOf<K, BuilderTree<K, V>>()
        val builders = list.map { value ->
            BuilderTree.Node<K, V>(
                key = keyGetter.invoke(value),
                parentKey = parentKeyGetter.invoke(value),
                value = value
            ).also {
                parents[it.key] = it
            }
        }
        val root = BuilderTree.Root<K, V>(rootKey)
        parents[rootKey] = root

        builders.forEach {
            if (it.key == it.parentKey) {
                return@forEach
            }
            parents[it.parentKey]?.addChildren(it)
        }

        return transformer.transform(root)
    }


    private sealed class BuilderTree<K, V>(val key: K) {
        val childBuilders = mutableListOf<Node<K, V>>()

        fun addChildren(item: Node<K, V>) {
            childBuilders.add(item)
        }

        class Root<K, V>(key: K) : BuilderTree<K, V>(key)

        class Node<K, V>(
            key: K,
            val parentKey: K,
            val value: V,
        ) : BuilderTree<K, V>(key)
    }
}