package forpdateam.ru.forpda.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.makeramen.roundedimageview.RoundedImageView;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 26.08.17.
 */

public class RoundAspectRatioImageView extends RoundedImageView {
    private float aspectRatio = 1.0f;

    public RoundAspectRatioImageView(Context context) {
        super(context);
    }

    public RoundAspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoundAspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
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
