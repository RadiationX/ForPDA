//package forpdateam.ru.forpda.fragments.news.main;
//
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentStatePagerAdapter;
//
//import forpdateam.ru.forpda.fragments.news.main.category.CategoryFragment;
//import forpdateam.ru.forpda.fragments.news.main.timeline.NewsTimelineFragment;
//
///**
// * Created by isanechek on 8/8/17.
// */
//
//public class NewsMainPageAdapter extends FragmentStatePagerAdapter {
//    public NewsMainPageAdapter(FragmentManager fm) {
//        super(fm);
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                return NewsTimelineFragment.createInstance();
//            case 1:
//                return CategoryFragment.createInstance();
//            default:
//                return NewsTimelineFragment.createInstance();
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return 2;
//    }
//}
