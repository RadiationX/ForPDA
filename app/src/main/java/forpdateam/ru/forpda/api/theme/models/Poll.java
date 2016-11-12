package forpdateam.ru.forpda.api.theme.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 12.11.16.
 */

public class Poll {
    private String title;
    private int votesCount;
    //true - result poll
    private boolean result;
    private boolean voteButton = false, showResultsButton = false, showPollButton = false;
    private List<PollQuestion> questions = new ArrayList<>();

    public boolean haveShowPollButton() {
        return showPollButton;
    }

    public void setShowPollButton() {
        showPollButton = true;
    }

    public boolean haveShowResultsButton() {
        return showResultsButton;
    }

    public void setShowResultButton() {
        showResultsButton = true;
    }

    public boolean haveVoteButton() {
        return voteButton;
    }

    public void setVoteButton() {
        voteButton = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    public List<PollQuestion> getQuestions() {
        return questions;
    }

    public void addQuestion(PollQuestion question) {
        questions.add(question);
    }

    public boolean isResult() {
        return result;
    }

    public void setIsResult(boolean result) {
        this.result = result;
    }

    public boolean haveButtons() {
        return voteButton | showResultsButton | showPollButton;
    }
}
