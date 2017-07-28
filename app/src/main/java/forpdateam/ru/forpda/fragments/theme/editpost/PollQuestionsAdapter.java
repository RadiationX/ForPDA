package forpdateam.ru.forpda.fragments.theme.editpost;

import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;

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
            this.questions.add(question);
            //notifyItemInserted(questions.indexOf(question));
            notifyDataSetChanged();
        } else {
            Toast.makeText(App.getContext(), "Максимальное кол-во вопросов: " + poll.getMaxQuestions(), Toast.LENGTH_SHORT).show();
        }
    }

    public EditPoll.Question getItem(int position) {
        return questions.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_poll_question, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EditPoll.Question item = getItem(position);
        assert item != null;

        holder.title.setText(item.getTitle());
        holder.title.setHint("Вопрос " + (position + 1));
        holder.titleField.setText("Вопрос " + (position + 1));
        holder.multi.setChecked(item.isMulti());

        PollChoicesAdapter choicesAdapter = choiceAdapters.get(item);

        Log.d("POLL", "ADAPTER Q 1: " + choicesAdapter + " : " + position);
        if (choicesAdapter == null) {
            choicesAdapter = new PollChoicesAdapter(item.getChoices(), poll);
            choiceAdapters.put(item, choicesAdapter);
        }
        Log.d("POLL", "ADAPTER Q 2: " + choicesAdapter + " : " + position + " : " + choicesAdapter.getItemCount());


        holder.choices.setAdapter(choicesAdapter);


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView title;
        public AppCompatEditText titleField;
        public AppCompatCheckBox multi;
        public RecyclerView choices;
        public Button addChoice;
        public ImageButton delete;

        public ViewHolder(View v) {
            super(v);
            title = (AppCompatTextView) v.findViewById(R.id.poll_question_title);
            titleField = (AppCompatEditText) v.findViewById(R.id.poll_question_title_field);
            multi = (AppCompatCheckBox) v.findViewById(R.id.poll_question_multi);
            choices = (RecyclerView) v.findViewById(R.id.poll_question_choices);
            addChoice = (Button) v.findViewById(R.id.poll_add_choice);
            delete = (ImageButton) v.findViewById(R.id.poll_question_delete);

            choices.setLayoutManager(new LinearLayoutManager(choices.getContext()));

            addChoice.setOnClickListener(v1 -> {
                PollChoicesAdapter choicesAdapter = choiceAdapters.get(questions.get(getLayoutPosition()));
                choicesAdapter.add(new EditPoll.Choice());
            });

            delete.setOnClickListener(v1 -> {
                EditPoll.Question question = questions.get(getLayoutPosition());
                questions.remove(question);
                choiceAdapters.remove(question);
                //notifyItemRemoved(getLayoutPosition());
                notifyDataSetChanged();
            });
        }
    }
}
