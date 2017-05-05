package forpdateam.ru.forpda.fragments.qms.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
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
 * Created by radiationx on 25.08.16.
 */
public class QmsChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<QmsMessage> chatItems;
    private final static int TYPE_DATE = 0, TYPE_MESSAGE = 1, TYPE_MY_MESSAGE = 2;
    private Context context;
    private QmsChatAdapter.OnItemClickListener itemClickListener;
    private QmsChatAdapter.OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(QmsMessage message);
    }

    public void setOnItemClickListener(final QmsChatAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final QmsChatAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView time;
        public LinearLayout root;
        public View dot;

        public MessageViewHolder(View v) {
            super(v);
            time = (TextView) v.findViewById(R.id.chat_item_time);
            root = (LinearLayout) v.findViewById(R.id.chat_item_wrapper);
            dot = v.findViewById(R.id.chat_item_dot);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                longItemClickListener.onItemClick(getItem(getLayoutPosition()));
                return true;
            }
            return false;
        }
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        public TextView date;

        public DateViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.qmsdate);
        }

    }

    public QmsChatAdapter(List<QmsMessage> chatItems, Context context) {
        this.chatItems = chatItems;
        this.context = context;
    }

    public void addMessage(QmsMessage message) {
        chatItems.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return chatItems.get(position).isDate() ? TYPE_DATE : chatItems.get(position).isMyMessage() ? TYPE_MY_MESSAGE : TYPE_MESSAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE)
            return new DateViewHolder(inflater.inflate(R.layout.qms_chat_date, parent, false));

        return new MessageViewHolder(inflater.inflate(viewType == TYPE_MY_MESSAGE ? R.layout.qms_chat_my_message : R.layout.qms_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_DATE) {
            bindDateHolder((DateViewHolder) holder, position);
            return;
        }
        bindMessageHolder((MessageViewHolder) holder, position);
    }

    private void bindDateHolder(DateViewHolder holder, int position) {
        QmsMessage item = chatItems.get(position);
        holder.date.setText(item.getDate());
    }

    private void bindMessageHolder(MessageViewHolder holder, int position) {
        QmsMessage item = chatItems.get(position);
        BaseTag view;
        if (!createdTrees.containsKey(position)) {
            Document document = Document.parse(chatItems.get(position).getContent());
            view = recurseUi(document.getRoot());
            createdTrees.put(position, view);
        } else {
            view = createdTrees.get(position);
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }
        holder.root.removeAllViews();
        holder.root.addView(view);
        holder.time.setText(item.getTime());
        holder.dot.setVisibility(item.getReadStatus() ? View.GONE : View.VISIBLE);
    }

    private HashMap<Integer, BaseTag> createdTrees = new HashMap<>();

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    public QmsMessage getItem(int position) {
        return chatItems.get(position);
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
                element.getLast().fixSpace();
            } else if (elClassios.contains("spoil")) {
                thisView = new SpoilerPostBlock(getContext());
            } else {
                thisView = new PostBlock(getContext());
            }
            if (!element.get(0).htmlNoParent().trim().equals(""))
                ((PostBlock) thisView).setTitle(Html.fromHtml(element.get(0).htmlNoParent().trim(), Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            else
                ((PostBlock) thisView).hideTitle();
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