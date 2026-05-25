package forpdateam.ru.forpda.model.interactors.news

import android.util.Log
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.replace
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.presentation.articles.detail.ArticleDetailExtra
import forpdateam.ru.forpda.presentation.articles.detail.ArticleTemplate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArticleInteractor @Inject constructor(
    val argExtra: ArticleDetailExtra,
    private val newsRepository: NewsRepository,
    private val articleTemplate: ArticleTemplate
) {

    private val dataState = MutableStateFlow<DetailsPage?>(null)
    private val commentsState = MutableStateFlow<List<Comment>?>(null)

    fun observeData(): Flow<DetailsPage> = dataState.filterNotNull()
    fun observeComments(): Flow<List<Comment>> = commentsState.filterNotNull()

    suspend fun loadArticle(): DetailsPage {
        val details = newsRepository.getDetails(argExtra.articleId)
        return articleTemplate.mapEntity(details)
    }

    suspend fun likeComment(commentId: Int) {
        newsRepository.likeComment(argExtra.articleId, commentId)
        updateComments { comments ->
            comments.replace(
                condition = { it.id == commentId },
                map = {
                    val karma = it.karma
                    it.copy(
                        karma = karma?.copy(
                            status = Comment.Karma.LIKED,
                            count = karma.count + 1
                        )
                    )
                }
            )
        }
    }

    suspend fun sendPoll(from: String, pollId: Int, answersId: IntArray): DetailsPage {
        return newsRepository.sendPoll(from, pollId, answersId)
    }

    suspend fun replyComment(commentId: Int, comment: String): DetailsPage {
        return newsRepository
            .replyComment(argExtra.articleId, commentId, comment)
            .let { articleTemplate.mapEntity(it) }
            .also { updateData(it) }
    }

    private fun updateData(article: DetailsPage) {
        dataState.value = article
        parseComments(article)
    }

    private fun updateComments(block: (List<Comment>) -> List<Comment>) {
        commentsState.update {
            it?.let(block)
        }
    }

    private fun parseComments(article: DetailsPage) {
        GlobalScope.launch {
            coRunCatching {
                newsRepository.getComments(article)
            }.onSuccess {
                commentsState.value = it
            }.onFailure {
                Log.d("ArticleInteractor", "parseComments", it)
            }
        }
    }
}