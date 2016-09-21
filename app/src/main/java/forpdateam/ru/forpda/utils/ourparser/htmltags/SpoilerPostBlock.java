package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 14.09.16.
 */
public class SpoilerPostBlock extends PostBlock {
    boolean open = false;

    public SpoilerPostBlock(Context context) {
        super(context);
        setBackgroundResource(R.drawable.post_block_spoiler_bg);
        textAppearanceRes = R.style.SpoilerTitleTextStyle;
        blockBody.setVisibility(GONE);
        setTitleOnClickListener(v -> {
            if (open) {
                blockBody.setVisibility(GONE);
                open = false;
            } else {
                blockBody.setVisibility(VISIBLE);
                open = true;
            }
        });
    }
}
