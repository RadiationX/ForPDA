package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

/**
 * Created by radiationx on 03.09.16.
 */
public class H1Tag extends BaseTag {
    @Override
    protected float size() {
        return super.size() * 2f;
    }

    public H1Tag(Context context) {
        super(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, px32,0, px16);
        setLayoutParams(params);
        setGravity(Gravity.CENTER_HORIZONTAL);
    }
}
