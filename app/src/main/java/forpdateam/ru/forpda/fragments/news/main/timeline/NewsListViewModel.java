//package forpdateam.ru.forpda.fragments.news.main.timeline;
//
//import android.arch.lifecycle.LiveData;
//import android.arch.lifecycle.MutableLiveData;
//import android.arch.lifecycle.ViewModel;
//import android.util.Log;
//
//import java.util.List;
//
//import forpdateam.ru.forpda.data.news.entity.News;
//import forpdateam.ru.forpda.data.news.NewsRepository;
//import forpdateam.ru.forpda.data.Request;
//import forpdateam.ru.forpda.data.Resource;
//import forpdateam.ru.forpda.data.news.local.NewsDao;
//
//public class NewsListViewModel extends ViewModel {
//
////    private MutableLiveData<Resource> response = new MutableLiveData<>();
////    private final NewsDao newsDao;
//////    private NewsRepository repository;
////
////    public NewsListViewModel(NewsDao dao) {
//////        repository = NewsRepository.getInstance();
////        this.newsDao = dao;
////        response = new MutableLiveData<>();
////    }
////
////    public LiveData<Resource<List<News>>> loadList(Request request) {
////        return repository.loadList(request, newsDao);
////    }
////
////    public void setDataToParent(Resource resource) {
////        response.setValue(resource);
////    }
////
////    public LiveData<Resource> getForParent() {
////        return response;
////    }
////
////    public void refresh(Request request) {
////        repository.updateList(request, newsDao);
////    }
//
//}
