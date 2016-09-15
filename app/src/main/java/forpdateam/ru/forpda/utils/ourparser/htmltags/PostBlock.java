package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.text.Spanned;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by radiationx on 14.09.16.
 */
public class PostBlock extends BaseTag {
    protected LinearLayout blockTitle;
    protected LinearLayout blockBody;
    public PostBlock(Context context) {
        super(context);
        setBackgroundColor(Color.RED);
        blockTitle = new LinearLayout(context);
        blockBody = new LinearLayout(context);
        blockTitle.setOrientation(LinearLayout.VERTICAL);
        blockBody.setOrientation(LinearLayout.VERTICAL);
        blockTitle.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        blockBody.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        blockTitle.setPadding(px3,px3, px3, px3);
        blockBody.setPadding(px3,px3, px3, px3);
        addView(blockTitle);
        addView(blockBody);
        blockTitle.setBackgroundColor(Color.argb(24,0,0,0));
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, px2,0, px2);
        setLayoutParams(params);
    }

    public void hideTitle(){
        blockTitle.setVisibility(GONE);
    }
    public void setTitle(String title){
        TextView textView = new TextView(getContext());
        textView.setText(title);
        blockTitle.addView(textView);
    }
    public void setTitle(Spanned title){
        TextView textView = new TextView(getContext());
        textView.setText(title);
        blockTitle.addView(textView);
    }
    public void addBody(View v){
        blockBody.addView(v);
    }
}
