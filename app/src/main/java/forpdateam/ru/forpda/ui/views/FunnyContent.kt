package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R

/**
 * Created by radiationx on 06.10.17.
 */
class FunnyContent(context: Context?) : RelativeLayout(context) {
    private val image: ImageView
    private val title: TextView
    private val desc: TextView

    init {
        inflate(context, R.layout.funny_content, this)
        image = findViewById(R.id.funny_image)
        title = findViewById(R.id.funny_title)
        desc = findViewById(R.id.funny_desc)
    }

    fun setImage(@DrawableRes resId: Int): FunnyContent {
        image.setImageDrawable(getVecDrawable(context, resId))
        return this
    }

    /*public FunnyContent setTitle(String text) {
        title.setText(text);
        title.setVisibility(VISIBLE);
        return this;
    }*/
    /*public FunnyContent setDesc(String text) {
        desc.setText(text);
        desc.setVisibility(VISIBLE);
        return this;
    }*/
    fun setTitle(@StringRes resId: Int): FunnyContent {
        title.setText(resId)
        title.visibility = VISIBLE
        return this
    }

    fun setDesc(@StringRes resId: Int): FunnyContent {
        desc.setText(resId)
        desc.visibility = VISIBLE
        return this
    }
}
