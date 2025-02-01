package forpdateam.ru.forpda.common.webview.jsinterfaces;

import forpdateam.ru.forpda.entity.remote.ForumPost;

/**
 * Created by radiationx on 27.04.17.
 */

public interface IPostFunctions {
    String JS_POSTS_FUNCTIONS = "IPostFunctions";

    void showUserMenu(ForumPost post);

    void showReputationMenu(ForumPost post);

    void showPostMenu(ForumPost post);

    void reportPost(ForumPost post);

    void reply(ForumPost post);

    void quotePost(String text, ForumPost post);

    void deletePost(ForumPost post);

    void editPost(ForumPost post);

    void votePost(ForumPost post, boolean type);

    void changeReputation(ForumPost post, boolean type);
}
