package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.widget.LinearLayout;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 03.09.16.
 */
public class LiTag extends BaseTag {
    public LiTag(Context context) {
        super(context);
        //setBackgroundColor(red);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(App.px8, 0, 0, App.px8);

        setLayoutParams(params);
    }
}
