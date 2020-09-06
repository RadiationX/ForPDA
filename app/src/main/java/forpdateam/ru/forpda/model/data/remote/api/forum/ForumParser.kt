package forpdateam.ru.forpda.model.data.remote.api.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.*
import java.util.regex.Matcher

class ForumParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Forum

    fun parseForums(response: String): ForumItemTree = ForumItemTree().also { root ->
        patternProvider
                .getPattern(scope.scope, scope.forums_from_search)
                .matcher(response)
                .findOnce { rootMatcher ->
                    val parentsList = ArrayList<ForumItemTree>()
                    var lastParent = root
                    parentsList.add(lastParent)
                    patternProvider
                            .getPattern(scope.scope, scope.forum_item_from_search)
                            .matcher(rootMatcher.group(1))
                            .findAll { matcher ->
                                ForumItemTree().apply {
                                    id = matcher.group(1).toInt()
                                    level = matcher.group(2).length / 2
                                    title = matcher.group(3).fromHtml()
                                    if (level <= lastParent.level) {
                                        //Удаление элементов, учитывая случай с резким скачком уровня вложенности
                                        for (i in 0 until lastParent.level - level + 1)
                                            parentsList.removeAt(parentsList.size - 1)
                                        lastParent = parentsList[parentsList.size - 1]
                                    }
                                    parentId = lastParent.id
                                    lastParent.addForum(this)
                                    if (level > lastParent.level) {
                                        lastParent = this
                                        parentsList.add(lastParent)
                                    }
                                }
                            }
                    parentsList.clear()
                }
    }

    fun parseRules(response: String): ForumRules = ForumRules().also { rules ->
        var itemMatcher: Matcher? = null
        patternProvider
                .getPattern(scope.scope, scope.rules_headers)
                .matcher(response)
                .findAll { headerMatcher ->
                    rules.addItem(ForumRules.Item().apply {
                        isHeader = true
                        number = headerMatcher.group(1)
                        text = headerMatcher.group(2)
                    })

                    val itemContent = headerMatcher.group(3)
                    itemMatcher = itemMatcher?.reset(itemContent) ?: patternProvider
                            .getPattern(scope.scope, scope.rules_items)
                            .matcher(itemContent)
                    itemMatcher
                            ?.findAll { itemMatcher ->
                                rules.addItem(ForumRules.Item().apply {
                                    number = itemMatcher.group(1)
                                    text = itemMatcher.group(2)
                                })
                            }
                }
    }

    fun parseAnnounce(response: String): Announce = Announce().also { data ->
        patternProvider
                .getPattern(scope.scope, scope.announce)
                .matcher(response)
                .findOnce {
                    data.title = it.group(1)
                    data.html = it.group(2)
                }
    }
}
