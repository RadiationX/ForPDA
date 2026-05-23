package ru.radiationx.links.parser.helpers

import ru.radiationx.coretypes.ForumId
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

internal enum class ForumIdCase(val value: String) {
    Forum("f"),
    ShowForum("showforum"),
    Search("forums"),
    SearchArray("forums[]")
}

internal fun String.parseForumId(): ForumId? {
    return toIntOrNull()?.let { ForumId(it) }
}

internal fun LinkUrl.parseForumId(case: ForumIdCase): ForumId? {
    return query(case.value)?.parseForumId()
}

internal fun LinkUrlBuilder.query(forumId: ForumId, case: ForumIdCase) {
    query(case.value, forumId.id)
}
