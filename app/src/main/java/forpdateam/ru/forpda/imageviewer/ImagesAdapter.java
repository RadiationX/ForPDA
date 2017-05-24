package forpdateam.ru.forpda.imageviewer;

import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;


/**
 * Created by radiationx on 24.05.17.
 */

public class ImagesAdapter extends PagerAdapter {
    //private SparseArray<View> views = new SparseArray<>();
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<String> urls;
    private OnPhotoTapListener tapListener;

    public ImagesAdapter(ImageViewerActivity context, List<String> urls) {
        this.inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();

        options = App.getDefaultOptionsUIL()
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .considerExifParams(true)
                .build();
        this.urls = urls;
    }

    public void setTapListener(OnPhotoTapListener tapListener) {
        this.tapListener = tapListener;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("FORPDA_LOG", "instantiateItem " + position);
        View imageLayout = inflater.inflate(R.layout.img_view_page, container, false);
        assert imageLayout != null;
        container.addView(imageLayout, 0);
        loadImage(imageLayout, position);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.e("FORPDA_LOG", "destroyItem " + position);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    private void loadImage(View imageLayout, int position) {
        assert imageLayout != null;
        ProgressBar progress = (ProgressBar) imageLayout.findViewById(R.id.progress);
        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photo_view);
        imageLoader.displayImage(urls.get(position), photoView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progress.setVisibility(View.INVISIBLE);
                //delayedHide(1000);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progress.setVisibility(View.INVISIBLE);
            }
        });

        photoView.setOnPhotoTapListener(tapListener);
    }
}
