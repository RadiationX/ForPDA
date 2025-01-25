package forpdateam.ru.forpda.ui.views

import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader

/**
 * Created by radiationx on 06.10.17.
 */
class PauseOnScrollListener(
    private val imageLoader: ImageLoader,
    private val pauseOnScroll: Boolean,
    private val pauseOnSettling: Boolean
) : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> imageLoader.resume()
            RecyclerView.SCROLL_STATE_DRAGGING -> if (pauseOnScroll) {
                imageLoader.pause()
            }

            RecyclerView.SCROLL_STATE_SETTLING -> if (pauseOnSettling) {
                imageLoader.pause()
            }
        }
    }
}