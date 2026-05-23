package ru.radiationx.links.parser.helpers

import ru.radiationx.coretypes.UserId
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

internal enum class UserIdCase(val value: String) {
    Qms("mid"),
    Reputation("mid"),
    ShowUser("showuser")
}

internal fun LinkUrl.parseUserId(case: UserIdCase): UserId? {
    return query(case.value)?.toIntOrNull()?.let { UserId(it) }
}

internal fun LinkUrlBuilder.query(userId: UserId, case: UserIdCase) {
    query(case.value, userId.id)
}
