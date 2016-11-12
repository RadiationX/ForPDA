package forpdateam.ru.forpda.api.theme.models;

/**
 * Created by radiationx on 12.11.16.
 */

public class PollQuestionItem {
    private String title;

    //For no result poll
    private int value;
    private String name, type;

    //For result poll
    private int votes;
    private float percent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}
