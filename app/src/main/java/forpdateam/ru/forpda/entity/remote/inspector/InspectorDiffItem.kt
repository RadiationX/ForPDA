package forpdateam.ru.forpda.entity.remote.inspector

import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId


sealed class InspectorDiff<T : InspectorItem<ID>, ID>(
    open val loadedItems: List<T>,
    open val savedItems: List<T>
) {

    val diff by lazy<List<Item<T, ID>>> {
        val result = mutableListOf<Item<T, ID>>()

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

    data class Favorites(
        override val loadedItems: List<InspectorItem.Favorite>,
        override val savedItems: List<InspectorItem.Favorite>
    ) : InspectorDiff<InspectorItem.Favorite, TopicId>(loadedItems, savedItems)

    data class Qms(
        override val loadedItems: List<InspectorItem.Qms>,
        override val savedItems: List<InspectorItem.Qms>
    ) : InspectorDiff<InspectorItem.Qms, QmsThreadId>(loadedItems, savedItems)

    sealed interface Item<T : InspectorItem<ID>, ID> {

        val item: T

        class New<T : InspectorItem<ID>, ID>(override val item: T) : Item<T, ID>

        class Same<T : InspectorItem<ID>, ID>(override val item: T) : Item<T, ID>

        class Read<T : InspectorItem<ID>, ID>(override val item: T) : Item<T, ID>
    }
}

