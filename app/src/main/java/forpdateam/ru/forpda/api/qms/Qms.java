package forpdateam.ru.forpda.api.qms;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThread;
import forpdateam.ru.forpda.client.Client;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by radiationx on 29.07.16.
 */
public class Qms {
    private final static Pattern contactsPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-member-id=\"([^\"]*?)\" (?=data-unread-count=\"([^\"]*?)\"|)[^>]*?>[^<]*?<div[^>]*?>[^<]*?<i[^>]*?></i>[^<]*?</div>[^<]*?<span[^>]*?>[^<]*?<div[^>]*?><img[^>]*?src=\"([^\"]*?)\" title=\"([^\"]*?)\"");
    private final static Pattern threadPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-thread-id=\"([^\"]*?)\"[\\s\\S]*?<div[^>]*?>([^<]*?)</div>[^<]*?([\\s\\S]*?)</a>");
    private final static Pattern threadName = Pattern.compile("([\\s\\S]*?) \\((\\d+)(?= / (\\d+)|)");

    private ArrayList<QmsContact> contactsList(final String url) throws Exception {
        ArrayList<QmsContact> list = new ArrayList<>();
        final String response = Client.getInstance().get(url);
        final Matcher matcher = contactsPattern.matcher(response);
        QmsContact contact = null;
        while (matcher.find()) {
            contact = new QmsContact();
            contact.setId(matcher.group(1));
            contact.setCount(matcher.group(2) == null ? "" : matcher.group(2));
            contact.setAvatar(matcher.group(3));
            contact.setNick(matcher.group(4));
            list.add(contact);
        }

        return list;
    }

    private ArrayList<QmsThread> threadList(final String url) throws Exception {
        ArrayList<QmsThread> list = new ArrayList<>();
        final String response = Client.getInstance().get(url);
        final Matcher matcher = threadPattern.matcher(response);
        QmsThread thread = null;
        while (matcher.find()) {
            thread = new QmsThread();
            thread.setId(matcher.group(1));
            thread.setDate(matcher.group(2));
            Matcher nameMatcher = threadName.matcher(Html.fromHtml(matcher.group(3).trim()));
            if (nameMatcher.find()) {
                thread.setName(nameMatcher.group(1));
                thread.setCountMessages(nameMatcher.group(2));
                thread.setCountNew(nameMatcher.group(3) == null ? "" : nameMatcher.group(3));
            }
            list.add(thread);
        }
        return list;
    }

    public Observable<ArrayList<QmsContact>> getContactList(final String url) {
        return Observable.create(new Observable.OnSubscribe<ArrayList<QmsContact>>() {
            @Override
            public void call(Subscriber<? super ArrayList<QmsContact>> subscriber) {
                try {
                    subscriber.onNext(contactsList(url));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<ArrayList<QmsThread>> getThreadList(final String url) {
        return Observable.create(new Observable.OnSubscribe<ArrayList<QmsThread>>() {
            @Override
            public void call(Subscriber<? super ArrayList<QmsThread>> subscriber) {
                try {
                    subscriber.onNext(threadList(url));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
