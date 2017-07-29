package forpdateam.ru.forpda.fragments.theme.editpost;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;
import forpdateam.ru.forpda.api.theme.models.Poll;
import forpdateam.ru.forpda.views.messagepanel.attachments.CustomBottomSheetDialog;

/**
 * Created by radiationx on 28.07.17.
 */

public class EditPollPopup {
    private BottomSheetDialog dialog;
    private View bottomSheet;

    private TextView pollTitle;
    private EditText pollTitleField;
    private ImageButton addPoll;
    private RecyclerView questionsView;

    private PollQuestionsAdapter questionsAdapter;
    private EditPoll poll;


    public EditPollPopup(Context context) {
        dialog = new BottomSheetDialog(context);
        //dialog.getWindow().getDecorView().setFitsSystemWindows(true);

        bottomSheet = View.inflate(context, R.layout.edit_poll, null);

        pollTitle = (TextView) bottomSheet.findViewById(R.id.poll_title);
        pollTitleField = (EditText) bottomSheet.findViewById(R.id.poll_title_field);
        addPoll = (ImageButton) bottomSheet.findViewById(R.id.add_poll);
        questionsView = (RecyclerView) bottomSheet.findViewById(R.id.poll_questions);

        questionsView.setLayoutManager(new LinearLayoutManager(questionsView.getContext()));

        addPoll.setOnClickListener(v -> {
            questionsAdapter.add(new EditPoll.Question());
        });


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }
                //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }*/
    }

    public void show() {
        if (bottomSheet != null && bottomSheet.getParent() != null && bottomSheet.getParent() instanceof ViewGroup) {
            ((ViewGroup) bottomSheet.getParent()).removeView(bottomSheet);
        }
        dialog.setContentView(bottomSheet);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        dialog.show();
    }

    public void setPoll(EditPoll poll) {
        this.poll = poll;
        pollTitleField.setText(poll.getTitle());
        questionsAdapter = new PollQuestionsAdapter(poll.getQuestions(), poll);
        questionsView.setAdapter(questionsAdapter);
    }

}
