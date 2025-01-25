package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.util.AttributeSet
import com.makeramen.roundedimageview.RoundedImageView
import forpdateam.ru.forpda.R
import kotlin.math.min

/**
 * Created by radiationx on 26.08.17.
 */
class RoundAspectRatioImageView : RoundedImageView {
    private var aspectRatio = 1.0f

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AspectRatio)
        aspectRatio = typedArray.getFloat(R.styleable.AspectRatio_aspectRatio, 1f)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height =
            min(
                (measuredWidth * aspectRatio).toDouble(),
                maxHeight.toDouble()
            ).toFloat()
        setMeasuredDimension(widthMeasureSpec, (height).toInt())
    }

    fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        requestLayout()
    }
}
