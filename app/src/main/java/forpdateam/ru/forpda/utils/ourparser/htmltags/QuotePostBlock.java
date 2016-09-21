package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 14.09.16.
 */
public class QuotePostBlock extends PostBlock {
    public QuotePostBlock(Context context) {
        super(context);
        setBackgroundResource(R.drawable.post_block_quote_bg);
        textAppearanceRes = R.style.QuoteTitleTextStyle;
    }

    public void addQuoteArrow() {
        blockTitle.setOrientation(HORIZONTAL);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        //imageView.setPadding(px12, px12, px12, px12);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.rightMargin = App.px16;
        params.gravity = Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(params);
        //imageView.setPadding(0,0,px8,0);
        imageView.setMinimumHeight(App.px24);
        imageView.setMinimumWidth(App.px24);
        blockTitle.addView(imageView);
    }
}
