package forpdateam.ru.forpda.fragments.theme.editpost;

import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;

/**
 * Created by radiationx on 28.07.17.
 */


public class PollChoicesAdapter extends RecyclerView.Adapter<PollChoicesAdapter.ViewHolder> {
    private List<EditPoll.Choice> choices = new ArrayList<>();
    private EditPoll poll;
    private EditPoll.Question question;

    public PollChoicesAdapter(EditPoll.Question question, EditPoll poll) {
        this.choices = question.getChoices();
        this.poll = poll;
        this.question = question;
    }

    public PollChoicesAdapter() {
    }

    public void add(EditPoll.Choice choice) {
        if (this.choices.size() < poll.getMaxChoices()) {
            question.increaseIndexOffset();
            choice.setIndex(question.getIndexOffset() + question.getBaseIndexOffset());
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
        return new ViewHolder(v, new MyCustomEditTextListener());
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EditPoll.Choice item = getItem(holder.getAdapterPosition());
        assert item != null;

        holder.myCustomEditTextListener.updatePosition(holder.getAdapterPosition());
        holder.title.getEditText().setText(item.getTitle());
        holder.title.setHint("Ответ " + (holder.getAdapterPosition() + 1));


    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextInputLayout title;
        public ImageButton delete;
        public MyCustomEditTextListener myCustomEditTextListener;

        public ViewHolder(View v, MyCustomEditTextListener myCustomEditTextListener) {
            super(v);
            title = (TextInputLayout) v.findViewById(R.id.poll_choice_title);
            delete = (ImageButton) v.findViewById(R.id.poll_choice_delete);

            this.myCustomEditTextListener = myCustomEditTextListener;
            this.title.getEditText().addTextChangedListener(myCustomEditTextListener);
            delete.setOnClickListener(v1 -> {
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Удалить ответ?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            EditPoll.Choice choice = choices.get(getLayoutPosition());
                            //notifyItemRemoved(getLayoutPosition());
                            if (choice.getIndex() > question.getBaseIndexOffset()) {
                                int start = choice.getIndex();
                                int end = question.getBaseIndexOffset() + question.getIndexOffset();
                                for (int i = start; i <= end; i++) {
                                    EditPoll.Choice c = EditPoll.findChoiceByIndex(question, i);
                                    if (c != null) {
                                        c.setIndex(c.getIndex() - 1);
                                    }
                                }
                                question.reduceIndexOffset();
                            }
                            choices.remove(getLayoutPosition());
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Нет", null)
                        .show();

            });
        }
    }

    private class MyCustomEditTextListener extends SimpleTextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            choices.get(position).setTitle(charSequence.toString());
        }
    }
}
