package forpdateam.ru.forpda.api.theme.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 12.11.16.
 */

public class PollQuestion {
    private String title;
    private List<PollQuestionItem> questionItems = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PollQuestionItem> getQuestionItems() {
        return questionItems;
    }

    public void addItem(PollQuestionItem questionItem) {
        questionItems.add(questionItem);
    }
}
