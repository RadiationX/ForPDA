package forpdateam.ru.forpda;

import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.mentions.Mentions;
import forpdateam.ru.forpda.model.repository.FavoritesRepository;
import forpdateam.ru.forpda.model.repository.HistoryRepository;
import forpdateam.ru.forpda.model.repository.MentionsRepository;
import forpdateam.ru.forpda.model.system.AppSchedulers;
import forpdateam.ru.forpda.model.system.SchedulersProvider;

/**
 * Created by radiationx on 01.01.18.
 */

public class Di {
    private static final Di ourInstance = new Di();

    public static Di get() {
        return ourInstance;
    }

    private Di() {
    }

    public SchedulersProvider schedulers = new AppSchedulers();

    public Favorites favoritesApi = new Favorites();
    public Mentions mentionsApi = new Mentions();

    public FavoritesRepository favoritesRepository = new FavoritesRepository(schedulers, favoritesApi);
    public HistoryRepository historyRepository = new HistoryRepository(schedulers);
    public MentionsRepository mentionsRepository = new MentionsRepository(schedulers, mentionsApi);
}
