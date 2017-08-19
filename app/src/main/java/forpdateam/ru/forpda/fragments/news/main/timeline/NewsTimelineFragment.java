//package forpdateam.ru.forpda.fragments.news.main.timeline;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.View;
//import android.widget.ProgressBar;
//
//import forpdateam.ru.forpda.R;
//import forpdateam.ru.forpda.TabManager;
//import forpdateam.ru.forpda.api.news.Constants;
//import forpdateam.ru.forpda.data.news.entity.News;
//import forpdateam.ru.forpda.fragments.TabFragment;
//import forpdateam.ru.forpda.fragments.news.BaseLifecycleFragment;
//import forpdateam.ru.forpda.fragments.news.details.NewsDetailsParentFragment;
//
///**
// * Created by isanechek on 8/8/17.
// */
//
//public class NewsTimelineFragment extends BaseLifecycleFragment implements
//        NewsListAdapter.ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
//
//    private ProgressBar progress;
//    private SwipeRefreshLayout refresh;
//    private RecyclerView list;
////    private NewsListViewModel viewModel;
//    private NewsListAdapter adapter;
//    private static final String TAG = NewsTimelineFragment.class.getSimpleName();
//
//    public NewsTimelineFragment() {
//
//    }
//
//    public static NewsTimelineFragment createInstance() {
//        return new NewsTimelineFragment();
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.news_list_fragment;
//    }
//
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        progress = (ProgressBar) view.findViewById(R.id.news_list_progress);
//        refresh = (SwipeRefreshLayout) view.findViewById(R.id.news_list_refresh);
//        refresh.setOnRefreshListener(this);
//        list = (RecyclerView) view.findViewById(R.id.news_list);
//        list.setLayoutManager(new LinearLayoutManager(getActivity()));
//        list.setHasFixedSize(true);
//        adapter = new NewsListAdapter();
//        adapter.setOnClickListener(this);
//        list.setAdapter(adapter);
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
////        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(getActivity());
////        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewsListViewModel.class);
////        viewModel.loadList(new Request(Constants.NEWS_CATEGORY_ALL, 0)).observe(this, item -> {
////            if (refresh.isRefreshing()) {
////                refresh.setEnabled(false);
////            }
////            if (item != null) {
////                if (item.data != null) {
////                    progress.setVisibility(View.GONE);
////                    refresh.setVisibility(View.VISIBLE);
////                    adapter.insertData(item.data);
////                }
////
////                if (item.status == Status.LOADING) {
////                    if (item.message != null && item.message.equals(Constants.NEWS_LOAD_DATA_TASK)) {
////                        showProgress(true);
////                    } else if (item.message != null && item.message.equals(Constants.NEWS_UPDATE_BACKGROUND_TASK)) {
////                        // show progress in toolbar
////
////                        showProgress(false);
////                        adapter.insertData(item.data);
////                    }
////                } else if (item.status == Status.SUCCESS) {
////                    showProgress(false);
////                    // hide toolbar progress
////
////                    adapter.insertData(item.data);
////                } else if (item.status == Status.ERROR) {
////                    // show error message
////                }
////            }
////
////        });
//
////        viewModel.setDataToParent(Resource.progress(true));
//    }
//
//    private void showProgress(boolean show) {
//        if (show) {
//            if (progress.getVisibility() == View.GONE) {
//                if (refresh.getVisibility() == View.VISIBLE) {
//                    /*
//                    * бля, так не должно быть, но на всякий пусть будет так.
//                    */
//                    if (adapter.getItemCount() > 0){
//                        adapter.clear();
//                    }
//                    refresh.setVisibility(View.GONE);
//                }
//                progress.setVisibility(View.VISIBLE);
//            }
//        } else {
//            if (progress.getVisibility() == View.VISIBLE) {
//                progress.setVisibility(View.GONE);
//                if (refresh.getVisibility() == View.GONE) {
//                    refresh.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void itemClick(View view, int position) {
//        News model = adapter.getItem(position);
//        Log.d(TAG, "CLICK ITEM " + model.title);
//        Bundle args = new Bundle();
//        args.putString(Constants.D_URL, model.url);
//        args.putString(Constants.D_IMG, model.imgUrl);
//        args.putString(Constants.D_TITLE, model.title);
//        args.putString(Constants.D_USERNAME, model.author);
//        args.putString(Constants.D_DATE, model.date);
//        args.putLong(Constants.D_ID, model.id);
////        TabManager.getInstance().add(new TabFragment.Builder<>(NewsDetailsParentFragment.class).setArgs(args).build());
//        TabManager.getInstance().add(new TabFragment.Builder<>(NewsDetailsParentFragment.class).setArgs(args).build(), view, this);
////        TabManager.getInstance().add(new TabFragment.Builder<>(NewsDetailsContentFragment2.class).setArgs(args).build());
//    }
//
//    @Override
//    public void onRefresh() {
////        viewModel.refresh(new Request(Constants.NEWS_CATEGORY_ALL, 0));
//    }
//}
