package forpdateam.ru.forpda.ui.activities.imageviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.databinding.ImgViewPageBinding

/**
 * Created by radiationx on 24.05.17.
 */

class ImageViewerAdapter : PagerAdapter() {

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
        val binding = ImgViewPageBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        container.addView(binding.root, 0)
        loadImage(binding, position)
        return binding
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    private fun loadImage(binding: ImgViewPageBinding, position: Int) {
        binding.progressBar.visibility = View.VISIBLE
        val item = items[position]


        ImageLoader.getInstance()
            .displayImage(item, binding.photoView, null, object : SimpleImageLoadingListener() {
                override fun onLoadingFailed(
                    imageUri: String?,
                    view: View?,
                    failReason: FailReason?
                ) {
                    binding.progressBar.visibility = View.GONE
                }

                override fun onLoadingComplete(
                    imageUri: String?,
                    view: View?,
                    loadedImage: Bitmap?
                ) {
                    binding. progressBar.visibility = View.GONE
                    //delayedHide(1000);
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    binding.progressBar.visibility = View.GONE
                }

                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    binding.progressBar.visibility = View.VISIBLE
                    if (binding.progressBar.isIndeterminate) {
                        binding.progressBar.isIndeterminate = false
                        binding.progressBar.stopAnimation()
                    }
                }
            }) { s, view, i, i1 -> binding.progressBar.progress = (100f * i / i1).toInt().toFloat() }

        binding.photoView.setOnPhotoTapListener(tapListener)
    }

}
