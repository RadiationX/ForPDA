package forpdateam.ru.forpda.api.theme.editpost.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 28.07.17.
 */

public class EditPoll {
    private String title = "";
    private int maxQuestions = 0;
    private int maxChoices = 0;
    private List<Question> questions = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public int getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Question getQuestion(int index) {
        return questions.get(index);
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public static class Question {
        private String title = "";
        private boolean isMulti = false;
        private List<Choice> choices = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isMulti() {
            return isMulti;
        }

        public void setMulti(boolean multi) {
            isMulti = multi;
        }

        public List<Choice> getChoices() {
            return choices;
        }

        public Choice getChoice(int index) {
            return choices.get(index);
        }

        public void addChoice(Choice choice) {
            this.choices.add(choice);
        }
    }

    public static class Choice {
        private String title = "";
        private int votes = 0;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }
    }
}
