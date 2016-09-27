package forpdateam.ru.forpda.api.favorites.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavData {
    private List<FavItem> favItems = new ArrayList<>();

    public void addItem(FavItem item) {
        favItems.add(item);
    }

    public List<FavItem> getFavItems() {
        return favItems;
    }
}
