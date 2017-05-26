package forpdateam.ru.forpda.views.messagepanel;

import android.content.Context;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 26.05.17.
 */

public class SimpleInstruction extends ScrollView {
    private TextView messageView;
    private Button closeButton;
    private OnClickListener listener;

    public SimpleInstruction(Context context) {
        super(context);
        addView(inflate(context, R.layout.message_panel_instruction, null));
        setFillViewport(true);
        messageView = (TextView) findViewById(R.id.instruction_message);
        closeButton = (Button) findViewById(R.id.instruction_close_button);
        closeButton.setOnClickListener((v) -> {
            this.setVisibility(GONE);
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }

    public void setText(String text) {
        messageView.setText(text);
    }

    public void setOnCloseClick(OnClickListener listener) {
        this.listener = listener;
    }
}
