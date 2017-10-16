package forpdateam.ru.forpda.rxapi.apiclasses;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.api.forum.models.ForumRules;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ForumRx {
    public Observable<ForumItemTree> getForums() {
        return Observable.fromCallable(() -> Api.Forum().getForums());
    }

    public Observable<Object> markAllRead() {
        return Observable.fromCallable(() -> Api.Forum().markAllRead());
    }

    public Observable<Object> markRead(int id) {
        return Observable.fromCallable(() -> Api.Forum().markRead(id));
    }

    public Observable<ForumRules> getRules(boolean withHtml) {
        return Observable.fromCallable(() -> transform(Api.Forum().getRules(), withHtml));
    }

    public static ForumRules transform(ForumRules rules, boolean withHtml) throws Exception {
        if (withHtml) {
            MiniTemplator t = App.get().getTemplate(App.TEMPLATE_FORUM_RULES);
            App.setTemplateResStrings(t);
            t.setVariableOpt("style_type", App.get().getCssStyleType());

            for (ForumRules.Item item : rules.getItems()) {
                t.setVariable("type", item.isHeader() ? "header" : "");
                t.setVariable("number", item.getNumber());
                t.setVariable("text", item.getText());
                t.addBlock("rules_item");
            }

            rules.setHtml(t.generateOutput());
            t.reset();
        }
        return rules;
    }
}
