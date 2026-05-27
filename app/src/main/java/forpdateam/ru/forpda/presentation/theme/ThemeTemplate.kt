package forpdateam.ru.forpda.presentation.theme

import android.content.Context
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.theme.PollQuestionItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import forpdateam.ru.forpda.ui.TemplateManager
import ru.radiationx.coretypes.PostId
import ru.radiationx.links.parser.LinkTransformer
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class ThemeTemplate @Inject constructor(
    private val context: Context,
    private val templateManager: TemplateManager,
    private val authHolder: AuthHolder,
    private val topicPreferencesHolder: TopicPreferencesHolder,
    private val linkTransformer: LinkTransformer
) {

    private val firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])")

    fun mapEntity(page: ThemePage): ThemePage = page.copy(html = mapString(page).asDeferredData())

    fun mapString(page: ThemePage): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_THEME)

        val authState = authHolder.get()
        val authorized = authState.isAuth()
        template.apply {
            templateManager.fillStaticStrings(this)
            val prevDisabled = !page.pagination.hasPrev()
            val nextDisabled = !page.pagination.hasNext()

            setVariableOpt("style_type", templateManager.getThemeType())

            setVariableOpt("topic_title", ApiUtils.htmlEncode(page.title))
            setVariableOpt("topic_description", ApiUtils.htmlEncode(page.desc))
            setVariableOpt("topic_url", linkTransformer.build(page.link).toString())

            setVariableOpt("all_pages_int", page.pagination.all)
            setVariableOpt("posts_on_page_int", page.pagination.perPage)
            setVariableOpt("current_page_int", page.pagination.current)

            setVariableOpt("authorized_bool", authorized.toString())
            setVariableOpt("is_curator_bool", false.toString())
            setVariableOpt("member_id_int", authState.asAuth()?.userId?.id ?: 0)
            setVariableOpt("elem_to_scroll", page.anchor?.value)
            setVariableOpt("body_type", "topic")

            setVariableOpt(
                "navigation_disable",
                TempHelper.getDisableStr(prevDisabled && nextDisabled)
            )
            setVariableOpt("first_disable", TempHelper.getDisableStr(prevDisabled))
            setVariableOpt("prev_disable", TempHelper.getDisableStr(prevDisabled))
            setVariableOpt("next_disable", TempHelper.getDisableStr(nextDisabled))
            setVariableOpt("last_disable", TempHelper.getDisableStr(nextDisabled))

            setVariableOpt("in_favorite_bool", java.lang.Boolean.toString(page.isInFavorite))
            val isEnableAvatars = topicPreferencesHolder.showAvatars.get()
            setVariableOpt("enable_avatars_bool", java.lang.Boolean.toString(isEnableAvatars))
            setVariableOpt("enable_avatars", if (isEnableAvatars) "show_avatar" else "hide_avatar")
            setVariableOpt(
                "avatar_type",
                if (topicPreferencesHolder.circleAvatars.get()) "circle_avatar" else "square_avatar"
            )


            val hatPostId: PostId? = page.posts.firstOrNull()?.post?.id
            var letterMatcher: Matcher? = null
            for (themePost in page.posts) {
                val post = themePost.post
                setVariableOpt("user_online", if (post.isOnline) "online" else "")
                setVariableOpt("post_id", post.id.id)
                setVariableOpt("user_id", post.user.id.id)

                //Post header
                setVariableOpt("avatar", post.user.avatar)
                setVariableOpt(
                    "none_avatar",
                    if (post.user.avatar.isNullOrEmpty()) "none_avatar" else ""
                )

                letterMatcher = letterMatcher?.reset(post.user.nick) ?: firstLetter.matcher(post.user.nick)
                val letter: String = letterMatcher?.run {
                    if (find()) group(1) else null
                } ?: post.user.nick.takeIf { it.isNotEmpty() }?.substring(0, 1).orEmpty()

                setVariableOpt("nick_letter", letter)
                setVariableOpt("nick", ApiUtils.htmlEncode(post.user.nick))
                setVariableOpt("curator", if (post.isCurator) "curator" else "")
                setVariableOpt("group_color", post.groupColor)
                setVariableOpt("group", post.group)
                setVariableOpt("reputation", post.reputation)
                setVariableOpt("date", post.date)
                setVariableOpt("number", themePost.number)

                //Post body
                if (page.posts.size > 1 && hatPostId == post.id) {
                    val hatOpened =
                        topicPreferencesHolder.hatOpened.get() || prevDisabled || page.isHatOpen
                    setVariableOpt("hat_state_class", if (hatOpened) "open" else "close")
                    //t.setVariableOpt("hat_body_state", prevDisabled || page.isHatOpen() ? "" : "hidden");
                    addBlockOpt("hat_button")
                    addBlockOpt("hat_content_start")
                    addBlockOpt("hat_content_end")
                } else {
                    setVariableOpt("hat_state_class", "")
                }
                setVariableOpt("body", post.body)

                //Post footer

                if (!authorized || post.canReport)
                    addBlockOpt("report_block")
                if (!authorized || (page.canQuote && post.user.id != authState.userId))
                    addBlockOpt("reply_block")
                if (!authorized || post.user.id != authState.userId)
                    addBlockOpt("vote_block")
                if (!authorized || post.canDelete)
                    addBlockOpt("delete_block")
                if (!authorized || post.canEdit)
                    addBlockOpt("edit_block")

                addBlockOpt("post")
            }

            //Poll block
            page.poll?.let { poll ->
                setVariableOpt("poll_state_class", if (page.isPollOpen) "open" else "close")
                val isResult = poll.isResult
                setVariableOpt("poll_type", if (isResult) "result" else "default")
                setVariableOpt(
                    "poll_title",
                    if (poll.title.isNullOrEmpty() || poll.title == "-") {
                        context.getString(R.string.poll)
                    } else {
                        poll.title
                    }
                )

                for (question in poll.questions) {
                    setVariableOpt("question_title", question.title)

                    for (questionItem in question.questionItems) {
                        when (questionItem) {
                            is PollQuestionItem.Regular -> {
                                setVariableOpt("question_item_title", questionItem.title)
                                setVariableOpt("question_item_type", questionItem.type)
                                setVariableOpt("question_item_name", questionItem.name)
                                setVariableOpt("question_item_value", questionItem.value)
                                addBlockOpt("poll_default_item")
                            }

                            is PollQuestionItem.Result -> {
                                setVariableOpt("question_item_title", questionItem.title)
                                setVariableOpt("question_item_votes", questionItem.votes)
                                setVariableOpt(
                                    "question_item_percent",
                                    questionItem.percent.toString()
                                )
                                addBlockOpt("poll_result_item")
                            }
                        }
                    }
                    addBlockOpt("poll_question_block")
                }
                setVariableOpt("poll_votes_count", poll.votesCount)
                if (poll.haveButtons()) {
                    if (poll.voteButton)
                        addBlockOpt("poll_vote_button")
                    if (poll.showResultsButton)
                        addBlockOpt("poll_show_results_button")
                    if (poll.showPollButton)
                        addBlockOpt("poll_show_poll_button")
                    addBlockOpt("poll_buttons")
                }
                addBlockOpt("poll_block")
            }
        }

        val result = template.generateOutput()
        template.reset()
        return result
    }

}