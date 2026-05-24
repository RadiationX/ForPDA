package ru.radiationx.links

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageNumber
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId

sealed interface Link : Parcelable {


    //https://4pda.to/2025/page/13/
    //https://4pda.to/2025/5/page/13/
    //https://4pda.to/2025/5/1/page/13/
    //https://4pda.to/2025/5/1/page/
    //https://4pda.to/2025/5/1/
    //https://4pda.to/tag/smartphones/
    //https://4pda.to/reviews/smartphones/
    //https://4pda.to/honor/page/2/

    //https://4pda.to/2026/05/21/456675/obzor_oppo_find_x9_ultra_vozmozhno_glavnyj_fotoflagman_goda/
    //https://4pda.to/2026/05/21/456675
    //https://4pda.to/?p=456764
    //https://4pda.to/index.php?p=456764
    //https://4pda.to/news/newer/1757418300/
    //https://4pda.to/news/older/1757418300/

    //https://4pda.to/?s=%FC%E1%FC%E1
    //https://4pda.to/reviews/smartphones/?s=nothing
    //https://4pda.to/reviews/smartphones/index.php?s=nothing
    sealed interface Site : Link {

        @Parcelize
        data class Page(val pageNumber: PageNumber, val paths: Paths?) : Site

        @Parcelize
        data class RelativePage(val timestampSec: Long, val type: Type) : Site {

            @Parcelize
            enum class Type : Parcelable {
                Newer,
                Older
            }
        }

        @Parcelize
        data class Details(val articleId: ArticleId, val commentId: CommentId?, val paths: Paths.Date?) : Site

        @Parcelize
        data class Search(val text: String, val pageNumber: PageNumber) : Site

        sealed interface Paths : Parcelable {

            @Parcelize
            data class Date(val year: Int, val month: Int?, val day: Int?) : Paths

            @Parcelize
            data class Tag(val tag: String) : Paths

            @Parcelize
            data class Category(val category: String, val subCategory: String?) : Paths
        }
    }

    //https://4pda.to/devdb/
    //https://4pda.to/devdb/phones
    //https://4pda.to/devdb/pad
    //https://4pda.to/devdb/ebook
    //https://4pda.to/devdb/smartwatch
    //https://4pda.to/devdb/phones/all
    //https://4pda.to/devdb/phones/all#letter-M
    //https://4pda.to/devdb/phones/select/
    //https://4pda.to/devdb/phones/select/all
    //https://4pda.to/devdb/phones/apple
    //https://4pda.to/devdb/phones/apple/all
    //https://4pda.to/devdb/phones/apple?sort=year
    //https://4pda.to/devdb/phones/apple?sort=year&sort-year=desc
    //https://4pda.to/devdb/phones/apple?sort=year&sort-year=asc
    //https://4pda.to/devdb/phones/apple?sort=rating
    //https://4pda.to/devdb/phones/apple?sort=rating&sort-rating=desc
    //https://4pda.to/devdb/phones/apple?sort=rating&sort-rating=asc
    //https://4pda.to/devdb/phones/apple?sort=title
    //https://4pda.to/devdb/phones/apple?sort=title&sort-title=desc
    //https://4pda.to/devdb/phones/apple?sort=title&sort-title=asc
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite:8_128_256
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#specification
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#comments
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#discussions
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#reviews
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#firmware
    //https://4pda.to/devdb/xiaomi_mi_note_10_lite#prices
    //https://4pda.to/devdb/search?s=nothing
    sealed interface DevDb : Link {

        @Parcelize
        data object Categories : DevDb

        @Parcelize
        data class Brands(val categoryId: DevDbCategoryId, val letter: String?) : DevDb

        @Parcelize
        data class Devices(val devicesId: DevDbDevicesId, val sort: Sort?) : DevDb {

            @Parcelize
            data class Sort(val field: String, val order: Order?) : Parcelable {

                @Parcelize
                enum class Order : Parcelable {
                    Asc,
                    Desc
                }
            }
        }

        @Parcelize
        data class Device(val deviceId: DevDbDeviceId, val tab: String?) : DevDb

        @Parcelize
        data class Search(val text: String) : DevDb
    }

    sealed interface Board : Link {

        //https://4pda.to/forum/index.php?act=auth
        //https://4pda.to/forum/index.php?act=auth#auth
        //https://4pda.to/forum/index.php?act=auth#reg
        //https://4pda.to/forum/index.php?act=auth#lostpass
        //https://4pda.to/forum/index.php?act=auth&action=registration#reg2
        sealed interface Auth : Board {

            @Parcelize
            data object LoginForm : Auth
        }

        //https://4pda.to/forum/
        //https://4pda.to/forum/index.php
        //https://4pda.to/forum/index.php?act=idx
        @Parcelize
        data object Root : Board

        //https://4pda.to/forum/index.php?act=boardrules
        @Parcelize
        data object Rules : Board

        //https://4pda.to/forum/index.php?showforum=956
        //https://4pda.to/forum/index.php?showforum=956&st=150
        //https://4pda.to/forum/lofiversion/index.php?f956.html
        //https://4pda.to/forum/lofiversion/index.php?f956-150.html
        //https://4pda.to/forum/lofiversion/index.php?f956
        //https://4pda.to/forum/lofiversion/index.php?f956-150
        @Parcelize
        data class Forum(val forumId: ForumId, val offset: PageOffset) : Board

        //https://4pda.to/forum/index.php?act=fav
        //https://4pda.to/forum/index.php?act=fav&type=all
        //https://4pda.to/forum/index.php?act=fav&type=topics
        //https://4pda.to/forum/index.php?act=fav&type=forums
        //https://4pda.to/forum/index.php?act=fav&st=30
        //https://4pda.to/forum/index.php?act=fav&sort_key=title&sort_by=A-Z
        //https://4pda.to/forum/index.php?act=fav&sort_key=last_post&sort_by=Z-A
        @Parcelize
        data class Favorite(val offset: PageOffset, val type: Type, val sort: Sort) : Board {

            @Parcelize
            enum class Type : Parcelable {
                All,
                Forums,
                Topics
            }

            @Parcelize
            data class Sort(val key: Key, val order: Order) : Parcelable {

                @Parcelize
                enum class Key : Parcelable {
                    Title,
                    LastPost
                }

                enum class Order {
                    Asc,
                    Desc
                }
            }
        }


        //https://4pda.to/forum/index.php?act=mentions&st=0
        @Parcelize
        data class Mentions(val offset: PageOffset) : Board


        //https://4pda.to/forum/index.php?act=announce&f=283&st=239
        @Parcelize
        data class Announce(val announceId: AnnounceId) : Board

        //https://4pda.to/forum/index.php?showuser=4575561
        @Parcelize
        data class Profile(val userId: UserId) : Board

        //https://4pda.to/forum/index.php?act=qms
        //https://4pda.to/forum/index.php?act=qms&mid=7898206
        //https://4pda.to/forum/index.php?act=qms&mid=7898206&t=9391335
        //https://4pda.to/forum/index.php?act=qms&action=create-thread
        //https://4pda.to/forum/index.php?act=qms&action=create-thread&mid=7898206
        //https://4pda.to/forum/index.php?act=qms&settings=blacklist
        //https://4pda.to/forum/index.php?act=qms&search=неофициальный
        sealed interface Qms : Board {

            @Parcelize
            data object Contacts : Qms

            @Parcelize
            data class Threads(val userId: UserId) : Qms

            @Parcelize
            data class Chat(val chatId: QmsChatId) : Qms

            @Parcelize
            data class CreateThread(val userId: UserId?) : Qms

            @Parcelize
            data object BlackList : Qms

        }

        //https://4pda.to/forum/index.php?act=rep&view=history&mid=7898206&mode=to&order=asc
        //https://4pda.to/forum/index.php?act=rep&view=history&mid=7898206&mode=from&order=desc
        //https://4pda.to/forum/index.php?act=rep&order=asc&st=5000
        //https://4pda.to/forum/index.php?act=rep&view=rating&order=asc
        @Parcelize
        sealed interface Reputation : Board {

            @Parcelize
            data class History(val userId: UserId, val mode: Mode, val order: Order, val offset: PageOffset) : Reputation {

                @Parcelize
                enum class Mode : Parcelable {
                    From,
                    To
                }
            }

            @Parcelize
            data class Rating(val order: Order, val offset: PageOffset) : Reputation

            @Parcelize
            enum class Order : Parcelable {
                Asc,
                Desc
            }
        }

        //https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=Spoil-115499851-1 - копирование спойлера
        //https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=entry115493099 - копирование спойлера
        //https://4pda.to/forum/index.php?act=findpost&pid=115493099 - упоминание например
        //https://4pda.to/forum/index.php?showtopic=1045802&anchor=entry115493099#Spoil-115499851-1
        //https://4pda.to/forum/index.php?showtopic=1045802#entry115493099 - после findpost&pid=
        //https://4pda.to/forum/index.php?showtopic=1045802#Spoil-115499851-1 - после findpost&pid=
        //https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=114328645 - копирование ссылки на пост
        //https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=115420723&anchor=Spoil-115420723-1 - копирование ссылки на пост
        //https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=115420723#Spoil-115420723-1 - копирование ссылки на пост
        //https://4pda.to/forum/index.php?showtopic=1045802&mode=show&st=0 - показать результаты опроса
        //https://4pda.to/forum/index.php?showtopic=208182&view=getnewpost
        //https://4pda.to/forum/index.php?showtopic=208182&view=getlastpost#Spoil-115499851-1
        sealed interface Topic : Board {

            @Parcelize
            data class FindPost(
                val postId: PostId,
                val anchor: Anchor?
            ) : Topic

            sealed interface ShowTopic : Topic {

                val topicId: TopicId

                @Parcelize
                data class Page(
                    override val topicId: TopicId,
                    val showPollResults: Boolean,
                    val offset: PageOffset,
                    val anchor: Anchor?
                ) : ShowTopic

                @Parcelize
                data class FindPost(
                    override val topicId: TopicId,
                    val postId: PostId,
                    val anchor: Anchor?
                ) : ShowTopic

                @Parcelize
                data class GetNewPost(
                    override val topicId: TopicId,
                ) : ShowTopic

                @Parcelize
                data class GetLastPost(
                    override val topicId: TopicId,
                ) : ShowTopic
            }

            sealed interface Anchor : Parcelable {

                val value: String

                @Parcelize
                data class Post(val postId: PostId, override val value: String) : Anchor

                @Parcelize
                data class Node(val name: String, val postId: PostId, val number: Int, override val value: String) : Anchor
            }
        }

        //https://4pda.to/forum/index.php?forums=285&topics=1026049&act=search&source=pst&query=kino
        //https://4pda.to/forum/index.php?act=search&query=kino&username=&forums%5B%5D=285&topics=1026049&source=pst&sort=rel&result=posts
        @Parcelize
        data class Search(
            val query: String,
            val nick: String,
            val forums: Set<Forum>,
            val subforums: Boolean,
            val topics: Set<TopicId>,
            val source: Source,
            val sort: Sort,
            val result: Result,
            val offset: PageOffset
        ) : Board {

            enum class Result {
                Topics,
                Posts
            }

            enum class Sort {
                Relevancy,
                DateAsc,
                DateDesc
            }

            enum class Source {
                All,
                Title,
                Post
            }

            sealed interface Forum : Parcelable {

                @Parcelize
                data object All : Forum

                @Parcelize
                data class Id(val forumId: ForumId) : Forum
            }
        }
    }

    sealed interface Other : Link {

        @Parcelize
        data class ExternalLink(val url: String, val e: String?) : Other
    }
}