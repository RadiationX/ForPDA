package forpdateam.ru.forpda.data.news.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//@Entity(tableName = News.NEWS_TABLE)
public class News extends RealmObject {

    public static final String NEWS_TABLE = "news";

//    @PrimaryKey(autoGenerate = true)
//    public long id;
    @PrimaryKey
    public String url;
    public String title;
    public String description;
    public String author;
    public String date;
    public String imgUrl;
    public String commentsCount;
    public String tags;

    // for details
    public String body;
//    @Relation(parentColumn = "id", entityColumn = "id", entity = MoreNews.class)
//    public List<MoreNews> moreNews;
    public String moreNewsId;
    public String navId;
//    @Relation(parentColumn = "id", entityColumn = "id", entity = NewsComment.class)
//    public List<NewsComment> comments;
    public String commentsId;
    public long lastUpdate;

    public boolean offline;
    public boolean favorite;
    public boolean readDone;
    public boolean newNews;

    // other
    public String category;
}
