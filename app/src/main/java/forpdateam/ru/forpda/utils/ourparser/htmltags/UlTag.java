package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 03.09.16.
 */
public class UlTag extends BaseTag {
    public UlTag(Context context) {
        super(context);
        //setBackgroundColor(red);
        setPadding(App.px8, App.px8, 0, App.px4);
    }
}
