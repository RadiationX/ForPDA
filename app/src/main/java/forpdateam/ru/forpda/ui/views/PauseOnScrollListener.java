package forpdateam.ru.forpda.ui.views;

import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by radiationx on 06.10.17.
 */

public class PauseOnScrollListener extends RecyclerView.OnScrollListener {
    private ImageLoader imageLoader;
    private final boolean pauseOnScroll;
    private final boolean pauseOnSettling;

    public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnSettling) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnSettling = pauseOnSettling;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                imageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (pauseOnScroll) {
                    imageLoader.pause();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                if (pauseOnSettling) {
                    imageLoader.pause();
                }
                break;
        }
    }
}