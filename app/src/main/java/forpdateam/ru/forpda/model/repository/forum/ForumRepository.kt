package forpdateam.ru.forpda.model.repository.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by radiationx on 03.01.18.
 */

class ForumRepository(
    private val schedulers: SchedulersProvider,
    private val forumApi: ForumApi,
    private val forumCache: ForumCache
) : BaseRepository(schedulers) {

    fun getForums(): Single<ForumItemTree> = Single
        .fromCallable { transformToTree(forumApi.getForums()) }
        .runInIoToUi()

    fun getCache(): Single<ForumItemTree> = Single
        .fromCallable {
            transformToTree(forumCache.getItems())
        }
        .runInIoToUi()

    fun markAllRead(): Single<Any> = Single
        .fromCallable { forumApi.markAllRead() }
        .runInIoToUi()

    fun markRead(id: Int): Single<Any> = Single
        .fromCallable { forumApi.markRead(id) }
        .runInIoToUi()

    fun getRules(): Single<ForumRules> = Single
        .fromCallable { forumApi.getRules() }
        .runInIoToUi()

    fun getAnnounce(id: Int, forumId: Int): Single<Announce> = Single
        .fromCallable { forumApi.getAnnounce(id, forumId) }
        .runInIoToUi()

    fun saveCache(rootForum: ForumItemTree): Completable = Completable
        .fromRunnable {
            val items = mutableListOf<ForumItemFlat>().apply {
                transformToList(this, rootForum)
            }
            forumCache.saveItems(items)
        }
        .runInIoToUi()


    private fun transformToList(
        list: MutableList<ForumItemFlat>,
        rootForum: ForumItemTree
    ) {
        rootForum.forums.forEach {
            list.add(it.item)
            transformToList(list, it)
        }
    }

    private fun transformToTree(list: List<ForumItemFlat>): ForumItemTree {
        val builders = list.map { ForumItemTreeBuilder(it) }
        val parents = LinkedHashMap<Int, ForumItemTreeBuilder>()
        val root = ForumItemTreeBuilder.createRoot()
        parents[root.item.id] = root
        builders.forEach {
            parents[it.item.id] = it
        }
        builders.forEach {
            if (it.item.id != it.item.parentId) {
                parents[it.item.parentId]?.addForum(it)
            }
        }
        return root.build()
    }

    private class ForumItemTreeBuilder(
        val item: ForumItemFlat,
        val forums: MutableList<ForumItemTreeBuilder> = mutableListOf()
    ) {
        companion object {
            fun createRoot(): ForumItemTreeBuilder {
                return ForumItemTreeBuilder(ForumItemFlat(-1, -1, -1, null))
            }
        }

        fun addForum(item: ForumItemTreeBuilder) {
            forums.add(item)
        }

        fun build(): ForumItemTree {
            return ForumItemTree(item, forums.map { it.build() })
        }
    }
}
