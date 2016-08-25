package forpdateam.ru.forpda.fragments.qms.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsChatItem;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<QmsChatItem> chatItems = new ArrayList<>();
    private final static int TYPE_DATE = 0, TYPE_MESSAGE = 1, TYPE_MY_MESSAGE = 2;

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView time;

        public MessageViewHolder(View v) {
            super(v);
            messageText = (TextView) v.findViewById(R.id.message_wrap);
            time = (TextView) v.findViewById(R.id.time);
        }

    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        public TextView date;

        public DateViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.qmsdate);
        }

    }

    public QmsChatAdapter(ArrayList<QmsChatItem> chatItems) {
        this.chatItems = chatItems;
    }

    @Override
    public int getItemViewType(int position) {
        return chatItems.get(position).isDate() ? TYPE_DATE : chatItems.get(position).getWhoseMessage() ? TYPE_MY_MESSAGE : TYPE_MESSAGE;
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
        QmsChatItem item = chatItems.get(position);
        holder.date.setText(item.getDate());
    }

    private void bindMessageHolder(MessageViewHolder holder, int position) {
        QmsChatItem item = chatItems.get(position);
        holder.messageText.setText(item.getContent());
        holder.time.setText(item.getTime());
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    public QmsChatItem getItem(int position) {
        return chatItems.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}