package forpdateam.ru.forpda.common.webview.jsinterfaces;

import forpdateam.ru.forpda.entity.remote.BaseForumPost;

/**
 * Created by radiationx on 27.04.17.
 */

public interface IPostFunctions {
    String JS_POSTS_FUNCTIONS = "IPostFunctions";

    void showUserMenu(BaseForumPost post);

    void showReputationMenu(BaseForumPost post);

    void showPostMenu(BaseForumPost post);

    void reportPost(BaseForumPost post);

    void reply(BaseForumPost post);

    void quotePost(String text, BaseForumPost post);

    void deletePost(BaseForumPost post);

    void editPost(BaseForumPost post);

    void votePost(BaseForumPost post, boolean type);

    void changeReputation(BaseForumPost post, boolean type);
}
