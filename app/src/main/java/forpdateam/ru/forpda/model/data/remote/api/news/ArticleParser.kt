package forpdateam.ru.forpda.model.data.remote.api.news

import android.util.Log
import android.util.SparseArray
import forpdateam.ru.forpda.entity.remote.news.*
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.regex.parser.Node
import forpdateam.ru.forpda.model.data.remote.api.regex.parser.Parser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Matcher

class ArticleParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Articles

    fun parseArticles(response: String): List<NewsItem> = patternProvider
            .getPattern(scope.scope, scope.list)
            .matcher(response)
            .map { matcher ->
                NewsItem().apply {
                    val isReview = matcher.group(1) == null
                    if (!isReview) {
                        url = matcher.group(1)
                        id = matcher.group(2).toInt()
                        title = matcher.group(3).fromHtml().fromHtml()
                        imgUrl = matcher.group(4)
                        commentsCount = matcher.group(5).toInt()
                        date = matcher.group(6)
                        authorId = matcher.group(7).toInt()
                        author = matcher.group(8).fromHtml()
                        description = matcher.group(9).fromHtml()
                        matcher.group(10)?.let {
                            tags.addAll(parseTags(it))
                        }
                    } else {
                        url = matcher.group(11)
                        id = matcher.group(12).toInt()
                        imgUrl = matcher.group(13)
                        title = matcher.group(14).fromHtml().fromHtml()
                        commentsCount = matcher.group(15).toInt()
                        date = matcher.group(17).replace('-', '.')
                        author = matcher.group(18).fromHtml()
                        description = matcher.group(20).trim().fromHtml()
                    }
                }
            }

    fun parseArticle(response: String): DetailsPage = patternProvider
            .getPattern(scope.scope, ParserPatterns.Articles.detail_detector)
            .matcher(response)
            .mapOnce {
                val hasV1 = !it.group(1).isNullOrEmpty()
                val hasV2 = !it.group(2).isNullOrEmpty()
                when {
                    hasV1 -> parseArticleV1(response)
                    hasV2 -> parseArticleV2(response)
                    else -> null
                }
            } ?: throw Exception("Not found article type")

    private fun parseArticleV1(response: String): DetailsPage = patternProvider
            .getPattern(scope.scope, scope.detail)
            .matcher(response)
            .mapOnce { matcher ->
                DetailsPage().apply {
                    id = matcher.group(1).toInt()
                    imgUrl = matcher.group(3)
                    title = matcher.group(4).fromHtml()
                    matcher.group(5)?.let {
                        tags.addAll(parseTags(it))
                    }
                    date = matcher.group(6)
                    authorId = matcher.group(7).toInt()
                    author = matcher.group(8).fromHtml()
                    commentsCount = matcher.group(9).toInt()
                    html = matcher.group(10)
                    matcher.group(11)?.also {
                        materials.addAll(parseMaterials(it))
                    }
                    navId = matcher.group(12)

                    karmaMap = parseKarma(response)

                    commentsSource = matcher.group(13)?.let { comments ->
                        patternProvider
                                .getPattern(scope.scope, scope.exclude_form_comment)
                                .matcher(comments)
                                .replaceFirst("")
                    }

                    /*Comment commentTree = parseComments(getKarmaMap(), getCommentsSource());
                    setCommentTree(commentTree);*/
                }
            } ?: throw Exception("Not found article by pattern v1")

    private fun parseArticleV2(response: String): DetailsPage = patternProvider
            .getPattern(scope.scope, scope.detail_v2)
            .matcher(response)
            .mapOnce { matcher ->
                DetailsPage().apply {
                    id = matcher.group(1).toInt()

                    patternProvider
                            .getPattern(ParserPatterns.Global.scope, ParserPatterns.Global.meta_tags)
                            .matcher(response)
                            .findAll {
                                val metaTarget = it.group(1)
                                val metaType = it.group(2)
                                val metaContent = it.group(3)
                                if (metaTarget == "og" && metaType == "image") {
                                    imgUrl = metaContent
                                }
                            }

                    //imgUrl = matcher.group(3)
                    title = matcher.group(3).fromHtml()
                    date = matcher.group(4)

                    //Дефолтный юзер с ником News
                    authorId = 204809
                    author = "News"

                    commentsCount = matcher.group(5).toInt()
                    html = matcher.group(6)
                    matcher.group(7)?.let {
                        tags.addAll(parseTags(it))
                    }
                    matcher.group(8)?.also {
                        materials.addAll(parseMaterials(it))
                    }
                    navId = matcher.group(9)

                    karmaMap = parseKarma(response)

                    commentsSource = matcher.group(10)?.let { comments ->
                        patternProvider
                                .getPattern(scope.scope, scope.exclude_form_comment)
                                .matcher(comments)
                                .replaceFirst("")
                    }

                    /*Comment commentTree = parseComments(getKarmaMap(), getCommentsSource());
                    setCommentTree(commentTree);*/
                }
            } ?: throw Exception("Not found article by pattern v2")

    private fun parseMaterials(source: String): List<Material> = patternProvider
            .getPattern(scope.scope, scope.materials)
            .matcher(source)
            .map {
                Material().apply {
                    imageUrl = it.group(1)
                    id = it.group(2).toInt()
                    title = it.group(3).fromHtml()
                }
            }

    private fun parseTags(source: String): List<Tag> = patternProvider
            .getPattern(scope.scope, scope.tags)
            .matcher(source)
            .map {
                Tag().apply {
                    tag = it.group(1)
                    title = it.group(2).fromHtml()
                }
            }

    private fun parseKarma(source: String): SparseArray<Comment.Karma> {
        val result = SparseArray<Comment.Karma>()
        patternProvider
                .getPattern(scope.scope, scope.karmaSource)
                .matcher(source)
                .findOnce {
                    Log.e("kulolo", "karma: ${it.group(1)}")
                    patternProvider
                            .getPattern(scope.scope, scope.karma)
                            .matcher(it.group(1))
                            .findAll {
                                try {
                                    val commentId = it.group(1).toInt()
                                    result.put(commentId, Comment.Karma().apply {
                                        status = it.group(2).toInt()
                                        count = it.group(5).toInt()
                                    })
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                }
        return result
    }

    fun parseComments(karmaMap: SparseArray<Comment.Karma>, source: String?): Comment {
        val comments = Comment()
        if (source != null) {
            val document = Parser.parse(source)
            recurseComments(karmaMap, document, comments, 0)
        }
        return comments
    }


    private fun recurseComments(karmaMap: SparseArray<Comment.Karma>, root: Node, parentComment: Comment, argLevel: Int): Comment {
        var level = argLevel
        val rootComments = Parser.findNode(root, "ul", "class", "comment-list")
        val commentNodes = Parser.findChildNodes(rootComments, "li", null, null)

        /*if (commentNodes.size() == 0) {
            return null;
        }*/
        for (commentNode in commentNodes) {
            val comment = Comment()

            var id: String? = null
            var userId: String? = null
            var userNick: String? = null
            var date: String? = null
            var content: String? = null
            var matcher: Matcher
            val anchorNode = Parser.findNode(commentNode, "div", "id", "comment-") ?: continue

            id = anchorNode.getAttribute("id")
            if (id != null) {
                matcher = patternProvider
                        .getPattern(scope.scope, scope.comment_id)
                        .matcher(id)
                if (matcher.find()) {
                    id = matcher.group(1)
                    comment.id = Integer.parseInt(id)
                }
            }

            val deletedString = anchorNode.getAttribute("class")
            val isDeleted = deletedString != null && deletedString.contains("deleted")
            comment.isDeleted = isDeleted

            if (!isDeleted) {
                val avatarNode = Parser.findNode(commentNode, "a", "class", "comment-avatar")
                val nickNode = Parser.findNode(commentNode, "a", "class", "nickname")
                        ?: Parser.findNode(commentNode, "span", "class", "nickname")
                val metaNode = Parser.findNode(commentNode, "a", "class", "date")

                userId = avatarNode!!.getAttribute("href")
                if (userId != null) {
                    matcher = patternProvider
                            .getPattern(scope.scope, scope.comment_user_id)
                            .matcher(userId)
                    if (matcher.find()) {
                        userId = matcher.group(1)
                        comment.userId = Integer.parseInt(userId)
                    }
                }

                userNick = Parser.getHtml(nickNode, true)
                comment.userNick = ApiUtils.fromHtml(userNick)

                date = metaNode?.let { Parser.ownText(metaNode).trim() }
                comment.date = date
            }

            val contentNode = Parser.findNode(commentNode, "p", "class", "content")
            content = Parser.getHtml(contentNode, true)
            comment.content = ApiUtils.fromHtml(content)
            comment.level = level
            comment.karma = karmaMap.get(comment.id)

            parentComment.children.add(comment)

            level++
            recurseComments(karmaMap, commentNode, comment, level)
            level--
        }

        return parentComment
    }

}
