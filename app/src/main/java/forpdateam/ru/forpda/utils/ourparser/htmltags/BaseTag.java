package forpdateam.ru.forpda.utils.ourparser.htmltags;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Spanned;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.utils.ourparser.Html;
import forpdateam.ru.forpda.utils.ourparser.LinkMovementMethod;

/**
 * Created by radiationx on 28.08.16.
 */
public class BaseTag extends LinearLayout {
    protected float size() {
        return 16;
    }

    public BaseTag(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public TextView setHtmlText(String text) {
        TextView textView = new TextView(getContext());
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setText(Html.fromHtml(text, Html.FROM_HTML_OPTION_USE_CSS_COLORS));
        textView.setTextSize(size());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        addView(textView);
        return textView;
    }

    private class FormatTextTask extends AsyncTask<Void, Void, Void> {
        private TextView textView;
        private String text;
        private Spanned spanned;

        FormatTextTask(TextView textView, String text) {
            this.textView = textView;
            this.text = text;
        }

        protected Void doInBackground(Void... urls) {
            spanned = Html.fromHtml(text, Html.FROM_HTML_OPTION_USE_CSS_COLORS);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (textView != null){
                textView.setText(spanned);
                if(spanned.toString().isEmpty()){
                    textView.setVisibility(GONE);
                }
            }
        }
    }

    public void setImage(String url) {
        ImageView imageView = new ImageView(getContext());
        ImageLoader.getInstance().displayImage(url, imageView);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, App.px8, 0, App.px8);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(imageView);
    }
}
