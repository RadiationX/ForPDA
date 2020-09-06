package forpdateam.ru.forpda.model.repository.forum

import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Observable
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
            .fromCallable { forumApi.getForums() }
            .runInIoToUi()

    fun getCache(): Single<ForumItemTree> = Single
            .fromCallable {
                ForumItemTree().apply {
                    forumApi.transformToTree(forumCache.getItems(), this)
                }
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
                val items = mutableListOf<ForumItemFlatBd>().apply {
                    transformToList(this, rootForum)
                }
                forumCache.saveItems(items)
            }
            .runInIoToUi()


    private fun transformToList(list: MutableList<ForumItemFlatBd>, rootForum: ForumItemTree) {
        rootForum.forums?.forEach {
            list.add(ForumItemFlatBd(it))
            transformToList(list, it)
        }
    }

}
