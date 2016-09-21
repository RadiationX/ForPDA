package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 14.09.16.
 */
public class CodePostBlock extends PostBlock {
    public CodePostBlock(Context context) {
        super(context);
        setBackgroundResource(R.drawable.post_block_code_bg);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        ((ViewGroup)blockBody.getParent()).removeView(blockBody);
        horizontalScrollView.addView(blockBody);
        addView(horizontalScrollView);
    }
}
