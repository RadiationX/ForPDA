package forpdateam.ru.forpda.entity.remote.editpost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 28.07.17.
 */

public class EditPoll {
    private String title = "";
    private int maxQuestions = 0;
    private int maxChoices = 0;
    private int baseIndexOffset = 0;
    private int indexOffset = 0;
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

    public int getBaseIndexOffset() {
        return baseIndexOffset;
    }

    public void setBaseIndexOffset(int baseIndexOffset) {
        this.baseIndexOffset = baseIndexOffset;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public void increaseIndexOffset() {
        this.indexOffset++;
    }

    public void reduceIndexOffset() {
        this.indexOffset--;
    }

    public static Question findQuestionByIndex(EditPoll poll, int index) {
        for (Question q : poll.getQuestions()) {
            if (index == q.getIndex()) {
                return q;
            }
        }
        return null;
    }

    public static Choice findChoiceByIndex(Question question, int index) {
        for (Choice q : question.getChoices()) {
            if (index == q.getIndex()) {
                return q;
            }
        }
        return null;
    }

    public static class Question {
        private String title = "";
        private boolean isMulti = false;
        private int index = 0;
        private int baseIndexOffset = 0;
        private int indexOffset = 0;
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

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getBaseIndexOffset() {
            return baseIndexOffset;
        }

        public void setBaseIndexOffset(int baseIndexOffset) {
            this.baseIndexOffset = baseIndexOffset;
        }

        public int getIndexOffset() {
            return indexOffset;
        }

        public void increaseIndexOffset() {
            this.indexOffset++;
        }

        public void reduceIndexOffset() {
            this.indexOffset--;
        }
    }

    public static class Choice {
        private String title = "";
        private int votes = 0;
        private int index = 0;

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

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
