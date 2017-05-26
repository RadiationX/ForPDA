package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.SimpleInstruction;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.ItemDragCallback;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.PanelItemAdapter;


/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class CodesPanelItem extends BasePanelItem {
    private static List<ButtonData> codes = null;
    private List<String> openedCodes = new ArrayList<>();

    public CodesPanelItem(Context context, MessagePanel panel) {
        super(context, panel, "Оформление");
        PanelItemAdapter adapter = new PanelItemAdapter(getCodes(), null, PanelItemAdapter.TYPE_DRAWABLE);

        adapter.setOnItemClickListener(item -> {
            int indexOf = openedCodes.indexOf(item.getText());
            String startText, endText = null, tag = item.getText();
            startText = "[".concat(indexOf >= 0 ? "/" : "").concat(tag).concat("]");
            if (indexOf < 0)
                endText = "[/".concat(tag).concat("]");

            if (messagePanel.insertText(startText, endText)) return;

            if (indexOf >= 0) {
                openedCodes.remove(indexOf);
            } else {
                openedCodes.add(item.getText());
            }
        });
        recyclerView.setColumnWidth(App.getInstance().dpToPx(96));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemDragCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        if (!App.getInstance().getPreferences().getBoolean("message_panel.instruction.user_sorting", false)) {
            SimpleInstruction instruction = new SimpleInstruction(getContext());
            instruction.setText("Вы можете настроить расположение элементов, перемещяя их!");
            instruction.setOnCloseClick((v) -> {
                App.getInstance().getPreferences().edit().putBoolean("message_panel.instruction.user_sorting", true).apply();
            });
            addView(instruction);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        List<String> listCodes = new ArrayList<>();
        for (ButtonData item : codes) {
            listCodes.add(item.getText());
        }
        String sorted = TextUtils.join(",", listCodes);
        App.getInstance().getPreferences().edit().putString("message_panel.advanced.codes.sorted", sorted).apply();
        super.onDetachedFromWindow();
    }

    public static List<ButtonData> getCodes() {
        if (codes != null) return codes;
        codes = new ArrayList<>();
        ArrayList<ButtonData> tempCodes = new ArrayList<>();
        tempCodes.add(new ButtonData("B", R.drawable.ic_code_bold, "Жирный"));
        tempCodes.add(new ButtonData("I", R.drawable.ic_code_italic, "Курсив"));
        tempCodes.add(new ButtonData("U", R.drawable.ic_code_underline, "Подчеркнуть"));
        tempCodes.add(new ButtonData("S", R.drawable.ic_code_s, "Зачеркнуть"));
        tempCodes.add(new ButtonData("SUB", R.drawable.ic_code_sub, "Снизу"));
        tempCodes.add(new ButtonData("SUP", R.drawable.ic_code_sup, "Сверху"));
        tempCodes.add(new ButtonData("LEFT", R.drawable.ic_code_left, "Влево"));
        tempCodes.add(new ButtonData("CENTER", R.drawable.ic_code_center, "По центру"));
        tempCodes.add(new ButtonData("RIGHT", R.drawable.ic_code_right, "Вправо"));
        tempCodes.add(new ButtonData("OFFTOP", R.drawable.ic_code_offtop, "Оффтоп"));
        tempCodes.add(new ButtonData("HIDE", R.drawable.ic_code_hide, "Скрытый"));
        tempCodes.add(new ButtonData("CUR", R.drawable.ic_code_cur, "Куратор"));

        tempCodes.add(new ButtonData("URL", R.drawable.ic_code_url, "Ссылка"));
        tempCodes.add(new ButtonData("QUOTE", R.drawable.ic_code_quote, "Цитата"));
        tempCodes.add(new ButtonData("CODE", R.drawable.ic_code_code, "Код"));
        tempCodes.add(new ButtonData("SPOILER", R.drawable.ic_code_spoiler, "Спойлер"));
        tempCodes.add(new ButtonData("LIST", R.drawable.ic_code_list, "Обычный список"));
        tempCodes.add(new ButtonData("NUMLIST", R.drawable.ic_code_numlist, "Нумерованный список"));
        tempCodes.add(new ButtonData("COLOR", R.drawable.ic_code_color, "Цвет текста"));
        tempCodes.add(new ButtonData("BACKGROUND", R.drawable.ic_code_background, "Цвет фона"));
        tempCodes.add(new ButtonData("SIZE", R.drawable.ic_code_size, "Размер текста"));


        String sorted = App.getInstance().getPreferences().getString("message_panel.advanced.codes.sorted", null);
        if (sorted != null) {
            for (String code : TextUtils.split(sorted, ",")) {
                for (ButtonData item : tempCodes) {
                    if (item.getText().equals(code)) {
                        codes.add(item);
                        break;
                    }
                }
            }
        } else {
            codes.addAll(tempCodes);
        }
        tempCodes.clear();

        return codes;
    }

}
