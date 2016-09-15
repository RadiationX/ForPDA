package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

/**
 * Created by radiationx on 14.09.16.
 */
public class SpoilerPostBlock extends PostBlock {
    boolean open = false;

    public SpoilerPostBlock(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#e4eaf2"));
        blockBody.setVisibility(GONE);
        blockTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (open) {
                    blockBody.setVisibility(GONE);
                    open = false;
                } else {
                    blockBody.setVisibility(VISIBLE);
                    open = true;
                }
            }
        });
    }
}
