package forpdateam.ru.forpda.model.data.remote

object ParserPatterns {

    object Global {
        const val scope = "global"
        const val meta_tags = "meta_tags"
    }

    object Auth {
        const val scope = "auth";
        const val captcha = "captcha";
        const val check_login = "check_login";
        const val errors_list = "errors_list";
    }

    object DevDb {
        const val scope = "devdb";
        const val main_root = "main_root";
        const val main_breadcrumb = "main_breadcrumb";
        const val main_specs = "main_specs";
        const val main_search = "main_search";
        const val brand_devices = "brand_devices";
        const val brands_letters = "brands_letters";
        const val brands_items_in_letter = "brands_items_in_letter";
        const val device_head = "device_head";
        const val device_images = "device_images";
        const val device_specs_titled = "device_specs_titled";
        const val device_reviews = "device_reviews";
        const val device_discuss_and_firm = "device_discuss_and_firm";
        const val device_discussions = "device_discussions";
        const val device_firmwares = "device_firmwares";
        const val device_comments = "device_comments";
    }

    object EditPost {
        const val scope = "editpost";
        const val form = "form";
        const val poll_info = "poll_info";
        const val poll_fucking_invalid_json = "poll_fucking_invalid_json";
        const val attachments = "attachments";
    }

    object Favorites {
        const val scope = "favorites";
        const val main = "main";
        const val check_action = "check_action";
    }

    object Forum {
        const val scope = "forum";
        const val rules_headers = "rules_headers";
        const val rules_items = "rules_items";
        const val announce = "announce";
        const val forums_from_search = "forums_from_search";
        const val forum_item_from_search = "forum_item_from_search";
    }

    object Mentions {
        const val scope = "mentions";
        const val main = "main";
    }

    object Articles {
        const val scope = "articles";
        const val list = "list";
        const val detail = "detail";
        const val detail_v2 = "detail_v2";
        const val detail_detector = "detail_detector";
        const val exclude_form_comment = "exclude_form_comment";
        const val tags = "tags";
        const val materials = "materials";
        const val karma = "karma";
        const val karmaSource = "karmaSource";
        const val comment_id = "comment_id";
        const val comment_user_id = "comment_user_id";
    }

    object Profile {
        const val scope = "profile";
        const val main = "main";
        const val info = "info";
        const val personal = "personal";
        const val contacts = "contacts";
        const val devices = "devices";
        const val site_stats = "site_stats";
        const val forum_stats = "forum_stats";
        const val note = "note";
        const val about = "about";
        const val warnings = "warnings";
    }

    object Qms {
        const val scope = "qms";
        const val contacts_main = "contacts_main";
        const val thread_main = "thread_main";
        const val thread_nick = "thread_nick";
        const val chat_info = "chat_info";
        const val chat_pattern = "chat_pattern";
        const val blacklist_main = "blacklist_main";
        const val blacklist_msg = "blacklist_msg";
        const val finduser = "finduser";
        const val send_message_error = "send_message_error";
        const val message_info = "message_info";
    }

    object Reputation {
        const val scope = "reputation";
        const val main = "main";
        const val info = "info";
    }

    object Search {
        const val scope = "search";
        const val articles = "articles";
        const val forum_topics = "forum_topics";
        const val forum_posts = "forum_posts";
    }

    object Topic {
        const val scope = "topic";
        const val posts = "posts";
        const val title = "title";
        const val already_in_fav = "already_in_fav";
        const val fav_id = "fav_id";
        const val topic_id = "topic_id";
        const val scroll_anchor = "scroll_anchor";
        const val poll_main = "poll_main";
        const val poll_questions = "poll_questions";
        const val poll_question_item = "poll_question_item";
        const val poll_buttons = "poll_buttons";
        const val attached_images = "attached_images";
    }

    object Topics {
        const val scope = "topics";
        const val title = "title";
        const val can_new_topic = "can_new_topic";
        const val announce = "announce";
        const val forum = "forum";
        const val topics = "topics";
    }

}