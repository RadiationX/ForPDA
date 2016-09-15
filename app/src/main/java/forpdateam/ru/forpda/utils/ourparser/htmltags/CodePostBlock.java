package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * Created by radiationx on 14.09.16.
 */
public class CodePostBlock extends PostBlock {
    public CodePostBlock(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#fddbcc"));
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        ((ViewGroup)blockBody.getParent()).removeView(blockBody);
        horizontalScrollView.addView(blockBody);
        addView(horizontalScrollView);
    }
}
