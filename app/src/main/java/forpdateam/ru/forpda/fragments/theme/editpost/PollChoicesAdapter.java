package forpdateam.ru.forpda.fragments.theme.editpost;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;

/**
 * Created by radiationx on 28.07.17.
 */


public class PollChoicesAdapter extends RecyclerView.Adapter<PollChoicesAdapter.ViewHolder> {
    private List<EditPoll.Choice> choices = new ArrayList<>();
    private EditPoll poll;

    public PollChoicesAdapter(List<EditPoll.Choice> choices, EditPoll poll) {
        this.choices = choices;
        this.poll = poll;
    }

    public PollChoicesAdapter() {
    }

    public void add(EditPoll.Choice choice) {
        if (this.choices.size() < poll.getMaxChoices()) {
            this.choices.add(choice);
            //notifyItemInserted(choices.indexOf(choice));
            notifyDataSetChanged();
        } else {
            Toast.makeText(App.getContext(), "Максимальное кол-во ответов: " + poll.getMaxChoices(), Toast.LENGTH_SHORT).show();
        }
    }

    public EditPoll.Choice getItem(int position) {
        return choices.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_poll_choice, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EditPoll.Choice item = getItem(position);
        assert item != null;

        holder.title.getEditText().setText(item.getTitle());
        holder.title.setHint("Ответ " + (position + 1));

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextInputLayout title;
        public ImageButton delete;

        public ViewHolder(View v) {
            super(v);
            title = (TextInputLayout) v.findViewById(R.id.poll_choice_title);
            delete = (ImageButton) v.findViewById(R.id.poll_choice_delete);
            delete.setOnClickListener(v1 -> {
                choices.remove(getLayoutPosition());
                //notifyItemRemoved(getLayoutPosition());
                notifyDataSetChanged();
            });
        }
    }
}
