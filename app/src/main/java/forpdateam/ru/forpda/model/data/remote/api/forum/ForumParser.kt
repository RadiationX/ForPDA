package forpdateam.ru.forpda.model.data.remote.api.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.extensions.findAll
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class ForumParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Forum

    private class Parent(
        val id: Int,
        val level: Int,
    )

    fun parseForums(response: String): List<ForumItemFlat> {
        return patternProvider
            .getPattern(scope.scope, scope.forums_from_search)
            .matcher(response)
            .mapOnce { rootMatcher ->
                val parentsList = ArrayList<Parent>()
                var lastParent = Parent(-1, -1)
                parentsList.add(lastParent)
                patternProvider
                    .getPattern(scope.scope, scope.forum_item_from_search)
                    .matcher(rootMatcher.group(1))
                    .map { matcher ->
                        val level = matcher.group(2).length / 2
                        if (level <= lastParent.level) {
                            //Удаление элементов, учитывая случай с резким скачком уровня вложенности
                            for (i in 0 until lastParent.level - level + 1)
                                parentsList.removeAt(parentsList.size - 1)
                            lastParent = parentsList[parentsList.size - 1]
                        }
                        val item = ForumItemFlat(
                            id = matcher.group(1).toInt(),
                            parentId = lastParent.id,
                            level = level,
                            title = matcher.group(3).fromHtml(),
                        )
                        if (level > lastParent.level) {
                            lastParent = Parent(item.id, level)
                            parentsList.add(lastParent)
                        }
                        item
                    }
            } ?: emptyList()
    }

    fun parseRules(response: String): ForumRules {
        val items = mutableListOf<ForumRules.Item>()
        patternProvider
            .getPattern(scope.scope, scope.rules_headers)
            .matcher(response)
            .findAll { headerMatcher ->
                items.add(
                    ForumRules.Item(
                        number = headerMatcher.group(1),
                        text = headerMatcher.group(2),
                        isHeader = true,
                    )
                )

                val itemContent = headerMatcher.group(3)
                patternProvider
                    .getPattern(scope.scope, scope.rules_items)
                    .matcher(itemContent)
                    .findAll { itemMatcher ->
                        items.add(
                            ForumRules.Item(
                                number = itemMatcher.group(1),
                                text = itemMatcher.group(2),
                                isHeader = false
                            )
                        )
                    }
            }
        return ForumRules(
            items = items,
            html = null
        )
    }

    fun parseAnnounce(response: String): Announce {
        val announce = patternProvider
            .getPattern(scope.scope, scope.announce)
            .matcher(response)
            .mapOnce {
                Announce(
                    title = it.group(1),
                    html = it.group(2)
                )
            }
        return requireNotNull(announce) {
            "Can't parse announce"
        }
    }
}
