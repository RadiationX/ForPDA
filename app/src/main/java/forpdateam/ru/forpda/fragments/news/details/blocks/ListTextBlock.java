package forpdateam.ru.forpda.fragments.news.details.blocks;

import java.util.ArrayList;

/**
 * Created by isanechek on 8/22/17.
 */

public class ListTextBlock {
    private String title;
    private ArrayList<String> list;

    public ListTextBlock(ArrayList<String> list) {
        this.list = list;
    }

    public ListTextBlock(String title, ArrayList<String> list) {
        this.title = title;
        this.list = list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}
