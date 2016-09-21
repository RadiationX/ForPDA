package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.utils.ourparser.LinkMovementMethod;

/**
 * Created by radiationx on 14.09.16.
 */
public class PostBlock extends BaseTag {
    protected LinearLayout blockTitle;
    protected LinearLayout blockBody;
    protected View.OnClickListener titleOnClickListener;
    protected int textAppearanceRes = android.R.style.TextAppearance;

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
        blockTitle.setPadding(px16, px12, px16, px12);
        blockBody.setPadding(px16, px16, px16, px16);
        addView(blockTitle);
        addView(blockBody);
        //blockTitle.setBackgroundColor(Color.argb(24, 0, 0, 0));
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, px8, 0, px8);
        setLayoutParams(params);
    }

    public void setTitleOnClickListener(OnClickListener titleOnClickListener) {
        this.titleOnClickListener = titleOnClickListener;
        blockTitle.setOnClickListener(this.titleOnClickListener);
    }

    public void hideTitle() {
        blockTitle.setVisibility(GONE);
    }

    public void setTitle(Spanned title) {
        TextView textView = new TextView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        textView.setLayoutParams(params);
        textView.setText(title);
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(App.getContext(), textAppearanceRes);
        } else {
            textView.setTextAppearance(textAppearanceRes);
        }
        if (titleOnClickListener == null)
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        else
            textView.setOnClickListener(titleOnClickListener);
        blockTitle.addView(textView);
    }

    public void addBody(View v) {
        blockBody.addView(v);
    }

    public LinearLayout getBlockTitle() {
        return blockTitle;
    }
}
