package forpdateam.ru.forpda.ui.activities.imageviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoView
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R

/**
 * Created by radiationx on 24.05.17.
 */

class ImageViewerAdapter : PagerAdapter() {

    private val options by lazy {
        App.defaultOptionsUIL
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .considerExifParams(true)
            .build()
    }

    private var tapListener: OnPhotoTapListener? = null

    private val items = mutableListOf<String>()

    fun setTapListener(tapListener: OnPhotoTapListener) {
        this.tapListener = tapListener
    }

    fun bindItem(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater
            .from(container.context)
            .inflate(R.layout.img_view_page, container, false)
        container.addView(imageLayout, 0)
        loadImage(imageLayout, position)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    private fun loadImage(imageLayout: View, position: Int) {
        val progressBar = imageLayout.findViewById<CircularProgressView>(R.id.progress_bar)
        val photoView: PhotoView = imageLayout.findViewById(R.id.photo_view)
        progressBar.visibility = View.VISIBLE
        val item = items[position]


        ImageLoader.getInstance()
            .displayImage(item, photoView, options, object : SimpleImageLoadingListener() {
                override fun onLoadingFailed(
                    imageUri: String?,
                    view: View?,
                    failReason: FailReason?
                ) {
                    progressBar.visibility = View.GONE
                }

                override fun onLoadingComplete(
                    imageUri: String?,
                    view: View?,
                    loadedImage: Bitmap?
                ) {
                    progressBar.visibility = View.GONE
                    //delayedHide(1000);
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    progressBar.visibility = View.GONE
                }

                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    progressBar.visibility = View.VISIBLE
                    if (progressBar.isIndeterminate) {
                        progressBar.isIndeterminate = false
                        progressBar.stopAnimation()
                    }
                }
            }) { s, view, i, i1 -> progressBar.progress = (100f * i / i1).toInt().toFloat() }

        photoView.setOnPhotoTapListener(tapListener)
    }

}
