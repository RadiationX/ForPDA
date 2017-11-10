package forpdateam.ru.forpda.ui.activities.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
    private ImagesAdapter.OnClickListener clickListener = null;
    private boolean crop = false;

    public ImagesAdapter(Context context, List<String> urls) {
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

    public void setOnClickListener(ImagesAdapter.OnClickListener onClickListener) {
        this.clickListener = onClickListener;
    }

    public void setCropImg(boolean crop) {
        this.crop = crop;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d("SUKA", "instantiateItem " + position);
        View imageLayout = inflater.inflate(R.layout.img_view_page, container, false);
        assert imageLayout != null;
        container.addView(imageLayout, 0);
        loadImage(imageLayout, position);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d("SUKA", "instantiateItem " + position);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    private void loadImage(View imageLayout, int position) {
        assert imageLayout != null;
        CircularProgressView progressBar = (CircularProgressView) imageLayout.findViewById(R.id.progress_bar);
        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photo_view);
        if (crop) photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (clickListener != null)
            photoView.setOnClickListener(v -> clickListener.itemClick(v, position));
        imageLoader.displayImage(urls.get(position), photoView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
                //delayedHide(1000);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
                if (progressBar.isIndeterminate()) {
                    progressBar.setIndeterminate(false);
                    progressBar.stopAnimation();
                }
            }
        }, (s, view, i, i1) -> progressBar.setProgress((int) (100F * i / i1)));

        photoView.setOnPhotoTapListener(tapListener);
    }

    public interface OnClickListener {
        void itemClick(View view, int position);
    }
}
