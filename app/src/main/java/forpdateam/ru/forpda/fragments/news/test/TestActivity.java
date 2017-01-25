//package forpdateam.ru.forpda.fragments.news.test;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//
//import com.annimon.stream.Stream;
//
//import java.util.List;
//
//import forpdateam.ru.forpda.Constants;
//import forpdateam.ru.forpda.R;
//import forpdateam.ru.forpda.data.Repository;
//import forpdateam.ru.forpda.fragments.news.INewsView;
//import forpdateam.ru.forpda.fragments.news.models.NewsModel;
//import forpdateam.ru.forpda.fragments.news.models.NewsCallbackModel;
//import forpdateam.ru.forpda.fragments.news.presenter.NewsPresenter;
//
//import static forpdateam.ru.forpda.utils.Utils.log;
//
///**
// * Created by isanechek on 12.01.17.
// */
//
//public class TestActivity extends Activity implements INewsView {
//    private static final String TAG = "TestActivity";
//
//    private NewsPresenter presenter;
//    private int s = 1;
//
//    public TestActivity() {
//        this.presenter = new NewsPresenter();
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.test_activity);
//        presenter.bindView(this, Repository.getInstance());
//        presenter.loadData(Constants.NEWS_CATEGORY_ALL, 0, null);
//        log(TAG + " onCreate");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        log(TAG + " onStart");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        log(TAG + " onResume");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        log(TAG + " onPause");
//        presenter.unbindView();
//        Repository.removeInstance();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        log(TAG + " onDestroy");
//    }
//
//    @Override
//    public void showData(List<NewsModel> list) {
//        log(TAG + " showData");
//
//        if (list.size() != 0) {
//            log(TAG + " Data Not Null");
//            Stream.of(list).forEach(model -> log(TAG + " news->> " + model.getTitle()));
//            if (s <= 3) {
//                s++;
//            }
//            log(TAG + " size S -> " + s);
//            presenter.loadData(Constants.NEWS_CATEGORY_ALL, s, null);
//        } else {
//            log(TAG + " Data NULL!!!");
//        }
//    }
//
//    @Override
//    public void showUpdateData(NewsCallbackModel model) {
//        log(TAG + " updateNewsListData");
//        log(TAG + " size -> " + model.getCache().size());
//        log(TAG + " show -> " + model.isShowMore());
//    }
//
//    @Override
//    public void showNewNews(List<NewsModel> list) {
//
//    }
//
//    @Override
//    public void showMoreNewNews(List<NewsModel> list) {
//
//    }
//
//    @Override
//    public void showProgress(boolean show) {
//
//    }
//
//    @Override
//    public void showErrorView() {
//
//    }
//
//    @Override
//    public void showNoNetworkPopUp(boolean show) {
//
//    }
//}
