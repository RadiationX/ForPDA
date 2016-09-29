package forpdateam.ru.forpda.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.jsoup.Jsoup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.utils.ourparser.Document;
import forpdateam.ru.forpda.utils.ourparser.Element;
import forpdateam.ru.forpda.utils.ourparser.Html;
import forpdateam.ru.forpda.utils.ourparser.htmltags.BaseTag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.CodePostBlock;
import forpdateam.ru.forpda.utils.ourparser.htmltags.H1Tag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.H2Tag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.LiTag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.PostBlock;
import forpdateam.ru.forpda.utils.ourparser.htmltags.QuotePostBlock;
import forpdateam.ru.forpda.utils.ourparser.htmltags.SpoilerPostBlock;
import forpdateam.ru.forpda.utils.ourparser.htmltags.UlTag;

/**
 * Created by radiationx on 27.09.16.
 */

public class ThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ThemePost> postList;
    private Context context;

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView nick;
        public TextView group;
        public TextView date;
        public LinearLayout root;

        public MessageViewHolder(View v) {
            super(v);
            avatar = (ImageView) v.findViewById(R.id.avatar);
            nick = (TextView) v.findViewById(R.id.nick);
            group = (TextView) v.findViewById(R.id.group);
            date = (TextView) v.findViewById(R.id.date);
            root = (LinearLayout) v.findViewById(R.id.content);
        }

    }

    public ThemeAdapter(List<ThemePost> chatItems, Context context) {
        this.postList = chatItems;
        this.context = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ThemeAdapter.MessageViewHolder(inflater.inflate(R.layout.theme_post_test, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindMessageHolder((ThemeAdapter.MessageViewHolder) holder, position);
    }


    private void bindMessageHolder(ThemeAdapter.MessageViewHolder holder, int position) {
        ThemePost item = postList.get(position);
        /*long time = System.currentTimeMillis();
        Document document = Document.parse(postList.get(position).getBody());
        Log.d("kek", "theme parsing time document " + (System.currentTimeMillis() - time));*/
        BaseTag view;
        if (!createdTrees.containsKey(position)) {
            long time = System.currentTimeMillis();
            Document document = Document.parse(postList.get(position).getBody());
            Log.d("kek", "theme parsing time document " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            view = recurseUi(document.getRoot());
            Log.d("kek", "theme parsing time layouts " + (System.currentTimeMillis() - time));
            createdTrees.put(position, view);
        } else {
            view = createdTrees.get(position);
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }
        holder.root.removeAllViews();
        holder.root.addView(view);
        holder.nick.setText(item.getUserName());
        holder.group.setText(item.getGroup());
        holder.date.setText(item.getDate());
        ImageLoader.getInstance().displayImage("http://s.4pda.to/forum/uploads/" + item.getUserAvatar(), holder.avatar);
    }

    private HashMap<Integer, BaseTag> createdTrees = new HashMap<>();

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public ThemePost getItem(int position) {
        return postList.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private final static Pattern p2 = Pattern.compile("^(b|i|u|del|s|strike|sub|sup|span|a|br)$");

    private BaseTag recurseUi(final Element element) {
        BaseTag thisView = null;
        String elClassios = element.attr("class");
        if (elClassios != null && elClassios.contains("post-block")) {
            if (elClassios.contains("quote")) {
                thisView = new QuotePostBlock(getContext());
                if (element.get(0).getSize() > 0) {
                    for (int i = 0; i < element.get(0).getSize(); i++) {
                        if (element.get(0).get(i).attr("title").contains("Перейти")) {
                            String url = element.get(0).get(i).attr("href");
                            ((QuotePostBlock) thisView).setTitleOnClickListener(v -> Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show());
                            ((QuotePostBlock) thisView).addQuoteArrow();
                            element.get(0).getElements().remove(i);
                            break;
                        }
                    }
                }
            } else if (elClassios.contains("code")) {
                thisView = new CodePostBlock(getContext());
                Element.fixSpace(element.getLast());
            } else if (elClassios.contains("spoil")) {
                thisView = new SpoilerPostBlock(getContext());
            } else {
                thisView = new PostBlock(getContext());
            }
            if (!element.get(0).htmlNoParent().trim().equals(""))
                ((PostBlock) thisView).setTitle(Html.fromHtml(element.get(0).htmlNoParent().trim(), Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            /*else
                ((PostBlock) thisView).hideTitle();*/
            ((PostBlock) thisView).addBody(recurseUi(element.getLast()));
            return thisView;
        } else {
            thisView = getViewByTag(element.tagName());
        }
        /*if (element.tagName().equals("img")) {
            thisView.setImage("http://beardycast.com/".concat(element.attr("src")));
            thisView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), element.attr("src"), Toast.LENGTH_SHORT).show();
                }
            });
            if (element.attr("alt") != null) {
                TextView textView = thisView.setHtmlText(element.attr("alt"));
                thisView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setTextSize(12);
            }

            return thisView;
        }*/

        String html = element.getText();

        boolean text = true;

        for (int i = 0; i < element.getElements().size(); i++) {
            Element child = element.get(i);
            BaseTag newView = null;
            if (p2.matcher(child.tagName()).matches()) {
                html = html.concat(child.html());
                text = true;
                continue;
            } else {
                newView = recurseUi(child);
                if (text) {
                    html = startBreakTag.matcher(html).replaceFirst("");
                    html = endBreakTag.matcher(html).replaceFirst("");
                    html = html.trim();
                    if (!html.isEmpty()) {
                        thisView.setHtmlText(html);
                        html = "";
                    }
                }
                html = "";
                text = false;
            }
            if (newView != null)
                thisView.addView(newView);
        }
        html = html.trim();
        if (!html.isEmpty()) {
            html = startBreakTag.matcher(html).replaceFirst("");
            html = endBreakTag.matcher(html).replaceFirst("");
            html = html.trim();
            html = html.concat(element.getAfterText());
            thisView.setHtmlText(html);
            html = "";
        }
        return thisView;
    }

    private final static Pattern startBreakTag = Pattern.compile("^([ ]*|)<br>");
    private final static Pattern endBreakTag = Pattern.compile("<br>([ ]*|)$");

    private BaseTag getViewByTag(String tag) {
        switch (tag) {
            case "h1":
                return new H1Tag(getContext());
            case "h2":
                return new H2Tag(getContext());
            case "ul":
                return new UlTag(getContext());
            case "li":
                return new LiTag(getContext());
            default:
                return new BaseTag(getContext());
        }
    }

    public Context getContext() {
        return context;
    }
}
