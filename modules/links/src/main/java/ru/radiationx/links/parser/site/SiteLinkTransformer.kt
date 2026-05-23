package ru.radiationx.links.parser.site

import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.PageNumber
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query
import ru.radiationx.links.url.segment

//https://4pda.to/2025/page/13/
//https://4pda.to/2025/5/page/13/
//https://4pda.to/2025/5/1/page/13/
//https://4pda.to/2025/5/1/page/
//https://4pda.to/2025/5/1/
//https://4pda.to/tag/smartphones/
//https://4pda.to/tag/smartphones/page/2
//https://4pda.to/reviews/smartphones/page/2
//https://4pda.to/honor/page/2/
//https://4pda.to/2026/05/index.php/page/2
//https://4pda.to/2026/05/21/456675/obzor_oppo_find_x9_ultra_vozmozhno_glavnyj_fotoflagman_goda/
//https://4pda.to/2026/05/21/456675
//https://4pda.to/2026/05/21/456675#comment10592113
//https://4pda.to/2026/05/21/?p=456764#comment10592113
//https://4pda.to/?p=456764#comment10592113
//https://4pda.to/index.php?p=456764
//https://4pda.to/index.php?p=456764#comment10592113
//https://4pda.to/2026/05/22/456764/i_vsyo_taki_on_suschestvuet_trump_mobile_t1_raspakovali_na_kameru_video/#comment10592113
//https://4pda.to/news/newer/1757418300/
//https://4pda.to/news/older/1757418300/
//https://4pda.to/news/older/1757418300/
//https://4pda.to/?s=%FC%E1%FC%E1
//https://4pda.to/page/1/?s=xiaomi
//https://4pda.to/?s=xiaomi
//https://4pda.to/index.php?s=xiaomi
//https://4pda.to/reviews/smartphones/?s=nothing
//https://4pda.to/reviews/smartphones/index.php?s=nothing
internal object SiteLinkTransformer {

    private val commentIdRegex = Regex("comment-(\\d+)")
    private val relativeSegments = setOf("newer", "older")
    private val forbiddenCategories = relativeSegments + setOf("page", "index.php")

    fun build(builder: LinkUrlBuilder, link: Links.Site): LinkUrl {
        with(builder) {
            when (link) {
                is Links.Site.Page -> {
                    fillPaths(link.paths)
                    fillPageNumber(link.pageNumber)
                }

                is Links.Site.Details -> {
                    fillPaths(link.paths)
                    query("p", link.articleId.id)
                    link.commentId?.also { fragment("comment${it.id}") }
                }

                is Links.Site.RelativePage -> {
                    segment("news")
                    when (link.type) {
                        Links.Site.RelativePage.Type.Newer -> segment("newer")
                        Links.Site.RelativePage.Type.Older -> segment("older")
                    }
                    segment(link.timestampSec)
                }

                is Links.Site.Search -> {
                    fillPageNumber(link.pageNumber)
                    query("s", link.text)
                }
            }
        }
        return builder.build()
    }

    private fun LinkUrlBuilder.fillPaths(paths: Links.Site.Paths?) {
        when (paths) {
            is Links.Site.Paths.Date -> {
                segment(paths.year)
                paths.month?.also { segment(it) }
                paths.day?.also { segment(it) }
            }

            is Links.Site.Paths.Tag -> {
                segment("tag")
                segment(paths.tag)
            }

            is Links.Site.Paths.Category -> {
                segment(paths.category)
                paths.subCategory?.also { segment(it) }
            }

            null -> {}
        }
    }

    private fun LinkUrlBuilder.fillPageNumber(pageNumber: PageNumber?) {
        if (pageNumber == null) return
        segment("page")
        segment(pageNumber.value)
    }

    fun parse(url: LinkUrl): Links.Site? {
        val details = parseDetailsQuery(url)
        if (details != null) return details

        val relativePage = parseRelative(url)
        if (relativePage != null) return relativePage

        val search = parseSearchQuery(url)
        if (search != null) return search

        val date = parseDatePaths(url)?.let { parseWithDate(url, it) }
        if (date != null) return date

        val tag = parseTagPaths(url)?.let { parseWithTag(url, it) }
        if (tag != null) return tag

        val category = parseCategoryPaths(url)?.let { parseWithCategory(url, it) }
        if (category != null) return category

        return null
    }

    private fun parseDetailsQuery(url: LinkUrl): Links.Site.Details? {
        val articleId = url.query("p")?.toIntOrNull()?.let { ArticleId(id = it) } ?: return null
        val commentId = parseCommentId(url)
        return Links.Site.Details(articleId = articleId, commentId = commentId, paths = null)
    }

    private fun parseSearchQuery(url: LinkUrl): Links.Site.Search? {
        val querySearch = url.query("s") ?: return null
        val paths = parseCategoryPaths(url)
        val pageNumber = parsePageNumberAfter(url, paths)
        return Links.Site.Search(text = querySearch, pageNumber = pageNumber)
    }

    private fun parseRelative(url: LinkUrl): Links.Site.RelativePage? {
        if (url.segment(0) != "news") return null
        val type = when (url.segment(1)) {
            "newer" -> Links.Site.RelativePage.Type.Newer
            "older" -> Links.Site.RelativePage.Type.Older
            else -> return null
        }
        val timestampSec = tryParseSegmentNumber(url, 2) ?: return null
        return Links.Site.RelativePage(timestampSec = timestampSec, type = type)
    }

    private fun parseWithDate(url: LinkUrl, paths: Links.Site.Paths.Date): Links.Site? {
        if (paths.lastIndex() == 2) {
            val articleId = tryParseSegmentNumber(url, 3)?.toInt()?.let { ArticleId(it) }
            if (articleId != null) {
                val commentId = parseCommentId(url)
                return Links.Site.Details(articleId = articleId, commentId = commentId, paths = paths)
            }
        }

        val pageNumber = parsePageNumberAfter(url, paths)
        return Links.Site.Page(pageNumber, paths)
    }

    private fun parseWithTag(url: LinkUrl, paths: Links.Site.Paths.Tag): Links.Site.Page {
        val pageNumber = parsePageNumberAfter(url, paths)
        return Links.Site.Page(pageNumber, paths)
    }

    private fun parseWithCategory(url: LinkUrl, paths: Links.Site.Paths.Category): Links.Site.Page {
        val pageNumber = parsePageNumberAfter(url, paths)
        return Links.Site.Page(pageNumber, paths)
    }

    private fun parseDatePaths(url: LinkUrl): Links.Site.Paths.Date? {
        val year = tryParseSegmentNumber(url, 0)?.toInt() ?: return null
        val month = tryParseSegmentNumber(url, 1)?.toInt()
        val day = tryParseSegmentNumber(url, 2)?.toInt()
        return Links.Site.Paths.Date(year = year, month = month, day = day)
    }

    private fun parseTagPaths(url: LinkUrl): Links.Site.Paths.Tag? {
        if (url.segment(0) != "tag") return null
        val tag = url.segment(1) ?: return null
        return Links.Site.Paths.Tag(tag = tag)
    }

    private fun parseCategoryPaths(url: LinkUrl): Links.Site.Paths.Category? {
        val category = url.segment(0)?.takeIf { it !in forbiddenCategories } ?: return null
        val subCategory = url.segment(1)?.takeIf { it !in forbiddenCategories }
        return Links.Site.Paths.Category(category, subCategory)
    }

    private fun tryParseSegmentNumber(url: LinkUrl, index: Int): Long? {
        val segment1 = url.segment(index) ?: return null
        if (!segment1.isNotEmpty() || !segment1.all { it.isDigit() }) return null
        return segment1.toLongOrNull()
    }

    private fun parsePageNumberAfter(url: LinkUrl, paths: Links.Site.Paths?): PageNumber {
        val pageSegmentIndex = (paths?.lastIndex() ?: -1) + 1
        if (url.segment(pageSegmentIndex) != "page") return PageNumber.default
        val pageNumberIndex = pageSegmentIndex + 1
        val page = tryParseSegmentNumber(url, pageNumberIndex)?.toInt() ?: return PageNumber.default
        return PageNumber(value = page)
    }

    private fun parseCommentId(url: LinkUrl): CommentId? {
        val fragment = url.fragment ?: return null
        val id = commentIdRegex.find(fragment)?.let { it.groupValues[1].toIntOrNull() } ?: return null
        return CommentId(id = id)
    }

    private fun Links.Site.Paths.lastIndex(): Int {
        return when (this) {
            is Links.Site.Paths.Date -> when {
                day != null -> 2
                month != null -> 1
                else -> 0
            }

            is Links.Site.Paths.Tag -> 1
            is Links.Site.Paths.Category -> when {
                subCategory != null -> 1
                else -> 0
            }
        }
    }
}