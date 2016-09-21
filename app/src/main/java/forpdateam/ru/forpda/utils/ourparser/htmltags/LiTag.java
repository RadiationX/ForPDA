package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * Created by radiationx on 03.09.16.
 */
public class LiTag extends BaseTag {
    public LiTag(Context context) {
        super(context);
        //setBackgroundColor(red);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(px8, 0, 0, px8);

        setLayoutParams(params);
    }
}
