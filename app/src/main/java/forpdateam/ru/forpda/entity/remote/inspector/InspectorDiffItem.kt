package forpdateam.ru.forpda.entity.remote.inspector


data class InspectorDiff<T : InspectorItem>(
    val loadedItems: List<T>,
    val savedItems: List<T>
) {

    val diff by lazy<List<Item<T>>> {
        val result = mutableListOf<Item<T>>()

        val loadedMap = loadedItems.associateBy { it.baseId }
        val savedMap = savedItems.associateBy { it.baseId }

        val loadedIds = loadedMap.keys
        val savedIds = savedMap.keys

        val addedIds = loadedIds - savedIds
        val removedIds = savedIds - loadedIds
        val sameIds = savedIds.intersect(loadedIds)

        addedIds.mapNotNull { loadedMap[it] }.forEach {
            result.add(Item.New(it))
        }
        removedIds.mapNotNull { savedMap[it] }.forEach {
            result.add(Item.Read(it))
        }
        sameIds.forEach {
            val new = loadedMap[it] ?: return@forEach
            val old = savedMap[it] ?: return@forEach
            val event = if (new.baseTimeStamp > old.baseTimeStamp) {
                Item.New(new)
            } else {
                Item.Same(new)
            }
            result.add(event)
        }

        result
    }

    sealed interface Item<T : InspectorItem> {

        val item: T

        class New<T : InspectorItem>(override val item: T) : Item<T>

        class Same<T : InspectorItem>(override val item: T) : Item<T>

        class Read<T : InspectorItem>(override val item: T) : Item<T>
    }
}

