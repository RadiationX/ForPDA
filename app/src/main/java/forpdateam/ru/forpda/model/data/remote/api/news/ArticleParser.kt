package forpdateam.ru.forpda.model.data.remote.api.news

import android.util.SparseArray
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.Material
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.entity.remote.news.Tag
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.GlobalParser
import forpdateam.ru.forpda.model.data.remote.api.regex.parser.Node
import forpdateam.ru.forpda.model.data.remote.api.regex.parser.Parser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class ArticleParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val globalParser: GlobalParser
) : BaseParser() {

    private val scope = ParserPatterns.Articles

    fun parseArticles(response: String): List<NewsItem> = patternProvider
        .getRegexParser(scope.scope, scope.list)
        .map(response) { matcher ->
            NewsItem(
                url = matcher.require(1),
                id = ArticleId(matcher.require(2).toInt()),
                title = matcher.require(3).fromHtml(),
                imgUrl = matcher.require(4),
                commentsCount = matcher.require(5).toInt(),
                date = matcher.require(6),
                author = User(
                    id = UserId(matcher.require(7).toInt()),
                    nick = matcher.require(8).fromHtml(),
                ),
                description = matcher.require(9).fromHtml(),
                tags = matcher.get(10)?.let { parseTags(it) }.orEmpty(),
                avatar = null
            )
        }

    fun parseArticle(response: String): DetailsPage = patternProvider
        .getRegexParser(scope.scope, ParserPatterns.Articles.detail_detector)
        .mapOnce(response) {
            val hasV1 = !it.get(1).isNullOrEmpty()
            val hasV2 = !it.get(2).isNullOrEmpty()
            when {
                hasV1 -> parseArticleV1(response)
                hasV2 -> parseArticleV2(response)
                else -> null
            }
        } ?: throw Exception("Not found article type")

    private fun parseArticleV1(response: String): DetailsPage = patternProvider
        .getRegexParser(scope.scope, scope.detail)
        .mapOnce(response) { matcher ->
            DetailsPage(
                id = ArticleId(matcher.require(1).toInt()),
                imgUrl = matcher.require(3),
                title = matcher.require(4).fromHtml(),
                tags = matcher.get(5)?.let { parseTags(it) }.orEmpty(),
                date = matcher.require(6),
                author = User(
                    id = UserId(matcher.require(7).toInt()),
                    nick = matcher.require(8).fromHtml(),
                ),
                commentsCount = matcher.require(9).toInt(),
                html = matcher.require(10),
                materials = matcher.get(11)?.let { parseMaterials(it) }.orEmpty(),
                karmaMap = parseKarma(response),
                commentsSource = matcher.get(13)?.let { parseExcludeFormComment(it) },
            )
        } ?: throw Exception("Not found article by pattern v1")

    private fun parseArticleV2(response: String): DetailsPage = patternProvider
        .getRegexParser(scope.scope, scope.detail_v2)
        .mapOnce(response) { matcher ->
            val imgUrl: String? = globalParser
                .parseMetaTags(response)
                .find { it.target == "og" && it.type == "image" }
                ?.content

            DetailsPage(
                id = ArticleId(matcher.require(1).toInt()),
                imgUrl = requireNotNull(imgUrl) { "imgUrl" },
                title = matcher.require(3).fromHtml(),
                date = matcher.require(4),
                //Дефолтный юзер с ником News
                author = User(
                    id = UserId(204809),
                    nick = "News",
                ),
                commentsCount = matcher.require(5).toInt(),
                html = matcher.require(6),
                tags = matcher.get(7)?.let { parseTags(it) }.orEmpty(),
                materials = matcher.get(8)?.let { parseMaterials(it) }.orEmpty(),
                karmaMap = parseKarma(response),
                commentsSource = matcher.get(10)?.let { parseExcludeFormComment(it) },
            )
        } ?: throw Exception("Not found article by pattern v2")

    private fun parseExcludeFormComment(source: String): String {
        return patternProvider
            .getPattern(scope.scope, scope.exclude_form_comment)
            .matcher(source)
            .replaceFirst("")
    }

    private fun parseMaterials(source: String): List<Material> = patternProvider
        .getRegexParser(scope.scope, scope.materials)
        .map(source) {
            Material(
                imageUrl = it.require(1),
                id = ArticleId(it.require(2).toInt()),
                title = it.require(3).fromHtml()
            )
        }

    private fun parseTags(source: String): List<Tag> = patternProvider
        .getRegexParser(scope.scope, scope.tags)
        .map(source) {
            Tag(
                tag = it.require(1),
                title = it.require(2).fromHtml()
            )
        }

    private fun parseKarma(source: String): SparseArray<Comment.Karma> {
        val result = SparseArray<Comment.Karma>()
        patternProvider
            .getRegexParser(scope.scope, scope.karmaSource)
            .findOnce(source) { sourceMatcher ->
                patternProvider
                    .getRegexParser(scope.scope, scope.karma)
                    .findAll(sourceMatcher.require(1)) {
                        try {
                            val commentId = it.require(1).toInt()
                            result.put(
                                commentId,
                                Comment.Karma(
                                    status = it.require(2).toInt(),
                                    count = it.require(5).toInt()
                                )
                            )
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
            }
        return result
    }

    suspend fun parseComments(
        karmaMap: SparseArray<Comment.Karma>,
        source: String?
    ): List<Comment> {
        return withContext(Dispatchers.Default) {
            val comments = CommentNode()
            if (source != null) {
                val document = Parser.parse(source)
                recurseComments(karmaMap, document, comments, 0)
            }
            commentsToList(comments)
        }
    }

    private fun commentsToList(comment: CommentNode): ArrayList<Comment> {
        val comments = ArrayList<Comment>()
        recurseCommentsToList(comments, comment)
        return comments
    }

    private fun recurseCommentsToList(comments: ArrayList<Comment>, comment: CommentNode) {
        for (child in comment.children) {
            comments.add(child.toComment())
            recurseCommentsToList(comments, child)
        }
    }

    private fun recurseComments(
        karmaMap: SparseArray<Comment.Karma>,
        root: Node,
        parentComment: CommentNode,
        argLevel: Int
    ): CommentNode {
        var level = argLevel
        val rootComments = Parser.findNode(root, "ul", "class", "comment-list")
        requireNotNull(rootComments)
        val commentNodes = Parser.findChildNodes(rootComments, "li", null, null)

        /*if (commentNodes.size() == 0) {
            return null;
        }*/
        for (commentNode in commentNodes) {
            val comment = CommentNode()

            val anchorNode = Parser.findNode(commentNode, "div", "id", "comment-") ?: continue

            comment.id = patternProvider
                .getRegexParser(scope.scope, scope.comment_id)
                .requireOnce(anchorNode.getAttribute("id")!!) {
                    it.require(1).toInt()
                }

            val deletedString = anchorNode.getAttribute("class")
            val isDeleted = deletedString != null && deletedString.contains("deleted")
            comment.isDeleted = isDeleted

            if (!isDeleted) {
                val avatarNode = Parser.findNode(commentNode, "a", "class", "comment-avatar")
                val nickNode = Parser.findNode(commentNode, "a", "class", "nickname")
                    ?: Parser.findNode(commentNode, "span", "class", "nickname")
                val dateNode = Parser.findNode(commentNode, "a", "class", "date")
                requireNotNull(avatarNode)
                requireNotNull(nickNode)
                requireNotNull(dateNode)

                comment.userId = patternProvider
                    .getRegexParser(scope.scope, scope.comment_user_id)
                    .requireOnce(avatarNode.getAttribute("href")!!) {
                        it.require(1).toInt()
                    }
                comment.userNick = Parser.getHtml(nickNode, true).fromHtml()
                comment.date = Parser.ownText(dateNode).trim()
            }

            val contentNode = Parser.findNode(commentNode, "p", "class", "content")
                ?: Parser.findNode(commentNode, "div", "class", "content")
            requireNotNull(contentNode)
            comment.content = Parser.getHtml(contentNode, true).fromHtml()
            comment.level = level
            comment.karma = karmaMap.get(comment.id)

            parentComment.children.add(comment)

            level++
            recurseComments(karmaMap, commentNode, comment, level)
            level--
        }

        return parentComment
    }

    private fun CommentNode.toComment(): Comment {
        return Comment(
            id = CommentId(id),
            user = User(UserId(userId), userNick!!),
            date = date,
            content = content,
            isDeleted = isDeleted,
            level = level,
            karma = karma
        )
    }

    private class CommentNode {
        var id: Int = 0
        var userId: Int = 0
        var userNick: String? = null
        var date: String? = null
        var content: String? = null
        var isDeleted = false
        val children = mutableListOf<CommentNode>()
        var level: Int = 0
        var karma: Comment.Karma? = null
    }
}
