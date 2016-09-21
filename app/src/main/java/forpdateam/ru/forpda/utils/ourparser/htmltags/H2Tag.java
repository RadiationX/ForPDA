package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 03.09.16.
 */
public class H2Tag extends BaseTag {
    @Override
    protected float size() {
        return super.size() * 1.5f;
    }

    public H2Tag(Context context) {
        super(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, App.px32, 0, App.px16);

        setLayoutParams(params);
    }
}
