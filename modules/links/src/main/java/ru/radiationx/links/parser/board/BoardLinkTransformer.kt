package ru.radiationx.links.parser.board

import ru.radiationx.links.Link
import ru.radiationx.links.parser.board.parts.AnnounceLinkTransformer
import ru.radiationx.links.parser.board.parts.AuthLinkTransformer
import ru.radiationx.links.parser.board.parts.FavoriteLinkTransformer
import ru.radiationx.links.parser.board.parts.ForumLinkTransformer
import ru.radiationx.links.parser.board.parts.LoFiLinkTransformer
import ru.radiationx.links.parser.board.parts.MentionsLinkTransformer
import ru.radiationx.links.parser.board.parts.ProfileLinkTransformer
import ru.radiationx.links.parser.board.parts.QmsLinkTransformer
import ru.radiationx.links.parser.board.parts.ReputationLinkTransformer
import ru.radiationx.links.parser.board.parts.RootLinkTransformer
import ru.radiationx.links.parser.board.parts.RulesLinkTransformer
import ru.radiationx.links.parser.board.parts.SearchLinkTransformer
import ru.radiationx.links.parser.board.parts.TopicLinkTransformer
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

internal object BoardLinkTransformer {


    fun build(builder: LinkUrlBuilder, link: Link.Board): LinkUrl {
        builder.segment("forum")
        return when (link) {
            is Link.Board.Root -> RootLinkTransformer.build(builder, link)
            is Link.Board.Announce -> AnnounceLinkTransformer.build(builder, link)
            is Link.Board.Auth -> AuthLinkTransformer.build(builder, link)
            is Link.Board.Favorite -> FavoriteLinkTransformer.build(builder, link)
            is Link.Board.Forum -> ForumLinkTransformer.build(builder, link)
            is Link.Board.Mentions -> MentionsLinkTransformer.build(builder, link)
            is Link.Board.Profile -> ProfileLinkTransformer.build(builder, link)
            is Link.Board.Qms -> QmsLinkTransformer.build(builder, link)
            is Link.Board.Reputation -> ReputationLinkTransformer.build(builder, link)
            is Link.Board.Rules -> RulesLinkTransformer.build(builder, link)
            is Link.Board.Topic -> TopicLinkTransformer.build(builder, link)
            is Link.Board.Search -> SearchLinkTransformer.build(builder, link)
        }
    }

    fun parse(url: LinkUrl): Link.Board? {
        if (url.segment(0) != "forum") return null
        AnnounceLinkTransformer.parse(url)?.also { return it }
        AuthLinkTransformer.parse(url)?.also { return it }
        FavoriteLinkTransformer.parse(url)?.also { return it }
        ForumLinkTransformer.parse(url)?.also { return it }
        MentionsLinkTransformer.parse(url)?.also { return it }
        ProfileLinkTransformer.parse(url)?.also { return it }
        QmsLinkTransformer.parse(url)?.also { return it }
        ReputationLinkTransformer.parse(url)?.also { return it }
        RulesLinkTransformer.parse(url)?.also { return it }
        TopicLinkTransformer.parse(url)?.also { return it }
        SearchLinkTransformer.parse(url)?.also { return it }
        LoFiLinkTransformer.parse(url)?.also { return it }
        RootLinkTransformer.parse(url)?.also { return it }
        return null
    }

}