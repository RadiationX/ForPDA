package forpdateam.ru.forpda.ui.fragments.theme.editpost;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;

/**
 * Created by radiationx on 28.07.17.
 */

public class PollQuestionsAdapter extends RecyclerView.Adapter<PollQuestionsAdapter.ViewHolder> {
    private List<EditPoll.Question> questions = new ArrayList<>();
    private EditPoll poll;
    private HashMap<EditPoll.Question, PollChoicesAdapter> choiceAdapters = new HashMap<>();

    public PollQuestionsAdapter(List<EditPoll.Question> questions, EditPoll poll) {
        this.questions = questions;
        this.poll = poll;
    }

    public PollQuestionsAdapter() {
    }

    public void add(EditPoll.Question question) {
        if (questions.size() < poll.getMaxQuestions()) {
            poll.increaseIndexOffset();
            question.setIndex(poll.getIndexOffset() + poll.getBaseIndexOffset());
            this.questions.add(question);
            //notifyItemInserted(questions.indexOf(question));
            notifyDataSetChanged();
        } else {
            Toast.makeText(App.getContext(), String.format(App.get().getString(R.string.poll_questions_Max), poll.getMaxQuestions()), Toast.LENGTH_SHORT).show();
        }
    }

    public EditPoll.Question getItem(int position) {
        return questions.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_poll_question, parent, false);
        return new ViewHolder(v, new CustomTextWatcher(), new CustomCheckedChangeListener());
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EditPoll.Question item = getItem(holder.getAdapterPosition());
        assert item != null;

        String qstr = String.format(App.get().getString(R.string.poll_question_Pos), (holder.getAdapterPosition() + 1));
        holder.customTextWatcher.updatePosition(holder.getAdapterPosition());
        holder.checkedChangeListener.updatePosition(holder.getAdapterPosition());

        holder.title.setText(qstr);
        holder.titleField.setText(item.getTitle());
        holder.titleField.setHint(qstr);

        holder.multi.setChecked(item.isMulti());

        PollChoicesAdapter choicesAdapter = choiceAdapters.get(item);

        if (choicesAdapter == null) {
            choicesAdapter = new PollChoicesAdapter(item, poll);
            choiceAdapters.put(item, choicesAdapter);
        }


        holder.choices.setAdapter(choicesAdapter);


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView title;
        public AppCompatEditText titleField;
        public AppCompatCheckBox multi;
        public RecyclerView choices;
        public Button addChoice;
        public ImageButton delete;
        public CustomTextWatcher customTextWatcher;
        public CustomCheckedChangeListener checkedChangeListener;

        public ViewHolder(View v, CustomTextWatcher customTextWatcher, CustomCheckedChangeListener checkedChangeListener) {
            super(v);
            title = (AppCompatTextView) v.findViewById(R.id.poll_question_title);
            titleField = (AppCompatEditText) v.findViewById(R.id.poll_question_title_field);
            multi = (AppCompatCheckBox) v.findViewById(R.id.poll_question_multi);
            choices = (RecyclerView) v.findViewById(R.id.poll_question_choices);
            addChoice = (Button) v.findViewById(R.id.poll_add_choice);
            delete = (ImageButton) v.findViewById(R.id.poll_question_delete);

            this.customTextWatcher = customTextWatcher;
            this.titleField.addTextChangedListener(customTextWatcher);

            this.checkedChangeListener = checkedChangeListener;
            this.multi.setOnCheckedChangeListener(checkedChangeListener);

            choices.setLayoutManager(new LinearLayoutManager(choices.getContext()));

            addChoice.setOnClickListener(v1 -> {
                PollChoicesAdapter choicesAdapter = choiceAdapters.get(questions.get(getLayoutPosition()));
                choicesAdapter.add(new EditPoll.Choice());
            });

            delete.setOnClickListener(v1 -> {
                new AlertDialog.Builder(v.getContext())
                        .setMessage(R.string.ask_delete_question)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            EditPoll.Question question = questions.get(getLayoutPosition());

                            if (question.getIndex() > poll.getBaseIndexOffset()) {
                                int start = question.getIndex();
                                int end = poll.getBaseIndexOffset() + poll.getIndexOffset();
                                for (int i = start; i <= end; i++) {
                                    EditPoll.Question q = EditPoll.findQuestionByIndex(poll, i);
                                    if (q != null) {
                                        q.setIndex(q.getIndex() - 1);
                                    }
                                }
                                poll.reduceIndexOffset();
                            }
                            questions.remove(question);
                            choiceAdapters.remove(question);
                            //notifyItemRemoved(getLayoutPosition());
                            notifyDataSetChanged();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            });
        }
    }

    private class CustomTextWatcher extends SimpleTextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            questions.get(position).setTitle(charSequence.toString());
        }
    }

    private class CustomCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            questions.get(position).setMulti(isChecked);
        }
    }
}
