package forpdateam.ru.forpda.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 26.08.17.
 */

public class AspectRatioImageView extends android.support.v7.widget.AppCompatImageView {
    private float aspectRatio = 1.0f;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AspectRatio);
        aspectRatio = typedArray.getFloat(R.styleable.AspectRatio_aspectRatio, 1);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float height = Math.min(getMeasuredWidth() * aspectRatio, getMaxHeight());
        setMeasuredDimension(widthMeasureSpec, (int) (height));
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        requestLayout();
    }
}
