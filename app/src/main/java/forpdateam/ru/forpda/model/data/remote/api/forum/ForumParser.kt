package forpdateam.ru.forpda.model.data.remote.api.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
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
            .getParserPattern(scope.scope, scope.forums_from_search)
            .mapOnce(response) { rootMatcher ->
                val parentsList = ArrayList<Parent>()
                var lastParent = Parent(-1, -1)
                parentsList.add(lastParent)
                patternProvider
                    .getParserPattern(scope.scope, scope.forum_item_from_search)
                    .map(rootMatcher.require(1)) { matcher ->
                        val level = matcher.require(2).length / 2
                        if (level <= lastParent.level) {
                            //Удаление элементов, учитывая случай с резким скачком уровня вложенности
                            for (i in 0 until lastParent.level - level + 1)
                                parentsList.removeAt(parentsList.size - 1)
                            lastParent = parentsList[parentsList.size - 1]
                        }
                        val item = ForumItemFlat(
                            id = matcher.require(1).toInt(),
                            parentId = lastParent.id,
                            level = level,
                            title = matcher.require(3).fromHtml(),
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
            .getParserPattern(scope.scope, scope.rules_headers)
            .findAll(response) { headerMatcher ->
                items.add(
                    ForumRules.Item(
                        number = headerMatcher.require(1),
                        text = headerMatcher.require(2),
                        isHeader = true,
                    )
                )

                val itemContent = headerMatcher.require(3)
                patternProvider
                    .getParserPattern(scope.scope, scope.rules_items)
                    .findAll(itemContent) { itemMatcher ->
                        items.add(
                            ForumRules.Item(
                                number = itemMatcher.require(1),
                                text = itemMatcher.require(2),
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
            .getParserPattern(scope.scope, scope.announce)
            .mapOnce(response) {
                Announce(
                    title = it.require(1),
                    html = it.require(2)
                )
            }
        return requireNotNull(announce) {
            "Can't parse announce"
        }
    }
}
